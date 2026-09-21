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
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.DataArray;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResultFormatFilterTest extends QueryOptionFilterTestSupport {

    private static final String REQUEST_URI = "https://example.org/v1.1/Observations?$resultFormat=dataArray";

    private ResultFormatFilter filter;

    @BeforeEach
    void setUpFilter() {
        filter = new ResultFormatFilter();
    }

    @Test
    void repeatedResultFormatParameterIsRejected() throws IOException {
        queryParameters.put("$resultFormat", List.of("dataArray", "dataArray"));

        filter.filter(requestContext);

        assertAbortedWithBadRequest();
    }

    @Test
    void missingResultFormatLeavesResponseUntouched() throws IOException {
        filter.filter(requestContext);

        ResultList<TestEntity> original = new ResultList<>(entities(3));
        setResponseEntity(original);
        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
    }

    @Test
    void unknownResultFormatLeavesResponseUntouched() throws IOException {
        queryParameters.putSingle("$resultFormat", "csv");
        filter.filter(requestContext);

        ResultList<TestEntity> original = new ResultList<>(entities(3));
        setResponseEntity(original);
        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
    }

    @Test
    void dataArrayFormatConvertsResultListToSingleDataArray() throws IOException {
        queryParameters.putSingle("$resultFormat", "dataArray");
        filter.filter(requestContext);
        when(uriInfo.getRequestUri()).thenReturn(URI.create(REQUEST_URI));

        setResponseEntity(new ResultList<>(42, "https://example.org/next", entities(2)));
        filter.filter(requestContext, responseContext);

        @SuppressWarnings("unchecked")
        ResultList<DataArray> result = (ResultList<DataArray>) responseEntity();
        assertEquals(42, result.count());
        assertEquals("https://example.org/next", result.nextLink());
        assertEquals(1, result.value().size());

        DataArray dataArray = result.value().get(0);
        assertEquals("Observation", dataArray.entityName());
        assertEquals(REQUEST_URI, dataArray.entityLink());
        assertEquals(List.of("@iot.selfLink", "@iot.id"), dataArray.components());
        assertEquals(2, dataArray.dataArray().size());
        assertEquals(List.of("https://example.org/TestEntities(0)", "entity-0", 0), dataArray.dataArray().get(0));
    }

    @Test
    void dataArrayFormatIsCaseInsensitive() throws IOException {
        queryParameters.putSingle("$resultFormat", "DATAARRAY");
        filter.filter(requestContext);
        when(uriInfo.getRequestUri()).thenReturn(URI.create(REQUEST_URI));

        setResponseEntity(new ResultList<>(entities(1)));
        filter.filter(requestContext, responseContext);

        ResultList<?> result = (ResultList<?>) responseEntity();
        assertEquals(1, result.value().size());
        assertEquals(DataArray.class, result.value().get(0).getClass());
    }

    @Test
    void emptyResultListIsUntouched() throws IOException {
        queryParameters.putSingle("$resultFormat", "dataArray");
        filter.filter(requestContext);

        ResultList<TestEntity> original = new ResultList<>(List.of());
        setResponseEntity(original);
        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
    }
}
