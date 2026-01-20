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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class FeatureOfInterestTest extends AbstractIntegrationTest {

    /**
     * test simple feature of interest in cache memory as not link to datastream
     *
     * @throws Exception
     */
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

    /**
     * test create feature of interest link between observation using observation
     * feature of interest endpoint
     *
     * @throws Exception
     */
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
        assertNotNull(foiCache.getDto(foiId));
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
        assertNull(foiCache.getDto(foiId));

    }

    /**
     * test create feature of interest in observation create endpoint (foi is
     * inline)
     *
     * @throws Exception
     */
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

    /**
     * test create feature of interest using the observation/featureofinterest
     * endpoint. the foi should be link to the observation and datastream
     *
     * @throws Exception
     */
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

        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Sensor,Observations", 201);

        UtilsAssert.assertDatastream(datastream, json, true);

    }

    /**
     * test create feature of interest missing field
     *
     * @throws Exception
     */
    public void testCreateFeatureOfInterestMissingField() throws Exception {
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

    /**
     * Tests that <code>PUT</code> can be used to update a FeatureOfInterest
     */
    /**
     * test update of feature of interest that are store in cache memory as not link
     * to an observation
     *
     * @throws Exception
     */
    @Test
    public void testUpdateFeatureOfInterest() throws Exception {
        // given
        String name = "testUpdateFeatureOfInterest";

        FeatureOfInterest dtoFeatureOfInterest = DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749));

        JsonNode json = getJsonResponseFromPost(dtoFeatureOfInterest, "FeaturesOfInterest", 201);
        String idFoi = getIdFromJson(json);
        UtilsAssert.assertFeatureOfInterest(dtoFeatureOfInterest, json);

        // when
        FeatureOfInterest dtoFeatureOfInterestUpdate = DtoFactory.getFeatureOfInterest(name + "Update",
                "application/vnd.geo+json", new Point(-122.4194, 37.7749));

        json = getJsonResponseFromPut(dtoFeatureOfInterestUpdate, String.format("FeaturesOfInterest(%s)", idFoi), 204);
        // then
        assertEquals(name + "Update", foiCache.getDto(idFoi).name());

    }

    /**
     * Tests that a observation can use id references to refer to existing feature
     * of interest resources and will be linked to them
     */
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
        assertNotNull(foiCache.getDto(foiId));
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
        assertNull(foiCache.getDto(foiId));
        String idObservation = getIdFromJson(json);
        // when
        FeatureOfInterest foiUpdate = DtoFactory.getFeatureOfInterest(name + "Update", "application/vnd.geo+json",
                new Point(-22.4194, 47.7749));
        json = getJsonResponseFromPost(foiUpdate, "FeaturesOfInterest", 201);
        String idFoiUpdate = getIdFromJson(json);

        json = getJsonResponseFromPut(new RefId(idFoiUpdate),
                String.format("Observations(%s)/FeatureOfInterest/$ref", idObservation), 204);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        ExpandedObservation obs = DtoMapperSimple.getObservationFromService(getMapper(), service);
        assertEquals(idFoiUpdate, obs.featureOfInterest().id());

    }

    /**
     * Tests that <code>PATCH</code> can be used to update a FeatureOfInterest
     */
    @Test
    public void testPatchFeatureOfInterest() throws Exception {
        // given
        String name = "testPatchFeatureOfInterest";

        FeatureOfInterest dtoFeatureOfInterest = DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749));

        JsonNode json = getJsonResponseFromPost(dtoFeatureOfInterest, "FeaturesOfInterest", 201);
        String idFoi = getIdFromJson(json);
        UtilsAssert.assertFeatureOfInterest(dtoFeatureOfInterest, json);

        // when
        FeatureOfInterest dtoFeatureOfInterestUpdate = DtoFactory.getFeatureOfInterest(name + "Update",
                "application/vnd.geo+json", new Point(-122.4194, 37.7749));

        json = getJsonResponseFromPatch(dtoFeatureOfInterestUpdate, String.format("FeaturesOfInterest(%s)", idFoi),
                204);
        // then
        assertEquals(name + "Update", foiCache.getDto(idFoi).name());

    }

    /**
     * Tests that <code>DELETE</code> can be used to update a FeatureOfInterest
     */

    @Test
    public void testDeleteInMemoryFeatureOfInterest() throws Exception {
        // given
        String name = "testCreateFeatureOfInterest";

        FeatureOfInterest dtoFeatureOfInterest = DtoFactory.getFeatureOfInterest(name, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749));

        JsonNode json = getJsonResponseFromPost(dtoFeatureOfInterest, "FeaturesOfInterest", 201);

        String foiId = getIdFromJson(json);
        UtilsAssert.assertFeatureOfInterest(dtoFeatureOfInterest, json);
        // when
        getJsonResponseFromDelete(String.format("FeaturesOfInterest(%s)", foiId), 204);
        // then
        assertNull(foiCache.getDto(foiId));

    }

}
