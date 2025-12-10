package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class ObservedPropertyTest extends AbstractIntegrationTest {

    @Test
    public void testCreateObservedProperty() throws Exception {
        // given
        String name = "testCreateObservedProperty";
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedPropertys", 201);

        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        json = getJsonResponseFromGet(String.format("ObservedPropertys(%s)", getIdFromJson(json)));
        UtilsAssert.assertObservedProperty(ObservedProperty, json);

    }

    @Test
    public void testCreateDatastreamLinkObservedProperty() throws Exception {
        // given
        String name = "testCreateDatastreamLinkObservedProperty";
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedPropertys", 201);
        String ObservedPropertyId = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(ObservedPropertyId));
        json = getJsonResponseFromPost(thing, "Datastreams?$expand=ObservedProperty", 201);
        UtilsAssert.assertDatastream(datastream, json, true);

    }
}
