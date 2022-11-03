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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.sensinact.gateway.launcher.FeatureLauncher.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.feature.FeatureService;

@ExtendWith(MockitoExtension.class)
class FeatureLaunchingTest {

    private static final String GROUP_ID = "org.eclipse.sensinact.gateway.launcher.test";

    @Mock
    BundleContext context;

    @Mock
    Config config;

    FeatureLauncher fl = new FeatureLauncher();

    private FeatureService fs;

    private List<Throwable> failures = new ArrayList<>();

    private Map<String, Bundle> installed = new HashMap<>();

    @BeforeEach
    void start() throws Exception {
        ServiceLoader<FeatureService> loader = ServiceLoader.load(FeatureService.class);
        fs = loader.findFirst().get();

        Mockito.when(config.featureDir()).thenReturn("src/test/resources/features");
        Mockito.when(config.repository()).thenReturn("src/test/resources/repository");

        Mockito.when(context.installBundle(Mockito.anyString(), Mockito.any())).thenAnswer(i -> {
            String fromIs;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(i.getArgument(1, InputStream.class)))) {
                fromIs = br.readLine();
            }
            try {
                assertEquals(i.getArgument(0, String.class), fromIs);
            } catch (AssertionFailedError e) {
                failures.add(e);
            }
            Bundle mock = Mockito.mock(Bundle.class, fromIs);
            installed.put(fromIs, mock);
            return mock;
        });

        fl.featureService = fs;

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
            String bundleClassifier) throws BundleException {

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
    void installFeatureSimpleName() throws BundleException {
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
    void cleanupOnStop() throws BundleException {
        installFeatureSimpleName();

        fl.stop();

        Bundle bundle = installed.get(getIdString(GROUP_ID, "buzz", "1.0.0", null, null));
        Bundle bundle2 = installed.get(getIdString(GROUP_ID, "buzz", "1.0.1", null, null));

        InOrder order = Mockito.inOrder(context, bundle, bundle2);

        order.verify(bundle2).stop();
        order.verify(bundle).stop();
        order.verify(bundle2).uninstall();
        order.verify(bundle).uninstall();
    }

    @Test
    void installFeatureListedTwice() throws BundleException {
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
    void installOverlappingFeatures() throws BundleException {
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
    void updateThatRemovesOverlappingFeatures() throws BundleException {

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
}
