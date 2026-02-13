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

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.DtoFactory;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.UtilsAssert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class ObservationHistoryMemoryTest extends AstractIntegrationTestHistoryMemory {

    /**
     * test create observation through datastream observation endpoint
     *
     * @throws Exception
     */
    @Test
    public void testDeleteObservation() throws Exception {
        // given
        String name = "testDeleteObservation";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(thingId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Sensor,Observations", 201);
        UtilsAssert.assertDatastream(datastream, json, true);
        String datastreamId = getIdFromJson(json);
        ExpandedObservation observsation = DtoFactory.getObservation(name);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 201);
        String obsId = getIdFromJson(json);
        observsation = DtoFactory.getObservation(name);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 201);

        UtilsAssert.assertObservation(observsation, json);
        json = getJsonResponseFromDelete(String.format("Observations(%s)", obsId), 200);

    }
}
