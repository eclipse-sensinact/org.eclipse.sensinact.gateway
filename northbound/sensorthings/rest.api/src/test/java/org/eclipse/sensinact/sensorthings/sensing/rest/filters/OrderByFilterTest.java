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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.rest.PaginationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;

class OrderByFilterTest extends QueryOptionFilterTestSupport {

    public record RankedEntity(String selfLink, String id, Integer rank, String category) implements Self {
    }

    private OrderByFilter filter;

    @BeforeEach
    void setUpFilter() {
        filter = new OrderByFilter();
        filter.providers = providersWith(new ObjectMapper());
    }

    private static RankedEntity entity(String id, Integer rank, String category) {
        return new RankedEntity("https://example.org/RankedEntities(" + id + ")", id, rank, category);
    }

    private List<String> sortedIds(List<RankedEntity> input) throws IOException {
        setResponseEntity(new ResultList<>(input));
        filter.filter(requestContext, responseContext);
        @SuppressWarnings("unchecked")
        ResultList<RankedEntity> result = (ResultList<RankedEntity>) responseEntity();
        return result.value().stream().map(RankedEntity::id).toList();
    }

    @Test
    void missingOrderBySortsById() throws IOException {
        filter.filter(requestContext);

        List<String> ids = sortedIds(List.of(entity("c", 1, "x"), entity("a", 2, "x"), entity("b", 3, "x")));

        assertEquals(List.of("a", "b", "c"), ids);
    }

    @Test
    void singleKeyDefaultsToAscending() throws IOException {
        queryParameters.putSingle("$orderby", "rank");
        filter.filter(requestContext);

        List<String> ids = sortedIds(List.of(entity("a", 3, "x"), entity("b", 1, "x"), entity("c", 2, "x")));

        assertEquals(List.of("b", "c", "a"), ids);
    }

    @Test
    void descendingSortIsReversed() throws IOException {
        queryParameters.putSingle("$orderby", "rank desc");
        filter.filter(requestContext);

        List<String> ids = sortedIds(List.of(entity("a", 3, "x"), entity("b", 1, "x"), entity("c", 2, "x")));

        assertEquals(List.of("a", "c", "b"), ids);
    }

    @Test
    void multipleKeysSortHierarchically() throws IOException {
        queryParameters.putSingle("$orderby", "category asc, rank desc");
        filter.filter(requestContext);

        List<String> ids = sortedIds(
                List.of(entity("a", 1, "y"), entity("b", 2, "x"), entity("c", 3, "y"), entity("d", 1, "x")));

        assertEquals(List.of("b", "d", "c", "a"), ids);
    }

    @Test
    void nullValuesSortFirst() throws IOException {
        queryParameters.putSingle("$orderby", "rank");
        filter.filter(requestContext);

        List<String> ids = sortedIds(List.of(entity("a", 2, "x"), entity("b", null, "x"), entity("c", 1, "x")));

        assertEquals(List.of("b", "c", "a"), ids);
    }

    @Test
    void unknownPropertyFailsDuringSorting() throws IOException {
        queryParameters.putSingle("$orderby", "unknownField");
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(List.of(entity("a", 1, "x"), entity("b", 2, "x"))));

        assertThrows(BadRequestException.class, () -> filter.filter(requestContext, responseContext));
    }

    @Test
    void nestedPathOnRecordsFailsDuringSorting() throws IOException {
        queryParameters.putSingle("$orderby", "rank/nested");
        filter.filter(requestContext);

        setResponseEntity(new ResultList<>(List.of(entity("a", 1, "x"), entity("b", 2, "x"))));

        assertThrows(BadRequestException.class, () -> filter.filter(requestContext, responseContext));
    }

    @Test
    void singleEntityResponseIsUntouched() throws IOException {
        queryParameters.putSingle("$orderby", "rank");
        filter.filter(requestContext);

        RankedEntity single = entity("a", 1, "x");
        setResponseEntity(single);
        filter.filter(requestContext, responseContext);

        assertEquals(single, responseEntity());
    }

    @Test
    void appliedPaginationLeavesResponseUntouched() throws IOException {
        queryParameters.putSingle("$orderby", "rank desc");
        filter.filter(requestContext);
        setRequestProperty(PaginationConstants.PAGINATION_APPLIED, Boolean.TRUE);

        ResultList<RankedEntity> pushedDownPage = new ResultList<>(
                List.of(entity("a", 1, "x"), entity("b", 2, "x")));
        setResponseEntity(pushedDownPage);
        filter.filter(requestContext, responseContext);

        assertSame(pushedDownPage, responseEntity());
    }
}
