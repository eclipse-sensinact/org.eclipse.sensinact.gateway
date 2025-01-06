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

package org.eclipse.sensinact.gateway.southbound.wot.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.constants.WoTConstants;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.ThingManager;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class WoTManagerIntegrationTest {

    @InjectService
    GatewayThread thread;

    @InjectService
    DataUpdate push;

    @InjectService
    ThingManager wotManager;

    /**
     * The object mapper
     */
    final ObjectMapper mapper = JsonMapper.builder().build();

    /**
     * Reads a file from the test folder
     *
     * @param filename File name
     * @return File content as string
     * @throws Exception Error reading file
     */
    String readFile(final String filename) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/" + filename)) {
            if (inputStream == null) {
                throw new FileNotFoundException(filename);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void testProviderRegister() throws Exception {
        final String rawContent = readFile("test.td.jsonld");
        final Thing thing = mapper.readValue(rawContent, Thing.class);
        final String providerName = wotManager.registerThing(thing);
        assertNotNull(providerName);
        assertEquals("wot_MyLampThing", providerName);

        thread.execute(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                try {
                    final SensinactProvider provider = twin.getProvider(providerName);
                    final SensinactService wotSvc = provider.getServices().get(WoTConstants.WOT_SERVICE);
                    assertNotNull(wotSvc);

                    SensinactResource rc = wotSvc.getResources().get("status");
                    assertNotNull(rc);
                    assertEquals(ResourceType.SENSOR, rc.getResourceType());
                    assertEquals(String.class, rc.getType());
                    assertEquals("Lamp status", rc.getMetadataValue("description").getValue().getValue());

                    rc = wotSvc.getResources().get("toggle");
                    assertNotNull(rc);
                    assertEquals(ResourceType.ACTION, rc.getResourceType());
                    assertEquals(List.of(), rc.getArguments());
                    assertEquals(Boolean.class, rc.getType());

                    rc = wotSvc.getResources().get("setHue");
                    assertNotNull(rc);
                    assertEquals(ResourceType.ACTION, rc.getResourceType());
                    assertEquals("Sets light hue", rc.getMetadataValue("description").getValue().getValue());
                    assertEquals(
                            List.of(Map.entry("r", Long.class), Map.entry("g", Long.class), Map.entry("b", Long.class)),
                            rc.getArguments());
                    assertEquals(List.class, rc.getType());
                    return pf.resolved(null);
                } catch (Throwable e) {
                    return pf.failed(e);
                }
            }
        }).getValue();

        assertTrue(wotManager.unregisterThing(providerName));
        assertTrue(thread.execute(new AbstractTwinCommand<Boolean>() {
            @Override
            protected Promise<Boolean> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                return pf.resolved(twin.getProvider(providerName) == null);
            }
        }).getValue(), "Provider not removed");

        // 2nd removal must softly fail
        assertFalse(wotManager.unregisterThing(providerName));
    }

    @Test
    void testNoRemovalUnmanagedProvider() throws Exception {
        final GenericDto dto = new GenericDto();
        dto.provider = "toto";
        dto.service = "titi";
        dto.resource = "tutu";
        dto.value = 12;
        dto.type = Integer.class;
        push.pushUpdate(dto).getValue();
        assertFalse(wotManager.unregisterThing(dto.provider));
        assertTrue(thread.execute(new AbstractTwinCommand<Boolean>() {
            @Override
            protected Promise<Boolean> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                try {
                    final SensinactProvider provider = twin.getProvider(dto.provider);
                    return pf.resolved(provider != null);
                } catch (Exception e) {
                    return pf.failed(e);
                }
            }
        }).getValue());
    }
}
