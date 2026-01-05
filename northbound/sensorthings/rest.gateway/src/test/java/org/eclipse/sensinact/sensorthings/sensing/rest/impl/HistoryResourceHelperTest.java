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
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.OBSERVATIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.sensorthings.impl.SensorthingsFilterComponent;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ExpansionSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@ExtendWith(MockitoExtension.class)
class HistoryResourceHelperTest {

    @Mock
    private SensiNactSession userSession;

    @Mock
    private Application application;

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

    // Helper method to verify parameter maps contain expected basic parameters
    private Map<String, Object> hasBasicParams() {
        return argThat(params -> params != null && "testProvider".equals(params.get("provider"))
                && "testService".equals(params.get("service")) && "testResource".equals(params.get("resource")));
    }

    private void setupResourceSnapshotMocks() {
        when(resourceSnapshot.getService()).thenReturn(serviceSnapshot);
        when(serviceSnapshot.getProvider()).thenReturn(providerSnapshot);
        when(providerSnapshot.getName()).thenReturn("testProvider");
        when(serviceSnapshot.getName()).thenReturn("testService");
        when(resourceSnapshot.getName()).thenReturn("testResource");
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
            when(uriBuilder.build(any(Object[].class))).thenReturn(java.net.URI.create("http://test.com/test"));
            when(uriBuilder.build()).thenReturn(java.net.URI.create("http://test.com/test"));
        }

        @Test
        @DisplayName("Should return empty result when no history provider is configured")
        void noHistoryProvider() {
            when(application.getProperties()).thenReturn(Map.of());

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession, application,
                    mapper, uriInfo, expansions, resourceSnapshot, null, 0);

            assertNotNull(result);
            assertTrue(result.value().isEmpty());
        }

        @Test
        @DisplayName("Should process historical data when history provider is configured")
        void withHistoryProvider() {
            Instant now = Instant.now();
            setupResourceSnapshotMocks();
            setupUriBuilder();
            String historyProvider = "test-history-provider";
            Integer maxResults = 1000;
            Long count = 5L;

            Map<String, Object> appProperties = Map.of("sensinact.history.provider", historyProvider,
                    "sensinact.history.result.limit", maxResults);
            when(application.getProperties()).thenReturn(appProperties);

            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams()))
                    .thenReturn(count);

            List<TimedValue<?>> timedValues = Arrays.asList(new DefaultTimedValue<>("value1", now),
                    new DefaultTimedValue<>("value2", now));
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("range"), hasBasicParams()))
                    .thenReturn(timedValues);

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession, application,
                    mapper, uriInfo, expansions, resourceSnapshot, null, 0);

            assertNotNull(result);
            assertEquals(count.intValue(), result.count().intValue());
            verify(userSession).actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams());
            verify(userSession, times(3)).actOnResource(eq(historyProvider), eq("history"), eq("range"),
                    hasBasicParams());
        }

        @Test
        @DisplayName("Should apply local result limit when provided")
        void withLocalResultLimit() {
            setupResourceSnapshotMocks();
            String historyProvider = "test-history-provider";
            Integer maxResults = 1000;
            int localResultLimit = 100;

            Map<String, Object> appProperties = Map.of("sensinact.history.provider", historyProvider,
                    "sensinact.history.result.limit", maxResults);
            when(application.getProperties()).thenReturn(appProperties);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams()))
                    .thenReturn(50L);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("range"), hasBasicParams()))
                    .thenReturn(Arrays.asList());

            HistoryResourceHelper.loadHistoricalObservations(userSession, application, mapper, uriInfo, expansions,
                    resourceSnapshot, null, localResultLimit);

            verify(userSession).actOnResource(eq(historyProvider), eq("history"), eq("range"), hasBasicParams());
        }

        @Test
        @DisplayName("Should apply SensorThings filter to the history")
        void withFilter() throws FilterParserException {
            Instant now = Instant.now();
            setupResourceSnapshotMocks();
            setupUriBuilder();
            String historyProvider = "test-history-provider";
            Integer maxResults = 1000;
            Long count = 6L;

            Map<String, Object> appProperties = Map.of("sensinact.history.provider", historyProvider,
                    "sensinact.history.result.limit", maxResults);
            when(application.getProperties()).thenReturn(appProperties);

            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams()))
                    .thenReturn(count);

            ICriterion filter = new SensorthingsFilterComponent().parseFilter(
                    String.format("result eq 'value1' or phenomenonTime lt %s", now.minus(1, DAYS)), OBSERVATIONS);

            List<TimedValue<?>> timedValues = Arrays.asList(new DefaultTimedValue<>("value1", now),
                    new DefaultTimedValue<>("value2", now));
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("range"), hasBasicParams()))
                    .thenReturn(timedValues, timedValues, List.of(new DefaultTimedValue<>("value1", now.minus(3, DAYS)),
                            new DefaultTimedValue<>("value3", now.minus(3, DAYS))), List.of());

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession, application,
                    mapper, uriInfo, expansions, resourceSnapshot, filter, 0);

            assertNotNull(result);
            assertEquals(4, result.value().size());
            // Result batches come in reverse order
            assertEquals("value1", result.value().get(0).result());
            assertEquals("value3", result.value().get(1).result());
            assertEquals("value1", result.value().get(2).result());
            assertEquals("value1", result.value().get(3).result());
            assertEquals(4, result.count().intValue());
            verify(userSession).actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams());
            verify(userSession, times(3)).actOnResource(eq(historyProvider), eq("history"), eq("range"),
                    hasBasicParams());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle count exceeding Integer.MAX_VALUE")
        void countExceedsIntegerMax() {
            setupResourceSnapshotMocks();
            String historyProvider = "test-history-provider";
            Long largeCount = (long) Integer.MAX_VALUE + 1;

            Map<String, Object> appProperties = Map.of("sensinact.history.provider", historyProvider,
                    "sensinact.history.result.limit", 1000);
            when(application.getProperties()).thenReturn(appProperties);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams()))
                    .thenReturn(largeCount);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("range"), hasBasicParams()))
                    .thenReturn(Arrays.asList());

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession, application,
                    mapper, uriInfo, expansions, resourceSnapshot, null, 0);

            assertNotNull(result);
            assertEquals(Integer.MAX_VALUE, result.count().intValue());
        }

        @Test
        @DisplayName("Should handle null count from history provider")
        void nullCount() {
            setupResourceSnapshotMocks();
            String historyProvider = "test-history-provider";

            Map<String, Object> appProperties = Map.of("sensinact.history.provider", historyProvider,
                    "sensinact.history.result.limit", 1000);
            when(application.getProperties()).thenReturn(appProperties);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams()))
                    .thenReturn(null);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("range"), hasBasicParams()))
                    .thenReturn(Arrays.asList());

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession, application,
                    mapper, uriInfo, expansions, resourceSnapshot, null, 0);

            assertNotNull(result);
            assertEquals(null, result.count());
        }
    }
}
