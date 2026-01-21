/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class ObservationTest extends AbstractIntegrationTest {

    /**
     * test create observation through datastream observation endpoint
     *
     * @throws Exception
     */
    @Test
    public void testCreateObservationThroughDatastream() throws Exception {
        // given
        String name = "createCreateObservationThroughDatastream";

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

        UtilsAssert.assertObservation(observsation, json);

    }

    /**
     * test observation with missing mandatory field
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
        String idObservation = getIdFromJson(json);
        UtilsAssert.assertObservation(observsation, json);
        // when
        getJsonResponseFromDelete(String.format("Observations(%s)", idObservation), 204);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, datastreamId, "datastream");
        ExpandedObservation lastObs = DtoMapperSimple.getResourceField(service, "lastObservation",
                ExpandedObservation.class);
        assertNull(lastObs);
    }

    /**
     * test create observation with missing required field
     */
    @Test
    public void testCreateObservationMissingField() throws Exception {
        // given
        String name = "createCreateObservationMissingField";

        ExpandedThing thing = DtoFactory.getExpandedThing(name + "alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(thingId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Sensor,Observations", 201);
        UtilsAssert.assertDatastream(datastream, json, true);
        String datastreamId = getIdFromJson(json);
        // result
        ExpandedObservation observsation = DtoFactory.getObservationLinkDatastream(name, null, Instant.now(), null,
                null);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 400);

        // phenomenomTime
        observsation = DtoFactory.getObservationLinkDatastream(name, 5.0, null, null, null);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 400);

    }

    /**
     * create observation using datastream endpoint
     *
     * @throws Exception
     */
    @Test
    public void testCreateObservationsInDatastream() throws Exception {
        // given
        String name = "createCreateObservationsInDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists2", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedObservation observsation1 = DtoFactory.getObservation(name + "1");
        ExpandedObservation observsation2 = DtoFactory.getObservation(name + "2");

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingWithObservations(name + "Datastream",
                DtoFactory.getRefId(thingId), List.of(observsation1, observsation2));

        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Observations,Sensor,ObservedProperty", 201);

        UtilsAssert.assertDatastream(datastream, json, true);

    }

    /**
     * create observation using datastream endpoint
     *
     * @throws Exception
     */
    @Test
    public void testDeleteFeatureOfInterestObservationsRef() throws Exception {
        // given
        String name = "testDeleteFeatureOfInterestObservationsRef";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists2", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedObservation observsation1 = DtoFactory.getObservation(name + "1");
        ExpandedObservation observsation2 = DtoFactory.getObservation(name + "2");

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingWithObservations(name + "Datastream",
                DtoFactory.getRefId(thingId), List.of(observsation1, observsation2));

        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Observations,Sensor,ObservedProperty", 201);
        JsonNode observationsNode = json.get("Observations");
        assertEquals(observationsNode.size(), 1);
        String idObservation = getIdFromJson(observationsNode.get(0));
        UtilsAssert.assertDatastream(datastream, json, true);
        // when
        getJsonResponseFromDelete(String.format("/Observations(%s)/FeatureOfInterest/$ref", idObservation), 409);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, DtoMapperSimple.extractFirstIdSegment(idObservation),
                "datastream");
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(), service);
        assertNotNull(obs);
        assertNotNull(obs.featureOfInterest());

    }

}
