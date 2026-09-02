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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.PaginationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkipFilterTest extends QueryOptionFilterTestSupport {

    private SkipFilter filter;

    @BeforeEach
    void setUpFilter() {
        filter = new SkipFilter();
    }

    @Test
    void noSkipParameterLeavesRequestAndResponseUntouched() throws IOException {
        filter.filter(requestContext);
        verify(requestContext, never()).setProperty(any(), any());

        ResultList<TestEntity> original = new ResultList<>(entities(5));
        setResponseEntity(original);
        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
    }

    @Test
    void skipDropsLeadingEntriesAndKeepsCountAndNextLink() throws IOException {
        queryParameters.putSingle("$skip", "3");
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(42, "https://example.org/next", entities(10)));
        filter.filter(requestContext, responseContext);

        ResultList<TestEntity> result = responseResultList();
        assertEquals(7, result.value().size());
        assertEquals("entity-3", result.value().get(0).id());
        assertEquals(42, result.count());
        assertEquals("https://example.org/next", result.nextLink());
    }

    @Test
    void skipLargerThanResultYieldsEmptyList() throws IOException {
        queryParameters.putSingle("$skip", "20");
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(entities(10)));
        filter.filter(requestContext, responseContext);

        assertTrue(responseResultList().value().isEmpty());
    }

    @Test
    void zeroSkipLeavesResponseUntouched() throws IOException {
        queryParameters.putSingle("$skip", "0");
        filter.filter(requestContext);

        ResultList<TestEntity> original = new ResultList<>(entities(10));
        setResponseEntity(original);
        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
    }

    @Test
    void negativeSkipIsRejected() throws IOException {
        queryParameters.putSingle("$skip", "-1");

        filter.filter(requestContext);

        assertAbortedWithBadRequest();
    }

    @Test
    void nonNumericSkipIsRejected() throws IOException {
        queryParameters.putSingle("$skip", "three");

        filter.filter(requestContext);

        assertAbortedWithBadRequest();
    }

    @Test
    void repeatedSkipParameterIsRejected() throws IOException {
        queryParameters.put("$skip", List.of("1", "2"));

        filter.filter(requestContext);

        assertAbortedWithBadRequest();
    }

    @Test
    void nonResultListEntityIsUntouched() throws IOException {
        queryParameters.putSingle("$skip", "3");
        filter.filter(requestContext);

        TestEntity single = entities(1).get(0);
        setResponseEntity(single);
        filter.filter(requestContext, responseContext);

        assertSame(single, responseEntity());
    }

    @Test
    void appliedPaginationLeavesResponseUntouched() throws IOException {
        queryParameters.putSingle("$skip", "3");
        filter.filter(requestContext);
        setRequestProperty(PaginationConstants.PAGINATION_APPLIED, Boolean.TRUE);

        ResultList<TestEntity> pushedDownPage = new ResultList<>(entities(10));
        setResponseEntity(pushedDownPage);
        filter.filter(requestContext, responseContext);

        assertSame(pushedDownPage, responseEntity());
    }
}
