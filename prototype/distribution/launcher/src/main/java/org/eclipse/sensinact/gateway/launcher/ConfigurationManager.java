/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.launcher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.felix.cm.json.ConfigurationReader;
import org.apache.felix.cm.json.ConfigurationResource;
import org.apache.felix.cm.json.Configurations;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = ConfigurationManager.class)
public class ConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);

    @Reference
    ConfigurationAdmin configAdmin;

    WatchService watchService;

    ExecutorService executor;

    Path configFile;

    private boolean expectedInterruption = false;
    private AtomicReference<Thread> currentWatchThread = new AtomicReference<>();

    @Activate
    void start() throws IOException {

        watchService = FileSystems.getDefault().newWatchService();

        configFile = Paths.get(System.getProperty("sensinact.config.dir", "./config"), "configuration.json");

        LOGGER.info("Eclipse sensiNact is watching for changes in configuration file {}", configFile);

        configFile.getParent().register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        executor = Executors.newSingleThreadExecutor(this::createThread);

        executor.submit(this::reloadConfigFile);
        executor.submit(this::watch);
    }

    private Thread createThread(Runnable r) {
        Thread t = new Thread(r, "Configuration management thread");
        t.setDaemon(true);
        return t;
    }

    @Deactivate
    void stop() {
        LOGGER.info("Eclipse sensiNact configuration manager is stopping. Configurations will not be changed");

        try {
            watchService.close();
        } catch (IOException e) {
            LOGGER.warn("Error when stopping the configuration manager", e);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn("Error when stopping the configuration manager", e);
        }
    }

    private void watch() {
        try {
            // Keep track of current watch thread
            currentWatchThread.set(Thread.currentThread());

            WatchKey key;
            try {
                key = watchService.take();
            } catch (ClosedWatchServiceException e) {
                LOGGER.error("No longer able to monitor the configuration file {}", configFile, e);
                return;
            } catch (InterruptedException e) {
                if (expectedInterruption) {
                    LOGGER.debug("Forced configuration reload");
                    Thread.interrupted();
                    executor.submit(this::reloadConfigFile);
                    executor.submit(this::watch);
                } else {
                    LOGGER.error("Interrupted while monitoring the configuration file {}", configFile, e);
                }
                return;
            }

            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                Kind<?> kind = watchEvent.kind();
                if (kind == OVERFLOW) {
                    // If there has been an overflow we don't know whether
                    // there has been a change, so assume there has
                    executor.submit(this::reloadConfigFile);
                    break;
                } else if (Files.isSameFile(configFile.getFileName(), (Path) watchEvent.context())) {
                    // There has been a change to the config file
                    executor.submit(this::reloadConfigFile);
                    break;
                }
            }

            if (key.isValid()) {
                key.reset();
                executor.submit(this::watch);
            } else {
                LOGGER.error("No longer able to monitor the configuration file {} as the key is invalid", configFile);
            }
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred watching the configuration file {}", configFile, e);
            executor.submit(this::watch);
        } finally {
            currentWatchThread.set(null);
        }
    }

    private ConfigurationResource loadConfigFile() throws IOException {
        final ConfigurationResource config;
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                ConfigurationReader configReader = Configurations.buildReader()
                        .withConfiguratorPropertyHandler((a, b, c) -> {
                        }).build(reader);

                config = configReader.readConfigurationResource();

                for (String warning : configReader.getIgnoredErrors()) {
                    LOGGER.warn("Configuration file parsing warning. Error was\n{}", warning);
                }
            }
        } else {
            config = new ConfigurationResource();
        }

        return config;
    }

    private void reloadConfigFile() {
        try {
            ConfigurationResource config = loadConfigFile();

            Map<String, Configuration> existingConfigs = Optional
                    .ofNullable(configAdmin.listConfigurations("(.sensinact.config=true)")).map(Arrays::stream)
                    .map(s -> s.collect(toMap(Configuration::getPid, Function.identity()))).orElse(Map.of());

            for (Entry<String, Hashtable<String, Object>> e : config.getConfigurations().entrySet()) {
                String pid = e.getKey();
                Hashtable<String, Object> value = e.getValue();
                value.put(".sensinact.config", Boolean.TRUE);
                if (existingConfigs.containsKey(pid)) {
                    // Remove so we don't delete later
                    existingConfigs.remove(pid).updateIfDifferent(value);
                } else {
                    createConfig(pid, value);
                }
            }

            for (Configuration c : existingConfigs.values()) {
                c.delete();
            }

        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred parsing the configuration file {}", configFile, e);
        }
    }

    private void createConfig(String pid, Hashtable<String, Object> value) throws IOException {
        int idx = pid.indexOf('~');
        Configuration cfg;
        if (idx < 0) {
            cfg = configAdmin.getConfiguration(pid, "?");
        } else {
            cfg = configAdmin.getFactoryConfiguration(pid.substring(0, idx), pid.substring(idx + 1), "?");
        }
        cfg.update(value);
    }

    private Hashtable<String, Object> mergeConfigs(final Map<String, Object> currentValues,
            final Map<String, Object> defaultValues) {
        final Hashtable<String, Object> newValues = new Hashtable<>(defaultValues);
        newValues.putAll(currentValues);
        return newValues;
    }

    public void updateConfigurations(final Map<String, Hashtable<String, Object>> newConfigurations,
            Collection<String> removedConfigurations) throws IOException {

        // Load current status
        final ConfigurationResource configResource = loadConfigFile();
        final Map<String, Hashtable<String, Object>> allConfigurations = configResource.getConfigurations();

        // Ensure that the configuration resource has the mandatory properties
        final Map<String, Object> configProperties = configResource.getProperties();
        if(configProperties.isEmpty()) {
            // Generate the mandatory properties
            configResource.getProperties().put(":configurator:resource-version", 1);
            // TODO: use the bundle context to get those?
            configResource.getProperties().put(":configurator:symbolic-name", "org.eclipse.sensinact.gateway.launcher");
            configResource.getProperties().put(":configurator:version", "0.0.1");
        }

        // Remove configuration
        if (removedConfigurations != null) {
            removedConfigurations.stream().forEach(allConfigurations::remove);
        }

        // Merge new configurations with the current values
        if (newConfigurations != null) {
            final Map<String, Hashtable<String, Object>> mergedConfigs = new Hashtable<>(newConfigurations.size());
            for (final Entry<String, Hashtable<String, Object>> entry : newConfigurations.entrySet()) {
                final String pid = entry.getKey();
                final Hashtable<String, Object> newConfig = entry.getValue();
                final Hashtable<String, Object> rcConfigProps = allConfigurations.get(pid);
                if (rcConfigProps == null) {
                    mergedConfigs.put(pid, newConfig);
                } else {
                    LOGGER.debug("Reusing configuration properties for PID %s", pid);
                    mergedConfigs.put(pid, mergeConfigs(rcConfigProps, newConfig));
                }
            }

            // Update configuration resource content
            allConfigurations.putAll(mergedConfigs);
        }

        // Write down the new configuration
        try (Writer writer = Files.newBufferedWriter(configFile)) {
            configResource.getProperties().put(":configurator:resource-version", 1);
            // configResource.getProperties().put(":configurator:symbolic-name", "org.eclipse.sensinact.gateway.feature.config.test");
            // configResource.getProperties().put(":configurator:version", "0.0.1");
            Configurations.buildWriter().build(writer).writeConfigurationResource(configResource);
        }

        // Force reload (in right thread)
        final Thread watchThread = currentWatchThread.get();
        if (watchThread != null) {
            expectedInterruption = true;
            watchThread.interrupt();
        }
    }

}
