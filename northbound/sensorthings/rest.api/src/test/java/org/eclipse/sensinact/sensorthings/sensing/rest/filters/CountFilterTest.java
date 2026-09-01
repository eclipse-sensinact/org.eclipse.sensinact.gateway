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

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CountFilterTest extends QueryOptionFilterTestSupport {

    private CountFilter filter;

    @BeforeEach
    void setUpFilter() {
        filter = new CountFilter();
    }

    @Test
    void countTrueAddsCountOfMaterializedList() throws IOException {
        queryParameters.putSingle("$count", "true");
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(entities(7)));
        filter.filter(requestContext, responseContext);

        assertEquals(7, responseResultList().count());
    }

    @Test
    void countTrueKeepsAlreadyPresentCount() throws IOException {
        queryParameters.putSingle("$count", "true");
        filter.filter(requestContext);

        ResultList<TestEntity> original = new ResultList<>(42, entities(7));
        setResponseEntity(original);
        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
        assertEquals(42, responseResultList().count());
    }

    @Test
    void countFalseRemovesPresentCount() throws IOException {
        queryParameters.putSingle("$count", "false");
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(42, "https://example.org/next", entities(7)));
        filter.filter(requestContext, responseContext);

        ResultList<TestEntity> result = responseResultList();
        assertNull(result.count());
        assertEquals("https://example.org/next", result.nextLink());
        assertEquals(7, result.value().size());
    }

    @Test
    void missingCountParameterRemovesPresentCount() throws IOException {
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(42, entities(7)));
        filter.filter(requestContext, responseContext);

        assertNull(responseResultList().count());
    }

    @Test
    void invalidCountValueIsRejected() throws IOException {
        queryParameters.putSingle("$count", "yes");

        filter.filter(requestContext);

        assertAbortedWithBadRequest();
    }

    @Test
    void repeatedCountParameterIsRejected() throws IOException {
        queryParameters.put("$count", List.of("true", "false"));

        filter.filter(requestContext);

        assertAbortedWithBadRequest();
    }

    @Test
    void nonResultListEntityIsUntouched() throws IOException {
        queryParameters.putSingle("$count", "true");
        filter.filter(requestContext);

        TestEntity single = entities(1).get(0);
        setResponseEntity(single);
        filter.filter(requestContext, responseContext);

        assertSame(single, responseEntity());
    }
}
