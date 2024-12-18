/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - Initial contribution
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.core.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.net.URL;

import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.ThingManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LoaderTest {

    @Test
    void testLoader() throws Exception {
        // Mock the configuration
        final URL targetUrl = getClass().getResource("/data/test.td.jsonld");
        assertNotNull(targetUrl, "Test file not found");

        final LoaderConfiguration mockConfig = Mockito.mock(LoaderConfiguration.class);
        Mockito.when(mockConfig.uri()).thenReturn(targetUrl.toString());

        // Mock the manager
        final String providerName = "test-provider";
        final ThingManager mockManager = Mockito.mock(ThingManager.class);
        Mockito.when(mockManager.registerThing(any(Thing.class))).then(args -> {
            final Thing t = args.getArgument(0, Thing.class);
            assertEquals("urn:name:lamp", t.id);
            assertEquals("MyLampThing", t.title);
            assertEquals("Thing Description for a Lamp thing", t.description);
            return providerName;
        });
        Mockito.when(mockManager.unregisterThing(anyString())).then(args -> {
            final String givenName = args.getArgument(0, String.class);
            return providerName.equals(givenName);
        });

        // Setup the loader
        final URIThingLoader loader = new URIThingLoader();
        loader.manager = mockManager;

        // Activate
        loader.activate(mockConfig);
        verify(mockManager, timeout(1000).times(1)).registerThing(any(Thing.class));
        verify(mockManager, never()).unregisterThing(anyString());

        // Deactivate
        reset(mockManager);
        loader.deactivate();
        verify(mockManager, timeout(1000).times(1)).unregisterThing(anyString());
        verify(mockManager, never()).registerThing(any(Thing.class));
    }
}
