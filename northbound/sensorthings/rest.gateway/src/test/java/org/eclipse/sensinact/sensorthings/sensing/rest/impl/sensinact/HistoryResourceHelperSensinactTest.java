/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
*   Data In Motion - rework onto the HistoryProvider service
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.northbound.filters.sensorthings.impl.SensorthingsFilterComponent;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.PaginationConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.databind.ObjectMapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@ExtendWith(MockitoExtension.class)
class HistoryResourceHelperSensinactTest {

    private static final ResourcePath PATH = new ResourcePath("testProvider", "testService", "testResource");

    @Mock
    private SensiNactSession userSession;

    @Mock
    private Application application;

    @Mock
    private HistoryProvider history;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private ExpansionSettings expansions;

    @Mock
    private ResourceSnapshot resourceSnapshot;

    @Mock
    private ServiceSnapshot serviceSnapshot;

    @Mock
    private ProviderSnapshot providerSnapshot;

    @Mock
    private UriBuilder uriBuilder;

    private void setupResourceSnapshotMocks() {
        when(resourceSnapshot.getService()).thenReturn(serviceSnapshot);
        when(serviceSnapshot.getProvider()).thenReturn(providerSnapshot);
        when(providerSnapshot.getName()).thenReturn("testProvider");
        when(serviceSnapshot.getName()).thenReturn("testService");
        when(resourceSnapshot.getName()).thenReturn("testResource");
    }

    private void setupApplication(int maxResults) {
        when(application.getProperties()).thenReturn(
                Map.of("sensinact.history.service", history, "sensinact.history.result.limit", maxResults));
    }

    @Nested
    @DisplayName("History Provider Configuration")
    class HistoryProviderConfiguration {

        private void setupUriBuilder() {
            when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
            when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.uri(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.resolveTemplate(eq("id"), startsWith("testProvider~testService~testResource~")))
                    .thenReturn(uriBuilder);
            when(uriBuilder.build(any(Object[].class))).thenReturn(URI.create("http://test.com/test"));
            when(uriBuilder.build()).thenReturn(URI.create("http://test.com/test"));
        }

        @Test
        @DisplayName("Should return empty result when no history provider is available")
        void noHistoryProvider() {
            when(application.getProperties()).thenReturn(Map.of());

            ResultList<Observation> result = HistoryResourceHelperSensinact.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, null, expansions, resourceSnapshot, null, 0);

            assertNotNull(result);
            assertTrue(result.value().isEmpty());
        }

        @Test
        @DisplayName("Should process historical data through the HistoryProvider service")
        void withHistoryProvider() {
            Instant now = Instant.now();
            setupResourceSnapshotMocks();
            setupUriBuilder();
            setupApplication(1000);

            when(history.getValueCount(eq(PATH), any())).thenReturn(5L);
            when(history.streamValues(any(), anyLong())).thenReturn(
                    Stream.of(new DefaultTimedValue<>("value2", now), new DefaultTimedValue<>("value1", now)));

            ResultList<Observation> result = HistoryResourceHelperSensinact.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, null, expansions, resourceSnapshot, null, 0);

