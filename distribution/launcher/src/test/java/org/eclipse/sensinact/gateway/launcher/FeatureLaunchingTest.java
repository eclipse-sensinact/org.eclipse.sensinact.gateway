/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

import org.eclipse.sensinact.gateway.launcher.FeatureLauncher.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.feature.FeatureService;

@ExtendWith(MockitoExtension.class)
class FeatureLaunchingTest {

    private static final String GROUP_ID = "org.eclipse.sensinact.gateway.launcher.test";

    @Mock
    BundleContext context;

    @Mock
    Config config;

    @Mock
    ConfigurationManager manager;

    FeatureLauncher fl = new FeatureLauncher();

    private FeatureService fs;

    private List<Throwable> failures = new ArrayList<>();

    private Map<String, Bundle> installed = new HashMap<>();

    @BeforeEach
    void start() throws Exception {
        ServiceLoader<FeatureService> loader = ServiceLoader.load(FeatureService.class);
        fs = loader.findFirst().get();

        final BundleRevision revMock = Mockito.mock(BundleRevision.class);
        Mockito.lenient().when(revMock.getTypes()).thenReturn(0);

        Mockito.lenient().when(config.featureDir()).thenReturn(new String[] { "src/test/resources/features" });
        Mockito.lenient().when(config.repository()).thenReturn(new String[] { "src/test/resources/repository" });

        Mockito.lenient().when(context.installBundle(Mockito.anyString(), Mockito.any())).thenAnswer(i -> {
            String fromIs;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(i.getArgument(1, InputStream.class)))) {
                fromIs = br.readLine();
            }
            try {
                assertEquals(i.getArgument(0, String.class), fromIs);
            } catch (AssertionFailedError e) {
                failures.add(e);
            }
            Bundle bundleMock = Mockito.mock(Bundle.class, fromIs);
            Mockito.lenient().when(bundleMock.adapt(eq(BundleRevision.class))).thenReturn(revMock);
            installed.put(fromIs, bundleMock);
            return bundleMock;
        });

        fl.featureService = fs;
        fl.configManager = manager;
    }

    String getIdString(String groupId, String artifactId, String version, String type, String classifier) {
        if (classifier == null || classifier.isBlank()) {
            if (type == null || type.isBlank()) {
                return fs.getID(groupId, artifactId, version).toString();
            } else {
                return fs.getID(groupId, artifactId, version, type).toString();
            }
        } else {
            return fs.getID(groupId, artifactId, version, type, classifier).toString();
        }
    }

    @ParameterizedTest
    @CsvSource({ "1.0.0,,,,", "1.0.0,feature,,bundle,", "1.0.0,json,feature,jar,bundle", "1.0.1,,,,",
            "1.0.1,feature,,bundle,", "1.0.1,json,feature,jar,bundle", "1.0.2-SNAPSHOT,,,,",
            "1.0.2-SNAPSHOT,feature,,bundle,", "1.0.2-SNAPSHOT,json,feature,jar,bundle" })
    void installFeatureWithMavenCoordinates(String version, String type, String classifier, String bundleType,
            String bundleClassifier) throws Exception {

        Mockito.when(config.features())
                .thenReturn(new String[] { getIdString(GROUP_ID, "fizz", version, type, classifier) });
        fl.start(context, config);

        assertTrue(failures.isEmpty(), () -> failures.toString());

        String bundleId = getIdString(GROUP_ID, "buzz", version, bundleType, bundleClassifier);
        InOrder order = Mockito.inOrder(context, installed.get(bundleId));

        order.verify(context).installBundle(eq(bundleId), any());
        order.verify(installed.get(bundleId)).start();
    }

    @Test
    void installFeatureSimpleName() throws Exception {
        Mockito.when(config.features()).thenReturn(new String[] { "foo" });
        fl.start(context, config);

        assertTrue(failures.isEmpty(), () -> failures.toString());

        String bundleId = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);
        String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.1", null, null);

        InOrder order = Mockito.inOrder(context, installed.get(bundleId), installed.get(bundleId2));

        order.verify(context).installBundle(eq(bundleId), any());
        order.verify(context).installBundle(eq(bundleId2), any());

        order.verify(installed.get(bundleId)).start();
        order.verify(installed.get(bundleId2)).start();
    }

    @Test
    void cleanupOnStop() throws Exception {
        installFeatureSimpleName();

        fl.stop();

        Bundle bundle = installed.get(getIdString(GROUP_ID, "buzz", "1.0.0", null, null));
        Bundle bundle2 = installed.get(getIdString(GROUP_ID, "buzz", "1.0.1", null, null));

        InOrder order = Mockito.inOrder(context, bundle, bundle2);

        order.verify(bundle2).stop();
        order.verify(bundle).stop();
        order.verify(bundle2).uninstall();
        order.verify(bundle).uninstall();

        if (manager.configFile != null) {
            Files.deleteIfExists(manager.configFile);
            manager.configFile = null;
        }
    }

    @Test
    void installFeatureListedTwice() throws Exception {
        Mockito.when(config.features()).thenReturn(new String[] { "foo", "foo" });
        fl.start(context, config);

        assertTrue(failures.isEmpty(), () -> failures.toString());

        String bundleId = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);
        String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.1", null, null);

        InOrder order = Mockito.inOrder(context, installed.get(bundleId), installed.get(bundleId2));

        order.verify(context).installBundle(eq(bundleId), any());
        order.verify(context).installBundle(eq(bundleId2), any());

        order.verify(installed.get(bundleId)).start();
        order.verify(installed.get(bundleId2)).start();

        order.verifyNoMoreInteractions();
    }

    @Test
    void installOverlappingFeatures() throws Exception {
        Mockito.when(config.features()).thenReturn(new String[] { "foo", "foobar", "bar" });
        fl.start(context, config);

        assertTrue(failures.isEmpty(), () -> failures.toString());

        String bundleId = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);
        String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.1", null, null);
        String bundleId3 = getIdString(GROUP_ID, "buzz", "1.0.2-SNAPSHOT", null, null);

        InOrder order = Mockito.inOrder(context, installed.get(bundleId), installed.get(bundleId2),
                installed.get(bundleId3));

        // First feature install and start
        order.verify(context).installBundle(eq(bundleId), any());
        order.verify(context).installBundle(eq(bundleId2), any());

        order.verify(installed.get(bundleId)).start();
        order.verify(installed.get(bundleId2)).start();

        // Additional bundle from feature 2 install and start
        order.verify(context).installBundle(eq(bundleId3), any());

        order.verify(installed.get(bundleId3)).start();

        order.verifyNoMoreInteractions();

        fl.stop();

        // All get removed in reverse order
        order.verify(installed.get(bundleId3)).stop();
        order.verify(installed.get(bundleId2)).stop();
        order.verify(installed.get(bundleId)).stop();
        order.verify(installed.get(bundleId3)).uninstall();
        order.verify(installed.get(bundleId2)).uninstall();
        order.verify(installed.get(bundleId)).uninstall();

    }

    @Test
    void updateThatRemovesOverlappingFeatures() throws Exception {

        Mockito.when(config.features()).thenReturn(new String[] { "foo", "foobar", "bar" });
        fl.start(context, config);

        Mockito.when(config.features()).thenReturn(new String[] { "foo", "bar" });
        fl.update(config);

        assertTrue(failures.isEmpty(), () -> failures.toString());

        String bundleId = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);
        String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.1", null, null);
        String bundleId3 = getIdString(GROUP_ID, "buzz", "1.0.2-SNAPSHOT", null, null);

        InOrder order = Mockito.inOrder(context, installed.get(bundleId), installed.get(bundleId2),
                installed.get(bundleId3));

        // First feature install and start
        order.verify(context).installBundle(eq(bundleId), any());
        order.verify(context).installBundle(eq(bundleId2), any());

        order.verify(installed.get(bundleId)).start();
        order.verify(installed.get(bundleId2)).start();

        // Additional bundle from feature 2 install and start
        order.verify(context).installBundle(eq(bundleId3), any());

        order.verify(installed.get(bundleId3)).start();

        order.verifyNoMoreInteractions();

        Mockito.when(config.features()).thenReturn(new String[] { "bar" });
        fl.update(config);

        order.verify(installed.get(bundleId2)).stop();
        order.verify(installed.get(bundleId2)).uninstall();
        order.verifyNoMoreInteractions();

        fl.stop();

        order.verify(installed.get(bundleId)).stop();
        order.verify(installed.get(bundleId3)).stop();
        order.verify(installed.get(bundleId)).uninstall();
        order.verify(installed.get(bundleId3)).uninstall();
        order.verifyNoMoreInteractions();
    }

    @Nested
    class FeatureDependencies {

        @Test
        void testSingleDependencySatisfied() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "foo", "need_foo" });
            fl.start(context, config);

            assertTrue(failures.isEmpty(), () -> failures.toString());

            String bundleId = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);
            String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.1", null, null);
            String bundleId3 = getIdString(GROUP_ID, "buzz", "1.0.2-SNAPSHOT", null, null);

            InOrder order = Mockito.inOrder(context, installed.get(bundleId), installed.get(bundleId2),
                    installed.get(bundleId3));

            order.verify(context).installBundle(eq(bundleId), any());
            order.verify(context).installBundle(eq(bundleId2), any());

            order.verify(installed.get(bundleId)).start();
            order.verify(installed.get(bundleId2)).start();

            order.verify(context).installBundle(eq(bundleId3), any());

            order.verify(installed.get(bundleId3)).start();
        }

        @Test
        void testSingleDependencySatisfiedMavenCoords() throws Exception {
            Mockito.when(config.features())
                    .thenReturn(new String[] { "org.eclipse.sensinact.gateway.launcher.test:fizz:1.0.0", "need_fizz" });
            fl.start(context, config);

            assertTrue(failures.isEmpty(), () -> failures.toString());

            String bundleId = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);
            String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.1", null, null);

            InOrder order = Mockito.inOrder(context, installed.get(bundleId), installed.get(bundleId2));

            order.verify(context).installBundle(eq(bundleId), any());
            order.verify(installed.get(bundleId)).start();

            order.verify(context).installBundle(eq(bundleId2), any());
            order.verify(installed.get(bundleId2)).start();
        }

        @Test
        void testSingleDependencyNotSatisfied() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "need_foo" });

            ConfigurationException exception = assertThrows(ConfigurationException.class,
                    () -> fl.start(context, config));

            assertEquals("features", exception.getProperty());
        }

        @Test
        void testSingleDependencyWrongOrder() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "need_foo", "foo" });

            ConfigurationException exception = assertThrows(ConfigurationException.class,
                    () -> fl.start(context, config));

            assertEquals("features", exception.getProperty());
        }

        @Test
        void testDependencyWrongType() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "bad_depends_type" });

            ConfigurationException exception = assertThrows(ConfigurationException.class,
                    () -> fl.start(context, config));

            assertEquals("features", exception.getProperty());
        }

        @Test
        void testDependencyUnknownMandatoryExtension() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "unkown_extension" });

            ConfigurationException exception = assertThrows(ConfigurationException.class,
                    () -> fl.start(context, config));

            assertEquals("features", exception.getProperty());
        }
    }

    @Nested
    class FeatureConfiguration {
        @Test
        void testFillInVariables() throws Exception {
            final Map<String, Object> values = new HashMap<>();
            values.put("constant.str", "constant");
            values.put("constant.int", 1);
            values.put("var.missing", "${missing}");
            values.put("var.int", "${value.int}");
            values.put("var.combine", "${value.prefix}-${value.int}");
            values.put("var.semi-missing", "${value.prefix}-${missing}");

            fl.fillInVariables(values, Map.of("value.int", 42, "value.prefix", "hello"));

            assertEquals("constant", values.get("constant.str"));
            assertEquals(1, values.get("constant.int"));
            assertEquals("${missing}", values.get("var.missing"));
            assertEquals(42, values.get("var.int"));
            assertEquals("hello-42", values.get("var.combine"));
            assertEquals("hello-${missing}", values.get("var.semi-missing"));
        }

        @Test
        void testSimpleConfig() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "config_simple" });
            fl.start(context, config);

            assertTrue(failures.isEmpty(), () -> failures.toString());

            String bundleId = getIdString(GROUP_ID, "buzz", "1.0.2-SNAPSHOT", null, null);
            String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);

            InOrder order = Mockito.inOrder(manager, context, installed.get(bundleId), installed.get(bundleId2));

            // Check updated configuration and order
            final Map<String, Hashtable<String, Object>> expectedConfs = Map.of("test-A",
                    new Hashtable<>(Map.of("test", "A", "value", 42)), "test-merged",
                    new Hashtable<>(Map.of("test", "merged", "value", 0, "value-A", 5L)));
            order.verify(manager).updateConfigurations(argThat(new MapConfigArgumentMatcher(expectedConfs)), eq(null));

            order.verify(context).installBundle(eq(bundleId), any());
            order.verify(context).installBundle(eq(bundleId2), any());

            order.verify(installed.get(bundleId)).start();
            order.verify(installed.get(bundleId2)).start();
        }

        @Test
        void testConfigOnly() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "config_only" });
            fl.start(context, config);

            assertTrue(failures.isEmpty(), () -> failures.toString());

            // Check updated configuration and order
            Map<String, Hashtable<String, Object>> expectedConfs = Map.of("test-only1",
                    new Hashtable<>(Map.of("test", "A", "value", 21)), "test-fixed",
                    new Hashtable<>(Map.of("test", "B", "value", 42)));
            Mockito.verify(manager).updateConfigurations(argThat(new MapConfigArgumentMatcher(expectedConfs)), eq(null));
            Mockito.verify(context, Mockito.never()).installBundle(any());
        }

        @Test
        void testConfigUpdate() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "config_only" });
            fl.start(context, config);

            assertTrue(failures.isEmpty(), () -> failures.toString());

            // Check configuration
            Map<String, Hashtable<String, Object>> expectedConfs = Map.of("test-only1",
                    new Hashtable<>(Map.of("test", "A", "value", 21)), "test-fixed",
                    new Hashtable<>(Map.of("test", "B", "value", 42)));
            Mockito.verify(manager).updateConfigurations(argThat(new MapConfigArgumentMatcher(expectedConfs)), eq(null));
            Mockito.verify(context, Mockito.never()).installBundle(any());

            Mockito.when(config.features()).thenReturn(new String[] { "config_only_update" });
            fl.update(config);

            assertTrue(failures.isEmpty(), () -> failures.toString());

            // Check updated configuration and order
            InOrder order = Mockito.inOrder(manager);
            expectedConfs = Map.of("test-only2", new Hashtable<>(Map.of("test", "C", "value", 21)), "test-fixed",
                    new Hashtable<>(Map.of("test", "B", "value", 43)));
            order.verify(manager).updateConfigurations(eq(null), eq(Set.of("test-only1", "test-fixed")));
            order.verify(manager).updateConfigurations(argThat(new MapConfigArgumentMatcher(expectedConfs)), eq(null));
            Mockito.verify(context, Mockito.never()).installBundle(any());
        }

        @Test
        void testConfigWithVars() throws Exception {
            Mockito.when(config.features()).thenReturn(new String[] { "config_vars" });
            fl.start(context, config);

            assertTrue(failures.isEmpty(), () -> failures.toString());

            // Check updated configuration and order
            Map<String, Hashtable<String, Object>> expectedConfs = new HashMap<>(
                    Map.of("test-A", new Hashtable<>(Map.of("test", "A", "value", 42)), "test-vars", new Hashtable<>(
                            Map.of("test", "vars", "value", 42, "text", "Hello_World", "missing", "${missing}"))));
            Mockito.verify(manager).updateConfigurations(argThat(new MapConfigArgumentMatcher(expectedConfs)),
                    eq(null));
            Mockito.verify(context, Mockito.never()).installBundle(any());
        }

        @Test
        void testConfigOverride() throws Exception {
            InOrder order = Mockito.inOrder(manager);

            Mockito.when(config.features()).thenReturn(new String[] { "config_only", "config_only_2" });
            fl.start(context, config);

            assertTrue(failures.isEmpty(), () -> failures.toString());

            // First "config_only"
            Map<String, Hashtable<String, Object>> expectedConfs = Map.of("test-only1",
                    new Hashtable<>(Map.of("test", "A", "value", 21)), "test-fixed",
                    new Hashtable<>(Map.of("test", "B", "value", 42)));
            order.verify(manager).updateConfigurations(argThat(new MapConfigArgumentMatcher(expectedConfs)), eq(null));

            // Then "config_only_2"
            expectedConfs = Map.of("test-only2", new Hashtable<>(Map.of("test", "Only2", "value", 15)), "test-fixed",
                    new Hashtable<>(Map.of("test", "Override", "value", 451)));
            order.verify(manager).updateConfigurations(argThat(new MapConfigArgumentMatcher(expectedConfs)), eq(null));
        }

        @Test
        void testPathUserInjection() throws Exception {
            final Path targetPath = Paths.get(System.getProperty("user.home"), "test");
            assertEquals(targetPath, fl.getPath(targetPath.toString()));
            assertEquals(targetPath, fl.getPath("~/test"));
        }

        @Test
        void testPathVariablesInjections() throws Exception {
            final Path targetPath = Paths.get(System.getProperty("user.home"), "test");

            String homeEnv = null;
            for (String possibleHomeEnv : Arrays.asList("HOME", "USERPROFILE")) {
                if (System.getenv(possibleHomeEnv) != null) {
                    homeEnv = possibleHomeEnv;
                    break;
                }
            }

            assumeTrue(homeEnv != null, "No home environment variable found");
            assertEquals(targetPath, fl.getPath("${" + homeEnv + "}/test"));
        }
    }

    class MapConfigArgumentMatcher implements ArgumentMatcher<Map<String, Hashtable<String, Object>>> {
        final Map<String, Hashtable<String, Object>> expected;

        MapConfigArgumentMatcher(Map<String, Hashtable<String, Object>> expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Map<String, Hashtable<String, Object>> arg) {
            if (!arg.keySet().equals(expected.keySet())) {
                return false;
            }

            for (final String pid : arg.keySet()) {
                final Hashtable<String, Object> expectedValues = expected.get(pid);
                final Hashtable<String, Object> argValues = arg.get(pid);
                if (!argValues.keySet().equals(expectedValues.keySet())) {
                    System.out.println("Different keys");
                    return false;
                }

                for (final String argKey : argValues.keySet()) {
                    final Object expectedValue = expectedValues.get(argKey);
                    final Object argValue = argValues.get(argKey);

                    if (!Objects.equals(expectedValue, argValue)) {
                        if (expectedValue instanceof Number && argValue instanceof Number) {
                            // We don't use floats in the tests
                            if (((Number) expectedValue).longValue() == ((Number) argValue).longValue()) {
                                continue;
                            }
                        }
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return String.valueOf(this.expected);
        }
    }
}
