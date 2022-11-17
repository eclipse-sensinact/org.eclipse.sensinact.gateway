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
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

@Component
public class ConfigurationManager {

    @Reference
    ConfigurationAdmin configAdmin;

    WatchService watchService;

    ExecutorService executor;

    private Path configFile;

    @Activate
    void start() throws IOException {

        watchService = FileSystems.getDefault().newWatchService();

        configFile = Paths.get(System.getProperty("sensinact.config.dir", "./config"), "configuration.json");
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

        try {
            watchService.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void watch() {
        try {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (ClosedWatchServiceException | InterruptedException e) {
                // TODO log this
                return;
            }

            for (WatchEvent<?> watchEvent : key.pollEvents()) {
                Kind<?> kind = watchEvent.kind();
                if (kind == OVERFLOW) {
                    // If there has been an overflow we don't know whether
                    // there has been a change, so assume there has
                    executor.submit(this::reloadConfigFile);
                    break;
                } else if (Files.isSameFile(configFile, (Path) watchEvent.context())) {
                    // There has been a change to the config file
                    executor.submit(this::reloadConfigFile);
                    break;
                }
            }

            if (key.isValid()) {
                key.reset();
                executor.submit(this::watch);
            } else {
                // TODO log that configuration is broken
            }
        } catch (Exception e) {
            // TODO log this error
            executor.submit(this::watch);
        }
    }

    private void reloadConfigFile() {
        try {
            ConfigurationResource config;
            if(Files.exists(configFile)) {
                config = new ConfigurationResource();
            } else {
                try (Reader reader = Files.newBufferedReader(configFile)) {
                    ConfigurationReader configReader = Configurations.buildReader()
                            .withConfiguratorPropertyHandler((a, b, c) -> {
                            }).build(reader);
                    
                    config = configReader.readConfigurationResource();
                    
                    if(!configReader.getIgnoredErrors().isEmpty()) {
                        // TODO log these warnings
                    }
                }
            }
            
            Map<String, Configuration> existingConfigs = Optional.ofNullable(configAdmin.listConfigurations("(.sensinact.config=true)"))
                    .map(Arrays::stream)
                    .map(s -> s.collect(toMap(Configuration::getPid, Function.identity())))
                    .orElse(Map.of());
            
            for(Entry<String, Hashtable<String, Object>> e : config.getConfigurations().entrySet()) {
                String pid = e.getKey();
                Hashtable<String,Object> value = e.getValue();
                value.put(".sensinact.config", Boolean.TRUE);
                if(existingConfigs.containsKey(pid)) {
                    // Remove so we don't delete later
                    existingConfigs.remove(pid).updateIfDifferent(value);
                } else {
                    createConfig(pid, value);
                }
            }
            
            for(Configuration c : existingConfigs.values()) {
                c.delete();
            }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createConfig(String pid, Hashtable<String, Object> value) throws IOException {
        int idx = pid.indexOf('~');
        Configuration cfg;
        if(idx < 0) {
            cfg = configAdmin.getConfiguration(pid, "?");
        } else {
            cfg = configAdmin.getFactoryConfiguration(pid.substring(0, idx), pid.substring(idx + 1), "?");
        }
        cfg.update(value);
    }
}