            assertNotNull(result);
            assertEquals(5, result.count().intValue());
            assertEquals(2, result.value().size());
            // the newest-first stream is reversed to chronological order
            assertEquals("value1", result.value().get(0).result());
            assertEquals("value2", result.value().get(1).result());
        }

        @Test
        @DisplayName("Should request the newest values up to the local result limit")
        void withLocalResultLimit() {
            setupResourceSnapshotMocks();
            setupApplication(1000);
            when(history.getValueCount(eq(PATH), any())).thenReturn(50L);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            HistoryResourceHelperSensinact.loadHistoricalObservations(userSession, application, mapper, uriInfo, null,
                    expansions, resourceSnapshot, null, 100);

            ArgumentCaptor<HistoryQuery> query = ArgumentCaptor.forClass(HistoryQuery.class);
            ArgumentCaptor<Long> maxTotal = ArgumentCaptor.forClass(Long.class);
            verify(history).streamValues(query.capture(), maxTotal.capture());
            assertEquals(PATH, query.getValue().path());
            assertEquals(SortOrder.DESCENDING, query.getValue().order());
            assertEquals(100L, maxTotal.getValue());
        }

        @Test
        @DisplayName("Should apply SensorThings filter to the history and adjust the count")
        void withFilter() throws FilterParserException {
            Instant now = Instant.now();
            setupResourceSnapshotMocks();
            setupUriBuilder();
            setupApplication(1000);

            when(history.getValueCount(eq(PATH), any())).thenReturn(6L);

            SensorthingsFilterComponent filterComponent = new SensorthingsFilterComponent();
            filterComponent.setSession(userSession);
            ICriterion filter = filterComponent.parseFilter(
                    String.format("result eq 'value1' or phenomenonTime lt %s", now.minus(1, DAYS)), OBSERVATIONS);

            // newest first, as the provider returns them
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.<TimedValue<?>>of(
                    new DefaultTimedValue<>("value1", now), new DefaultTimedValue<>("value2", now),
                    new DefaultTimedValue<>("value1", now), new DefaultTimedValue<>("value2", now),
                    new DefaultTimedValue<>("value1", now.minus(3, DAYS)),
                    new DefaultTimedValue<>("value3", now.minus(3, DAYS))));

            ResultList<Observation> result = HistoryResourceHelperSensinact.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, null, expansions, resourceSnapshot, filter, 0);

            assertNotNull(result);
            assertEquals(4, result.value().size());
            // chronological order, the two value2 entries filtered out
            assertEquals("value3", result.value().get(0).result());
            assertEquals("value1", result.value().get(1).result());
            assertEquals("value1", result.value().get(2).result());
            assertEquals("value1", result.value().get(3).result());
            assertEquals(4, result.count().intValue());
        }
    }

    @Nested
    @DisplayName("Query Pushdown")
    class QueryPushdown {

        @Mock
        private ContainerRequestContext requestContext;

        private final Map<String, Object> requestProperties = new HashMap<>();
        private final MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

        private void setupRequestContext() {
            lenient().when(requestContext.getUriInfo()).thenReturn(uriInfo);
            lenient().when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
            lenient().doAnswer(
                    invocation -> requestProperties.put(invocation.getArgument(0), invocation.getArgument(1)))
                    .when(requestContext).setProperty(anyString(), any());
            when(requestContext.getProperty(anyString()))
                    .thenAnswer(invocation -> requestProperties.get(invocation.getArgument(0)));
        }

        private void setupUriBuilder() {
            when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
            when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.uri(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.resolveTemplate(eq("id"), startsWith("testProvider~testService~testResource~")))
                    .thenReturn(uriBuilder);
            when(uriBuilder.build(any(Object[].class))).thenReturn(URI.create("http://test.com/test"));
            when(uriBuilder.build()).thenReturn(URI.create("http://test.com/test"));
        }

        private ResultList<Observation> load() {
            return HistoryResourceHelperSensinact.loadHistoricalObservations(userSession, application, mapper,
                    uriInfo, requestContext, expansions, resourceSnapshot, null, 0);
        }

        @Test
        @DisplayName("Should push $top and $skip into the history query and mark the pagination applied")
        void topAndSkipArePushedDown() {
            Instant now = Instant.now();
            setupResourceSnapshotMocks();
            setupRequestContext();
            setupUriBuilder();
            setupApplication(1000);
            requestProperties.put(PaginationConstants.TOP_PROP, 2);
            requestProperties.put(PaginationConstants.SKIP_PROP, 10);

            when(history.getValueCount(eq(PATH), any())).thenReturn(50L);
            when(history.getValues(any())).thenReturn(HistoryPage.of(
                    List.of(new DefaultTimedValue<>("value1", now), new DefaultTimedValue<>("value2", now)), 10,
                    true));
            UriBuilder requestUriBuilder = mock(UriBuilder.class);
            when(uriInfo.getRequestUriBuilder()).thenReturn(requestUriBuilder);
            when(requestUriBuilder.replaceQueryParam(any(), any())).thenReturn(requestUriBuilder);
            when(requestUriBuilder.build()).thenReturn(URI.create("http://test.com/next"));

            ResultList<Observation> result = load();

            ArgumentCaptor<HistoryQuery> query = ArgumentCaptor.forClass(HistoryQuery.class);
            verify(history).getValues(query.capture());
            assertEquals(PATH, query.getValue().path());
            assertEquals(SortOrder.ASCENDING, query.getValue().order());
            assertEquals(10, query.getValue().offset());
            assertEquals(2, query.getValue().limit());

            assertEquals(2, result.value().size());
            assertEquals("value1", result.value().get(0).result());
            assertEquals(50, result.count().intValue());
            assertEquals("http://test.com/next", result.nextLink());
            verify(requestUriBuilder).replaceQueryParam("$skip", 12L);
            assertEquals(Boolean.TRUE, requestProperties.get(PaginationConstants.PAGINATION_APPLIED));
        }

        @Test
        @DisplayName("Should push a descending time $orderby down and keep the returned order")
        void descendingTimeOrderIsPushedDown() {
            Instant now = Instant.now();
            setupResourceSnapshotMocks();
            setupRequestContext();
            setupUriBuilder();
            setupApplication(1000);
            requestProperties.put(PaginationConstants.TOP_PROP, 2);
            queryParameters.putSingle("$orderby", "phenomenonTime desc");

            when(history.getValueCount(eq(PATH), any())).thenReturn(2L);
            when(history.getValues(any())).thenReturn(HistoryPage.of(List.of(
                    new DefaultTimedValue<>("newest", now), new DefaultTimedValue<>("older", now.minusSeconds(60))),
                    0, false));

            ResultList<Observation> result = load();

            ArgumentCaptor<HistoryQuery> query = ArgumentCaptor.forClass(HistoryQuery.class);
            verify(history).getValues(query.capture());
            assertEquals(SortOrder.DESCENDING, query.getValue().order());
            assertEquals("newest", result.value().get(0).result());
            assertEquals("older", result.value().get(1).result());
            assertNull(result.nextLink());
        }

        @Test
        @DisplayName("Should not push down when the $orderby is not a time field")
        void nonTimeOrderByFallsBackToInMemoryPagination() {
            setupResourceSnapshotMocks();
            setupRequestContext();
            setupApplication(1000);
            requestProperties.put(PaginationConstants.TOP_PROP, 2);
            queryParameters.putSingle("$orderby", "result desc");

            when(history.getValueCount(eq(PATH), any())).thenReturn(0L);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            load();

            verify(history, never()).getValues(any());
            assertNull(requestProperties.get(PaginationConstants.PAGINATION_APPLIED));
        }

        @Test
        @DisplayName("Should not push down when a $filter is present")
        void filterFallsBackToInMemoryPagination() throws FilterParserException {
            setupResourceSnapshotMocks();
            setupApplication(1000);
            requestProperties.put(PaginationConstants.TOP_PROP, 2);

            SensorthingsFilterComponent filterComponent = new SensorthingsFilterComponent();
            filterComponent.setSession(userSession);
            ICriterion filter = filterComponent.parseFilter("result eq 'value1'", OBSERVATIONS);

            when(history.getValueCount(eq(PATH), any())).thenReturn(0L);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            HistoryResourceHelperSensinact.loadHistoricalObservations(userSession, application, mapper, uriInfo,
                    requestContext, expansions, resourceSnapshot, filter, 0);

            verify(history, never()).getValues(any());
            assertNull(requestProperties.get(PaginationConstants.PAGINATION_APPLIED));
        }

        @Test
        @DisplayName("Should not push down without an effective $top")
        void missingTopFallsBackToInMemoryPagination() {
            setupResourceSnapshotMocks();
            setupRequestContext();
            setupApplication(1000);

            when(history.getValueCount(eq(PATH), any())).thenReturn(0L);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            load();

            verify(history, never()).getValues(any());
            assertNull(requestProperties.get(PaginationConstants.PAGINATION_APPLIED));
        }

        @Test
        @DisplayName("Should cap the pushed-down limit at the configured result limit")
        void topIsCappedByResultLimit() {
            setupResourceSnapshotMocks();
            setupRequestContext();
            setupApplication(100);
            requestProperties.put(PaginationConstants.TOP_PROP, 500);

            when(history.getValueCount(eq(PATH), any())).thenReturn(0L);
            when(history.getValues(any())).thenReturn(HistoryPage.of(List.of(), 0, false));

            load();

            ArgumentCaptor<HistoryQuery> query = ArgumentCaptor.forClass(HistoryQuery.class);
            verify(history).getValues(query.capture());
            assertEquals(100, query.getValue().limit());
        }

        @Test
        @DisplayName("Should report existing history for an empty pushed-down page")
        void emptyPageWithHistoryIsNotMistakenForNoHistory() {
            setupResourceSnapshotMocks();
            setupRequestContext();
            setupApplication(1000);
            requestProperties.put(PaginationConstants.TOP_PROP, 5);
            requestProperties.put(PaginationConstants.SKIP_PROP, 100);

            when(history.getValueCount(eq(PATH), any())).thenReturn(20L);
            when(history.getValues(any())).thenReturn(HistoryPage.of(List.of(), 100, false));

            ResultList<Observation> result = load();

            assertTrue(result.value().isEmpty());
            assertEquals(20, result.count().intValue());
            assertTrue(HistoryResourceHelperSensinact.hasHistory(result));
            assertFalse(HistoryResourceHelperSensinact.hasHistory(new ResultList<>(0, null, List.of())));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should clamp a count exceeding Integer.MAX_VALUE")
        void countExceedsIntegerMax() {
            setupResourceSnapshotMocks();
            setupApplication(1000);
            when(history.getValueCount(eq(PATH), any())).thenReturn((long) Integer.MAX_VALUE + 1);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            ResultList<Observation> result = HistoryResourceHelperSensinact.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, null, expansions, resourceSnapshot, null, 0);

            assertNotNull(result);
            assertEquals(Integer.MAX_VALUE, result.count().intValue());
        }

        @Test
        @DisplayName("Should report a zero count for empty history")
        void emptyHistoryHasZeroCount() {
            setupResourceSnapshotMocks();
            setupApplication(1000);
            when(history.getValueCount(eq(PATH), any())).thenReturn(0L);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            ResultList<Observation> result = HistoryResourceHelperSensinact.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, null, expansions, resourceSnapshot, null, 0);

            assertNotNull(result);
            assertEquals(0, result.count().intValue());
            assertTrue(result.value().isEmpty());
        }
    }
}
