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
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.eNS_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.northbound.filters.sensorthings.impl.SensorthingsFilterComponent;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.HistoryResourceHelperSensorthings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class HistoryResourceHelperSensorthingsTest {

    private static final ResourcePath PATH = new ResourcePath("testProvider", "datastream", "lastObservation");

    @Mock
    private SensiNactSession userSession;

    @Mock
    private HistoryProvider history;

    private static ObjectMapper mapper;
    private static DtoMapper dtoMapper;

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

    private ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = JsonMapper.builder().build();
        }
        return mapper;
    }

    private DtoMapper getDtoMapper() {
        if (dtoMapper == null) {
            dtoMapper = new DtoMapper(null, 0, null, null);
        }
        return dtoMapper;
    }

    private String getObservation(String id, Object result, Instant instant) {

        ExpandedObservation obs = new ExpandedObservation(null, id,
                instant != null ? instant : Instant.now().truncatedTo(ChronoUnit.SECONDS),
                instant != null ? instant : Instant.now().truncatedTo(ChronoUnit.SECONDS), result, "test", null, null,
                null, null, null, null,
                new FeatureOfInterest(null, "test", "test", "test", "test", new Point(0, 0), null, null), false);
        try {
            return mapper.writeValueAsString(obs);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    private TimedValue<?> observationValue(Object result, Instant instant) {
        return new DefaultTimedValue<>(getObservation("testProvider~testResource", result, instant), instant);
    }

    private void setupResourceSnapshotMocks() {
        when(resourceSnapshot.getService()).thenReturn(serviceSnapshot);
        when(serviceSnapshot.getProvider()).thenReturn(providerSnapshot);
        when(providerSnapshot.getName()).thenReturn("testProvider");
        when(serviceSnapshot.getName()).thenReturn("datastream");
        when(resourceSnapshot.getName()).thenReturn("lastObservation");
    }

    @Nested
    @DisplayName("History Provider Configuration")
    class HistoryProviderConfiguration {

        private void setupUriBuilder() {
            when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
            when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.uri(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.resolveTemplate(eq("id"), startsWith("testProvider~testResource~"))).thenReturn(uriBuilder);
            when(uriBuilder.build(any(Object[].class))).thenReturn(URI.create("http://test.com/test"));
            when(uriBuilder.build()).thenReturn(URI.create("http://test.com/test"));
        }

        @Test
        @DisplayName("Should return empty result when no history provider is available")
        void noHistoryProvider() {

            ResultList<Observation> result = HistoryResourceHelperSensorthings.loadHistoricalObservations(userSession,
                    getDtoMapper(), getMapper(), uriInfo, null, expansions, resourceSnapshot, null, null, 0, null);

            assertNotNull(result);
            assertTrue(result.value().isEmpty());
        }

        @Test
        @DisplayName("Should process historical data through the HistoryProvider service")
        void withHistoryProvider() {
            Instant now = Instant.now();
            setupResourceSnapshotMocks();
            setupUriBuilder();

            when(history.getValueCount(eq(PATH), any())).thenReturn(5L);
            // newest first, as the provider returns them
            when(history.streamValues(any(), anyLong()))
                    .thenReturn(Stream.of(observationValue("value2", now), observationValue("value1", now)));

            ResultList<Observation> result = HistoryResourceHelperSensorthings.loadHistoricalObservations(userSession,
                    getDtoMapper(), getMapper(), uriInfo, null, expansions, resourceSnapshot, null, history, 1000, null);

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

            when(history.getValueCount(eq(PATH), any())).thenReturn(50L);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            HistoryResourceHelperSensorthings.loadHistoricalObservations(userSession, getDtoMapper(), getMapper(),
                    uriInfo, null, expansions, resourceSnapshot, null, history, 100, null);

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
            when(providerSnapshot.getModelPackageUri()).thenReturn(eNS_URI);

            when(history.getValueCount(eq(PATH), any())).thenReturn(6L);

            SensorthingsFilterComponent filterComponent = new SensorthingsFilterComponent();
            filterComponent.setSession(userSession);
            ICriterion filter = filterComponent.parseFilter(
                    String.format("result eq 'value1' or phenomenonTime lt %s", now.minus(1, DAYS)), OBSERVATIONS);

            // newest first, as the provider returns them
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of(
                    observationValue("value1", now), observationValue("value2", now),
                    observationValue("value1", now), observationValue("value2", now),
                    observationValue("value1", now.minus(3, DAYS)), observationValue("value3", now.minus(3, DAYS))));

            ResultList<Observation> result = HistoryResourceHelperSensorthings.loadHistoricalObservations(userSession,
                    getDtoMapper(), getMapper(), uriInfo, null, expansions, resourceSnapshot, filter, history, 1000, null);

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
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should skip the count query when $count is not requested")
        void countNotRequestedSkipsTheCountQuery() {
            setupResourceSnapshotMocks();

            ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            ResultList<Observation> result = HistoryResourceHelperSensorthings.loadHistoricalObservations(userSession,
                    getDtoMapper(), getMapper(), uriInfo, requestContext, expansions, resourceSnapshot, null, history,
                    1000, null);

            assertNull(result.count());
            verify(history, never()).getValueCount(any(), any());
        }

        @Test
        @DisplayName("Should clamp a count exceeding Integer.MAX_VALUE")
        void countExceedsIntegerMax() {
            setupResourceSnapshotMocks();

            when(history.getValueCount(eq(PATH), any())).thenReturn((long) Integer.MAX_VALUE + 1);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            ResultList<Observation> result = HistoryResourceHelperSensorthings.loadHistoricalObservations(userSession,
                    getDtoMapper(), getMapper(), uriInfo, null, expansions, resourceSnapshot, null, history, 1000, null);

            assertNotNull(result);
            assertEquals(Integer.MAX_VALUE, result.count().intValue());
        }

        @Test
        @DisplayName("Should report a zero count for empty history")
        void emptyHistoryHasZeroCount() {
            setupResourceSnapshotMocks();

            when(history.getValueCount(eq(PATH), any())).thenReturn(0L);
            when(history.streamValues(any(), anyLong())).thenReturn(Stream.of());

            ResultList<Observation> result = HistoryResourceHelperSensorthings.loadHistoricalObservations(userSession,
                    getDtoMapper(), getMapper(), uriInfo, null, expansions, resourceSnapshot, null, history, 0, null);

            assertNotNull(result);
            assertEquals(0, result.count().intValue());
            assertTrue(result.value().isEmpty());
        }
    }
}
