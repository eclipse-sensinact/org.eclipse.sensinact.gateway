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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.eclipse.sensinact.gateway.launcher.FeatureLauncher.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.feature.FeatureService;

@ExtendWith(MockitoExtension.class)
class FeatureLaunchingMultipleFoldersTest {

    private static final String GROUP_ID = "org.eclipse.sensinact.gateway.launcher.test";

    @Mock
    BundleContext context;

    @Mock
    Config config;

    @Mock
    ConfigurationManager manager;

    FeatureLauncher fl = new FeatureLauncher();

    private FeatureService fs;

    private Map<String, Bundle> installed = new HashMap<>();

    private Map<String, String> contents = new HashMap<>();

    @BeforeEach
    void start() throws Exception {
        ServiceLoader<FeatureService> loader = ServiceLoader.load(FeatureService.class);
        fs = loader.findFirst().get();

        Mockito.lenient().when(config.featureDir())
                .thenReturn(new String[] { "src/test/resources/features2", "src/test/resources/features" });
        Mockito.lenient().when(config.repository())
                .thenReturn(new String[] { "src/test/resources/repository2", "src/test/resources/repository" });

        Mockito.lenient().when(context.installBundle(Mockito.anyString(), Mockito.any())).thenAnswer(i -> {
            String fromIs;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(i.getArgument(1, InputStream.class)))) {
                fromIs = br.readLine();
            }

            String name = i.getArgument(0, String.class);
            contents.put(name, fromIs);

            Bundle mock = Mockito.mock(Bundle.class, fromIs);
            installed.put(name, mock);
            return mock;
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

    /**
     * Tests the loading of a feature available in both folders. One of the bundles
     * must be overridden.
     */
    @Test
    void testFeatureOverride() throws Exception {
        Mockito.when(config.features()).thenReturn(new String[] { "foo" });
        fl.start(context, config);

        String bundleId = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);
        String bundleIdContent = getIdString(GROUP_ID, "buzz-repo2", "1.0.0", null, null);
        String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.1", null, null);
        String bundleId3 = getIdString(GROUP_ID, "buzz", "1.0.2-SNAPSHOT", null, null);

        InOrder order = Mockito.inOrder(context, installed.get(bundleId), installed.get(bundleId2),
                installed.get(bundleId3));

        order.verify(context).installBundle(eq(bundleId), any());
        order.verify(context).installBundle(eq(bundleId2), any());
        order.verify(context).installBundle(eq(bundleId3), any());

        order.verify(installed.get(bundleId)).start();
        order.verify(installed.get(bundleId2)).start();
        order.verify(installed.get(bundleId3)).start();

        assertEquals(bundleIdContent, contents.get(bundleId));
        assertEquals(bundleId2, contents.get(bundleId2));
        assertEquals(bundleId3, contents.get(bundleId3));
    }

    /**
     * Tests a feature available in the second folder. One of the bundles must be
     * overridden.
     */
    @Test
    void testFeatureNoOverride() throws Exception {
        Mockito.when(config.features()).thenReturn(new String[] { "bar" });
        fl.start(context, config);

        String bundleId = getIdString(GROUP_ID, "buzz", "1.0.2-SNAPSHOT", null, null);
        String bundleId2 = getIdString(GROUP_ID, "buzz", "1.0.0", null, null);
        String bundleId2Content = getIdString(GROUP_ID, "buzz-repo2", "1.0.0", null, null);

        InOrder order = Mockito.inOrder(context, installed.get(bundleId), installed.get(bundleId2));

        order.verify(context).installBundle(eq(bundleId), any());
        order.verify(context).installBundle(eq(bundleId2), any());

        order.verify(installed.get(bundleId)).start();
        order.verify(installed.get(bundleId2)).start();

        assertEquals(bundleId, contents.get(bundleId));
        assertEquals(bundleId2Content, contents.get(bundleId2));
    }
}
