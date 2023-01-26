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

import static java.util.Map.of;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@ExtendWith(MockitoExtension.class)
class ConfigurationManagerTest {

    @Mock
    ConfigurationAdmin configAdmin;

    @InjectMocks
    ConfigurationManager manager;

    private final Map<String, Configuration> configs = new HashMap<>();

    private final Map<String, Configuration> activeConfigs = new HashMap<>();

    private final Semaphore semaphore = new Semaphore(0);

    @BeforeEach
    void start() throws Exception {

        Mockito.lenient().when(configAdmin.getConfiguration(anyString(), anyString())).thenAnswer(i -> {
            String pid = i.getArgument(0, String.class);
            Configuration mock = Mockito.mock(Configuration.class, pid);
            Mockito.when(mock.getPid()).thenReturn(pid);
            Mockito.doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    activeConfigs.remove(pid);
                    return null;
                }
            }).when(mock).delete();
            configs.put(pid, mock);
            activeConfigs.put(pid, mock);
            semaphore.release();
            return mock;
        });
        Mockito.lenient().when(configAdmin.getFactoryConfiguration(anyString(), anyString(), anyString()))
                .thenAnswer(i -> {
                    String factoryPid = i.getArgument(0, String.class);
                    String pid = factoryPid + "~" + i.getArgument(1, String.class);
                    Configuration mock = Mockito.mock(Configuration.class, pid);
                    Mockito.when(mock.getFactoryPid()).thenReturn(factoryPid);
                    Mockito.when(mock.getPid()).thenReturn(pid);
                    Mockito.doAnswer(new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocation) throws Throwable {
                            activeConfigs.remove(pid);
                            return null;
                        }
                    }).when(mock).delete();
                    configs.put(pid, mock);
                    activeConfigs.put(pid, mock);
                    semaphore.release();
                    return mock;
                });
        Mockito.lenient().when(configAdmin.listConfigurations(eq("(.sensinact.config=true)"))).then(i -> {
            return activeConfigs.values().toArray(Configuration[]::new);
        });
    }

    @AfterEach
    void stop() {
        configs.clear();
        activeConfigs.clear();
        if (manager != null)
            manager.stop();
    }

    @Nested
    class ConfigFileWatching {

        @Test
        void testSimpleConfig() throws Exception {
            System.setProperty("sensinact.config.dir", "target/test-classes/configs/a");
            manager.start();

            assertTrue(semaphore.tryAcquire(2, 1, SECONDS));

            Mockito.verify(configAdmin).getConfiguration(eq("test.pid"), anyString());
            Mockito.verify(configAdmin).getFactoryConfiguration(eq("test.factory.pid"), eq("boo"), anyString());

            Mockito.verify(configs.get("test.pid"), Mockito.timeout(500))
                    .update(argThat(isConfig(of("foo", "bar", "fizzbuzz", 42L, ".sensinact.config", true))));
            Mockito.verify(configs.get("test.factory.pid~boo"), Mockito.timeout(500))
                    .update(argThat(isConfig(of("fizz", "buzz", "foobar", 24L, ".sensinact.config", true))));
        }

        @Test
        void testSimpleUpdate() throws Exception {
            Path path = Paths.get("target/test-classes/configs/b");
            System.setProperty("sensinact.config.dir", path.toString());

            Path configFile = path.resolve("configuration.json");

            if (Files.exists(configFile)) {
                Files.delete(configFile);
            }

            manager.start();

            Thread.sleep(500);

            Mockito.verify(configAdmin).listConfigurations(anyString());

            Mockito.verifyNoMoreInteractions(configAdmin);

            Files.copy(path.resolve("configuration-initial.json"), configFile);

            assertTrue(semaphore.tryAcquire(3, 15, SECONDS));

            Mockito.verify(configAdmin).getConfiguration(eq("test.pid"), anyString());
            Mockito.verify(configAdmin).getConfiguration(eq("test.pid.temp"), anyString());
            Mockito.verify(configAdmin).getFactoryConfiguration(eq("test.factory.pid"), eq("boo"), anyString());

            Mockito.verify(configs.get("test.pid"), Mockito.timeout(500))
                    .update(argThat(isConfig(of("foo", "bar", "fizzbuzz", 42L, ".sensinact.config", true))));
            Mockito.verify(configs.get("test.pid.temp"), Mockito.timeout(500))
                    .update(argThat(isConfig(of("ttl", 1L, ".sensinact.config", true))));
            Mockito.verify(configs.get("test.factory.pid~boo"), Mockito.timeout(500))
                    .update(argThat(isConfig(of("fizz", "buzz", "foobar", 24L, ".sensinact.config", true))));

            Files.newInputStream(path.resolve("configuration-update.json")).transferTo(Files.newOutputStream(configFile));

            // Mockito timeout as there is no creation of configurations
            Mockito.verify(configs.get("test.pid"), Mockito.timeout(500).atLeast(1))
                    .updateIfDifferent(argThat(isConfig(of("bar", "foo", "fizzbuzz", 84L, ".sensinact.config", true))));
            Mockito.verify(configs.get("test.pid.temp"), Mockito.timeout(500)).delete();
            Mockito.verify(configs.get("test.factory.pid~boo"), Mockito.timeout(500).atLeast(1))
                    .updateIfDifferent(argThat(isConfig(of("buzz", "fizz", "foobar", 48L, ".sensinact.config", true))));
        }

    }

    @Nested
    class ConfigFileUpdate {

        Path tmpFolder;
        Path configFile;

        @BeforeEach
        void setup() throws Exception {
            tmpFolder = Files.createTempDirectory("sensinact-config-test");
            System.setProperty("sensinact.config.dir", tmpFolder.toString());
            configFile = tmpFolder.resolve("configuration.json");
            if (Files.exists(configFile)) {
                Files.delete(configFile);
            }

            manager.start();
        }

        @AfterEach
        void cleanup() throws Exception {
            if (configFile != null && Files.exists(configFile)) {
                Files.delete(configFile);
            }

            Files.delete(tmpFolder);
        }

        @Test
        void testNullProtection() throws Exception {
            manager.updateConfigurations(null, null);
            assertTrue(activeConfigs.isEmpty());
        }

        @Test
        void testAddition() throws Exception {
            Map<String, Hashtable<String, Object>> addedConfs = Map.of("test1",
                    new Hashtable<>(Map.of("txt", "A", "value", 21)), "test2",
                    new Hashtable<>(Map.of("txt", "B", "value", 42)));

            manager.updateConfigurations(addedConfs, null);
            assertTrue(semaphore.tryAcquire(2, 1, SECONDS));
            assertTrue(activeConfigs.containsKey("test1"));
            assertTrue(activeConfigs.containsKey("test2"));
            assertEquals(2, activeConfigs.size());
        }
    }

    ArgumentMatcher<Dictionary<String, Object>> isConfig(Map<String, Object> config) {
        return new ArgumentMatcher<Dictionary<String, Object>>() {

            @Override
            public boolean matches(Dictionary<String, Object> arg) {
                Set<String> keySet = new HashSet<>();
                Enumeration<String> keys = arg.keys();
                while (keys.hasMoreElements()) {
                    keySet.add(keys.nextElement());
                }
                if (config.keySet().equals(keySet)) {
                    return keySet.stream().allMatch(s -> arg.get(s).equals(config.get(s)));
                }

                return false;
            }

            @Override
            public String toString() {
                return "Expected config " + config;
            }
        };
    }
}
