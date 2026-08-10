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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.UriBuilder;

class TopFilterTest extends QueryOptionFilterTestSupport {

    static class PlainResource {
        public void listEntities() {
        }

        @PaginationLimit(3)
        public void listEntitiesPaginated() {
        }
    }

    private TopFilter filter;

    @BeforeEach
    void setUpFilter() throws Exception {
        filter = new TopFilter();
        filter.resourceInfo = mock(ResourceInfo.class);
        useResourceMethod("listEntities");
    }

    private void useResourceMethod(String name) throws Exception {
        Method method = PlainResource.class.getMethod(name);
        when(filter.resourceInfo.getResourceMethod()).thenReturn(method);
    }

    private UriBuilder mockRequestUriBuilder(String resultUri) {
        UriBuilder uriBuilder = mock(UriBuilder.class);
        when(uriInfo.getRequestUriBuilder()).thenReturn(uriBuilder);
        when(uriBuilder.replaceQueryParam(any(), any())).thenReturn(uriBuilder);
        when(uriBuilder.build()).thenReturn(URI.create(resultUri));
        return uriBuilder;
    }

    @Test
    void noTopParameterAndNoLimitLeavesRequestUntouched() throws IOException {
        filter.filter(requestContext);

        verify(requestContext, never()).setProperty(any(), any());
        verify(requestContext, never()).abortWith(any());
    }

    @Test
    void missingTopParameterFallsBackToPaginationLimit() throws Exception {
        useResourceMethod("listEntitiesPaginated");

        filter.filter(requestContext);

        ResultList<TestEntity> tenEntities = new ResultList<>(entities(10));
        setResponseEntity(tenEntities);
        mockRequestUriBuilder("https://example.org/TestEntities?%24skip=3");
        filter.filter(requestContext, responseContext);

        assertEquals(3, responseResultList().value().size());
    }

    @Test
    void topParameterIsCappedByPaginationLimit() throws Exception {
        useResourceMethod("listEntitiesPaginated");
        queryParameters.putSingle("$top", "100");

        filter.filter(requestContext);

        ResultList<TestEntity> tenEntities = new ResultList<>(entities(10));
        setResponseEntity(tenEntities);
        mockRequestUriBuilder("https://example.org/TestEntities?%24skip=3");
        filter.filter(requestContext, responseContext);

        assertEquals(3, responseResultList().value().size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "five", "0", "-5" })
    void invalidValueIsRejected(String topValue) throws IOException {
        queryParameters.putSingle("$top", topValue);

        filter.filter(requestContext);

        assertAbortedWithBadRequest();
    }

    @Test
    void repeatedTopParameterIsRejected() throws IOException {
        queryParameters.put("$top", List.of("5", "10"));

        filter.filter(requestContext);

        assertAbortedWithBadRequest();
    }

    @Test
    void topSmallerThanResultTruncatesAndAddsNextLink() throws IOException {
        queryParameters.putSingle("$top", "4");
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(42, entities(10)));
        UriBuilder uriBuilder = mockRequestUriBuilder("https://example.org/TestEntities?%24skip=4");
        filter.filter(requestContext, responseContext);

        ResultList<TestEntity> result = responseResultList();
        assertEquals(4, result.value().size());
        assertEquals("entity-0", result.value().get(0).id());
        assertEquals(42, result.count());
        assertEquals("https://example.org/TestEntities?%24skip=4", result.nextLink());
        verify(uriBuilder).replaceQueryParam("$skip", 4);
    }

    @Test
    void nextLinkSkipIsPreviousSkipPlusTop() throws IOException {
        queryParameters.putSingle("$top", "4");
        filter.filter(requestContext);
        setRequestProperty(SkipFilter.SKIP_PROP, 6);

        setResponseEntity(new ResultList<>(entities(10)));
        UriBuilder uriBuilder = mockRequestUriBuilder("https://example.org/TestEntities?%24skip=10");
        filter.filter(requestContext, responseContext);

        verify(uriBuilder).replaceQueryParam("$skip", 10);
    }

    @Test
    void topCoveringAllResultsProducesNoNextLink() throws IOException {
        queryParameters.putSingle("$top", "10");
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(entities(10)));
        filter.filter(requestContext, responseContext);

        ResultList<TestEntity> result = responseResultList();
        assertEquals(10, result.value().size());
        assertNull(result.nextLink());
    }

    @Test
    void responseWithoutTopPropertyIsUntouched() throws IOException {
        ResultList<TestEntity> original = new ResultList<>(entities(10));
        setResponseEntity(original);

        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
    }

    @Test
    void nonResultListEntityIsUntouched() throws IOException {
        queryParameters.putSingle("$top", "4");
        filter.filter(requestContext);

        TestEntity single = entities(1).get(0);
        setResponseEntity(single);
        filter.filter(requestContext, responseContext);

        assertSame(single, responseEntity());
    }
}
