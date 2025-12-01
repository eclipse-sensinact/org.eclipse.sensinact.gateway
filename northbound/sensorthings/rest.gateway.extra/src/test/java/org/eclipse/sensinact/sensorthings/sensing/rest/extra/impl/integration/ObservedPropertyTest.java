package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
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

        ExpandedObservedProperty observedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(observedProperty, "ObservedProperties", 201);
        // when

        UtilsAssert.assertObservedProperty(observedProperty, json);

        json = getJsonResponseFromGet(String.format("ObservedProperties(%s)", getIdFromJson(json)));
        UtilsAssert.assertObservedProperty(observedProperty, json);

    }

}
