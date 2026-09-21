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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class SelectFilterTest extends QueryOptionFilterTestSupport {

    private SelectFilter filter;

    @BeforeEach
    void setUpFilter() {
        filter = new SelectFilter();
        filter.providers = providersWith(new ObjectMapper());
    }

    @Test
    void missingSelectLeavesEntityUntouched() throws IOException {
        ResultList<TestEntity> original = new ResultList<>(entities(2));
        setResponseEntity(original);

        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
    }

    @Test
    void selectRetainsOnlyRequestedFieldsInListValues() throws IOException {
        queryParameters.putSingle("$select", "id");
        setResponseEntity(new ResultList<>(42, entities(2)));

        filter.filter(requestContext, responseContext);

        JsonNode json = (JsonNode) responseEntity();
        assertEquals(42, json.get("@iot.count").asInt());
        for (JsonNode value : json.get("value")) {
            assertEquals(List.of("id"), List.copyOf(value.propertyNames()));
        }
    }

    @Test
    void commaSeparatedSelectRetainsAllListedFields() throws IOException {
        queryParameters.putSingle("$select", "id,rank");
        setResponseEntity(new ResultList<>(entities(1)));

        filter.filter(requestContext, responseContext);

        JsonNode value = ((JsonNode) responseEntity()).get("value").get(0);
        assertTrue(value.has("id"));
        assertTrue(value.has("rank"));
        assertEquals(2, value.size());
    }

    @Test
    void selectOnSingleEntityRetainsOnlyRequestedFields() throws IOException {
        queryParameters.putSingle("$select", "rank");
        setResponseEntity(entities(1).get(0));

        filter.filter(requestContext, responseContext);

        JsonNode json = (JsonNode) responseEntity();
        assertEquals(List.of("rank"), List.copyOf(json.propertyNames()));
    }

    @Test
    void selectingUnknownFieldYieldsEmptyObjects() throws IOException {
        queryParameters.putSingle("$select", "doesNotExist");
        setResponseEntity(new ResultList<>(entities(1)));

        filter.filter(requestContext, responseContext);

        JsonNode value = ((JsonNode) responseEntity()).get("value").get(0);
        assertEquals(0, value.size());
    }
}
