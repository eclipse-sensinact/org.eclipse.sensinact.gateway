/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.history.memory;

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.DtoFactory;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.UtilsAssert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class LocationHistoryMemoryTest extends AstractIntegrationTestHistoryMemory {

    /**
     * test delete historicalLocation
     */
    @Test
    public void testDeleteHistocalLocation() throws Exception {
        // given
        String name = "testDeleteHistocalLocation";
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedLocation dtoLocation = DtoFactory.getLocation(name + "1");

        json = getJsonResponseFromPost(dtoLocation, String.format("Things(%s)/Locations", idThing), 201);
        UtilsAssert.assertLocation(dtoLocation, json);
        json = getJsonResponseFromGet(String.format("Things(%s)/HistoricalLocations", idThing), 200);
        String idHistoricalLocation = getIdFromJsonValues(json, 0);
        getJsonResponseFromDelete(String.format("/HistoricalLocations(%s)", idHistoricalLocation), 200);

    }

}
