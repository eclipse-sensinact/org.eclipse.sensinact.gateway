package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration.AbstractIntegrationTest.mapper;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class ThingTest extends AbstractIntegrationTest {

    // @formatter:off
    /* 
    POST /Things

    {
        "name": "testCreateThingExistingLocation",
        "description": "testThing existing Location",
        "properties": {
            "manufacturer": "New Corp",
            "installationDate": "2025-11-25"
        }, 
        "Locations": [
            { "@iot.id": "idLocation" }
        ]
    }
    */
    // @formatter:on

    @Test
    public void testCreateThingExistingLocation() throws Exception {
        // Given
        String name = "testCreateThingExistingLocation";
        ExpandedThing alreadyExists = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(alreadyExists, "/Things", 201);

        List<RefId> listId = new ArrayList<RefId>();
        listId.add(DtoFactory.getRefId(getIdFromJson(json)));
        ExpandedLocation location = DtoFactory.getLocationLinkThing(name, listId);
        HttpResponse<String> response = queryPost("/Locations", location);
        Location createdLocation = mapper.readValue(response.body(), Location.class);
        List<ExpandedLocation> idLocations = List.of(DtoFactory.getIdLocation(createdLocation.id()));
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations("testCreateThingExistingLocation",
                "testThing existing Location", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"),
                idLocations);

        // When

        json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);

    }

    // @formatter:off
    /* 
    POST /Things

    {
        "name": "testCreateThingExistingLocation",
        "description": "testThing existing Location",
        "properties": {
            "manufacturer": "New Corp",
            "installationDate": "2025-11-25"
        },
        "Datastreams": [
            { "@iot.id": "idLocation" }
        ]
    }
    */
    // @formatter:on

    @Test
    public void testCreateThingExistsDatastream() throws Exception {
        // Given
        String name = "testCreateThingExistsDatastream";
        ExpandedThing dtoExistsThingFromDataStream = DtoFactory.getExpandedThing("alreadyExists",
                "testThing existing Datastream ", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));

        // When
        JsonNode json = getJsonResponseFromPost(dtoExistsThingFromDataStream, "/Things", 201);
        String idExistsThing = getIdFromJson(json);
        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThing(name,
                DtoFactory.getRefId(idExistsThing));
        HttpResponse<String> response = queryPost("/Datastreams", datastream);
        ExpandedDataStream createdDatastream = mapper.readValue(response.body(), ExpandedDataStream.class);
        List<ExpandedDataStream> idDatastream = List.of(DtoFactory.getIdDatastream(createdDatastream.id()));
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreams(name, "testThing existing Datastream ",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), idDatastream);

        // When
        json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);

    }

    // @formatter:off
    /* 
     * e.g
    POST /Things

    {
        "name": "testExtraThing",
        "description": "testThing existing Location",
        "properties": {
            "manufacturer": "New Corp",
            "installationDate": "2025-11-25"
        }
    }
    */
    // @formatter:on

    @Test
    public void testCreateThingSimple() throws Exception {
        // Given
        String name = "testCreateThingSimple";

        ExpandedThing dtoThing = DtoFactory.getExpandedThing(name, "testThing",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        // When
        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);
        // Then
        UtilsAssert.assertThing(dtoThing, json);

    }

    // @formatter:off
    /* 
     * e.g
    POST /Things

    {
        "name": "testExtraThing",
        "description": "testThing existing Location",
        "properties": {
            "manufacturer": "New Corp",
            "installationDate": "2025-11-25"
        },
        Locations:[
            name:"location1",
            description:"",
            location:{
            ...
            },
            encodingType:""
            
        ]
    }
    */
    // @formatter:on

    @Test
    public void testCreateThingWith1Location() throws Exception {
        // Given
        String name = "testCreateThingWith1Location";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name));
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations(name, "testThing With 1 Location ",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);
        json = getJsonResponseFromGet(String.format("/Things(%s)?$expand=Locations", name));
        UtilsAssert.assertThing(dtoThing, json, true);
    }

    // @formatter:off
    /* 
     * e.g
    POST /Things

    {
        "name": "testExtraThing",
        "description": "testThing existing Location",
        "properties": {
            "manufacturer": "New Corp",
            "installationDate": "2025-11-25"
        },
        Locations:[
            {
                name:"location1",
                description:"",
                location:{
                ...
                },
                encodingType:""
            }
        ],
        Datastreams:[
            {
                ...
            }
        ]
    }
    */
    // @formatter:on

    @Test
    public void testCreateThingWithLocationAndDatastream() throws Exception {
        // Given
        String name = "testCreateThingWithLocationAndDatastream";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "1"));

        List<ExpandedDataStream> datastreams = List.of(DtoFactory.getDatastreamMinimal(name + "2"));

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);
        json = getJsonResponseFromGet(String.format("/Things(%s)?$expand=Locations,Datastreams", name));
        UtilsAssert.assertThing(dtoThing, json, true);

    }

    // @formatter:off
    /* 
     * e.g
    POST /Things

    {
        "name": "testExtraThing",
        "description": "testThing existing Location",
        "properties": {
            "manufacturer": "New Corp",
            "installationDate": "2025-11-25"
        },
        Locations:[
            {
                name:"location1",
                description:"",
                location:{
                ...
                },
                encodingType:""
            }
        ],
        Datastreams:[
            {
                ...,
                Sensor:{
                    ...
                },
                ObservedProperty:{...},
                Observations:[
                {...}
                ]
            }
        ]
    }
    */
    // @formatter:on
    public void testCreateThingWithLocationAndDatastreamIncludeSensorObservedPropertyhObservation() throws Exception {
        // Given
        String name = "testCreateThingWithLocationAndDatastreamIncludeSensorObservedPropertyhObservation";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name));

        ExpandedDataStream datastream1 = DtoFactory.getDatastreamMinimal(name);
        ExpandedDataStream datastream2 = DtoFactory.getDatastreamMinimal(name + "1");
        ExpandedDataStream datastream3 = DtoFactory.getDatastreamMinimal(name + "2");

        List<ExpandedDataStream> datastreams = List.of(datastream1, datastream2, datastream3);
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);
        json = getJsonResponseFromGet(String.format(
                "/Things(%s)?$expand=Locations,Datastreams($expand=Sensor,ObservedProperty,Observations)", name));
        UtilsAssert.assertThing(dtoThing, json, true);
    }

    // @formatter:off
    /* 
     * e.g
    POST /Things

    {
        "name": "testExtraThing",
        "description": "testThing existing Location",
        "properties": {
            "manufacturer": "New Corp",
            "installationDate": "2025-11-25"
        },
        Locations:[
            {
                name:"location1",
                description:"",
                location:{
                ...
                },
                encodingType:""
            }
        ],
        Datastreams:[
            {
                ...,
                Sensor:{
                    ...
                },
                ObservedProperty:{...},
                Observations:[
                {...}
                ]
            }
        ]
    }
    */
    // @formatter:on
    public void testCreateThingWithExpandLocationAndDatastreamIncludeSensorObservedPropertyhObservation()
            throws Exception {
        // Given
        String name = "testCreateThingWithExpandLocationAndDatastreamIncludeSensorObservedPropertyhObservation";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name));

        ExpandedDataStream datastream1 = DtoFactory.getDatastreamMinimal(name);
        ExpandedDataStream datastream2 = DtoFactory.getDatastreamMinimal(name + "1");
        ExpandedDataStream datastream3 = DtoFactory.getDatastreamMinimal(name + "2");

        List<ExpandedDataStream> datastreams = List.of(datastream1, datastream2, datastream3);
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing,
                "/Things?$expand=Locations,Datastreams($expand=Sensor,ObservedProperty,Observations)", 201);

        UtilsAssert.assertThing(dtoThing, json, true);

    }

 // @formatter:off
    /* 
     * e.g
    POST /Things

    {
        "name": "testExtraThing",
        "description": "testThing existing Location",
        "properties": {
            "manufacturer": "New Corp",
            "installationDate": "2025-11-25"
        },
        Locations:[
            {...},
            {
                name:"location1",
                description:"",
                location:{
                ...
                },
                encodingType:""
            }
        ],
       
    }
    */
    // @formatter:on
    @Test
    public void testCreateThingWithMultipleLocation() throws Exception {
        // Given
        String name = "testCreateThingWithMultipleLocation";

        ExpandedLocation location1 = DtoFactory.getLocation(name + "1");
        ExpandedLocation location2 = DtoFactory.getLocation(name + "2");
        List<ExpandedLocation> locations = List.of(location1, location2);
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Locations", 201);

        UtilsAssert.assertThing(dtoThing, json, true);

    }

}
