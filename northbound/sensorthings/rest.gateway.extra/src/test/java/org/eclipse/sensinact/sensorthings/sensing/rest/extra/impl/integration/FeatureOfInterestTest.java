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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class FeatureOfInterestTest extends AbstractIntegrationTest {

    @Test
    public void testCreateFeatureOfInterest() throws Exception {
        // given
        String name = "testCreateFeatureOfInterest";

        FeatureOfInterest dtoFeatureOfInterest = DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749));

        // when
        JsonNode json = getJsonResponseFromPost(dtoFeatureOfInterest, "FeaturesOfInterest", 201);
        UtilsAssert.assertFeatureOfInterest(dtoFeatureOfInterest, json);

    }

    @Test
    public void testCreateFeatureOfInterestLinkObservation() throws Exception {
        // given
        String name = "testCreateFeatureOfInterest";

        FeatureOfInterest dtoFeatureOfInterest = DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749));

        // when
        JsonNode json = getJsonResponseFromPost(dtoFeatureOfInterest, "FeaturesOfInterest", 201);
        UtilsAssert.assertFeatureOfInterest(dtoFeatureOfInterest, json);
        String foiId = getIdFromJson(json);
        assertNotNull(foiUseCase.getInMemoryFeatureOfInterest(foiId));
        // create datastream with observation
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(idThing));

        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);

        UtilsAssert.assertDatastream(dtoDatastream, json);
        // when
        ExpandedObservation dtoObservation = DtoFactory.getObservationLinkFeatureOfInterest(name + "2", foiId);
        json = getJsonResponseFromPost(dtoObservation,
                String.format("Datastreams(%s)/Observations?$expand=FeatureOfInterest", idDatastream), 201);

        UtilsAssert.assertObservation(dtoObservation, json);
        assertNull(foiUseCase.getInMemoryFeatureOfInterest(foiId));

    }

    @Test
    public void testCreateFeatureOfInterestAndLinkitWithObservation() throws Exception {
        // given
        String name = "testCreateFeatureOfInterestAndLinkitWithObservation";
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(idThing));

        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);

        UtilsAssert.assertDatastream(dtoDatastream, json);
        // when
        ExpandedObservation dtoObservation = DtoFactory.getObservationWithFeatureOfInterest(name + "2",
                DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json", new Point(-122.4194, 37.7749)));
        json = getJsonResponseFromPost(dtoObservation,
                String.format("Datastreams(%s)/Observations?$expand=FeatureOfInterest", idDatastream), 201);

        UtilsAssert.assertObservation(dtoObservation, json, true);

    }

    @Test
    public void testCreateObservationWithFeatureOfInterestThroughDatastream() throws Exception {
        // given
        String name = "createCreateObservationsWithFeatureOfInterestThroughDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists2", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedObservation observsation1 = DtoFactory.getObservationWithFeatureOfInterest(name + "1",
                DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json", new Point(-122.4194, 37.7749)));

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingWithObservations(name + "Datastream",
                DtoFactory.getRefId(thingId), List.of(observsation1));

        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 201);

        UtilsAssert.assertDatastream(datastream, json, true);

    }

    public void createFeatureOfInterestMissingField() throws Exception {
        String name = "createCreateObservationsWithFeatureOfInterestThroughDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists2", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        // name
        ExpandedObservation observsation1 = DtoFactory.getObservationWithFeatureOfInterest(name + "1",
                DtoFactory.getFeatureOfInterest(null, "application/vnd.geo+json", new Point(-122.4194, 37.7749)));
        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingWithObservations(name + "Datastream",
                DtoFactory.getRefId(thingId), List.of(observsation1));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 400);

        // encodingType
        observsation1 = DtoFactory.getObservationWithFeatureOfInterest(name + "1",
                DtoFactory.getFeatureOfInterest("test", null, new Point(-122.4194, 37.7749)));
        datastream = DtoFactory.getDatastreamMinimalLinkThingWithObservations(name + "Datastream",
                DtoFactory.getRefId(thingId), List.of(observsation1));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 400);

        // feature
        observsation1 = DtoFactory.getObservationWithFeatureOfInterest(name + "1",
                DtoFactory.getFeatureOfInterest("test", "application/vnd.geo+json", null));
        datastream = DtoFactory.getDatastreamMinimalLinkThingWithObservations(name + "Datastream",
                DtoFactory.getRefId(thingId), List.of(observsation1));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 400);

        UtilsAssert.assertDatastream(datastream, json, true);
    }

    @Test
    public void testUpdateFeatureOfInterest() throws Exception {
        // given
        String name = "testUpdateFeatureOfInterest";

        FeatureOfInterest dtoFeatureOfInterest = DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749));

        JsonNode json = getJsonResponseFromPost(dtoFeatureOfInterest, "FeaturesOfInterest", 201);
        UtilsAssert.assertFeatureOfInterest(dtoFeatureOfInterest, json);

        // when
        FeatureOfInterest dtoFeatureOfInterestUpdate = DtoFactory.getFeatureOfInterest(name + "Update",
                "application/vnd.geo+json", new Point(-122.4194, 37.7749));

        json = getJsonResponseFromPut(dtoFeatureOfInterestUpdate, "FeaturesOfInterest", 204);

    }

    @Test
    public void testUpdateFeatureOfInterestLinkObservationRef() throws Exception {
        // given
        String name = "testUpdateFeatureOfInterestLinkObservationRef";

        FeatureOfInterest dtoFeatureOfInterest = DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749));

        // when
        JsonNode json = getJsonResponseFromPost(dtoFeatureOfInterest, "FeaturesOfInterest", 201);
        UtilsAssert.assertFeatureOfInterest(dtoFeatureOfInterest, json);
        String foiId = getIdFromJson(json);
        assertNotNull(foiUseCase.getInMemoryFeatureOfInterest(foiId));
        // create datastream with observation
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(idThing));

        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);
        UtilsAssert.assertDatastream(dtoDatastream, json);
        ExpandedObservation dtoObservation = DtoFactory.getObservationLinkFeatureOfInterest(name + "2", foiId);
        json = getJsonResponseFromPost(dtoObservation,
                String.format("Datastreams(%s)/Observations?$expand=FeatureOfInterest", idDatastream), 201);
        UtilsAssert.assertObservation(dtoObservation, json);
        assertNull(foiUseCase.getInMemoryFeatureOfInterest(foiId));
        String idObservation = getIdFromJson(json);
        // when
        FeatureOfInterest foiUpdate = DtoFactory.getFeatureOfInterest(name + "Update", "application/vnd.geo+json",
                new Point(-22.4194, 47.7749));
        json = getJsonResponseFromPost(foiUpdate, "FeaturesOfInterest", 201);
        String idFoiUpdate = getIdFromJson(json);

        json = getJsonResponseFromPut(new RefId(idFoiUpdate),
                String.format("Observations(%s)/FeatureOfInterest/$ref", idObservation), 204);
        // then
    }

}
