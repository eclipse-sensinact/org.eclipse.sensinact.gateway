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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
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

    private MultivaluedMap<String, String> queryParameters;

    @BeforeEach
    void setUp() {
        queryParameters = new MultivaluedHashMap<>();
    }

    // Helper method to verify parameter maps contain expected basic parameters
    private Map<String, Object> hasBasicParams() {
        return argThat(params ->
            params != null &&
            "testProvider".equals(params.get("provider")) &&
            "testService".equals(params.get("service")) &&
            "testResource".equals(params.get("resource"))
        );
    }

    // Helper method to verify parameter maps contain expected parameters with specific values
    private Map<String, Object> hasParams(Object... expectedParams) {
        return argThat(params -> {
            if (params == null) return false;

            // Check basic parameters
            if (!"testProvider".equals(params.get("provider")) ||
                !"testService".equals(params.get("service")) ||
                !"testResource".equals(params.get("resource"))) {
                return false;
            }

            // Check additional expected parameters in pairs (key, value)
            for (int i = 0; i < expectedParams.length; i += 2) {
                String key = (String) expectedParams[i];
                Object expectedValue = expectedParams[i + 1];
                if (!expectedValue.equals(params.get(key))) {
                    return false;
                }
            }
            return true;
        });
    }

    private void setupResourceSnapshotMocks() {
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(resourceSnapshot.getService()).thenReturn(serviceSnapshot);
        when(serviceSnapshot.getProvider()).thenReturn(providerSnapshot);
        when(providerSnapshot.getName()).thenReturn("testProvider");
        when(serviceSnapshot.getName()).thenReturn("testService");
        when(resourceSnapshot.getName()).thenReturn("testResource");
    }

    @Nested
    @DisplayName("History Provider Configuration")
    class HistoryProviderConfiguration {

        private void setupUriBuilder(Instant timestamp) {
            when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
            when(uriBuilder.path(anyString())).thenReturn(uriBuilder);
            when(uriBuilder.uri(anyString())).thenReturn(uriBuilder);
            String timestampString = Long.toString(timestamp.toEpochMilli(), 16);
            when(uriBuilder.resolveTemplate("id", "testProvider~testService~testResource~" + timestampString))
                    .thenReturn(uriBuilder);
            when(uriBuilder.build(any(Object[].class))).thenReturn(java.net.URI.create("http://test.com/test"));
            when(uriBuilder.build()).thenReturn(java.net.URI.create("http://test.com/test"));
        }

        @Test
        @DisplayName("Should return empty result when no history provider is configured")
        void noHistoryProvider() {
            when(application.getProperties()).thenReturn(Map.of());

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, expansions, resourceSnapshot, 0);

            assertNotNull(result);
            assertTrue(result.value.isEmpty());
        }

        @Test
        @DisplayName("Should process historical data when history provider is configured")
        void withHistoryProvider() {
            Instant now = Instant.now();
            setupResourceSnapshotMocks();
            setupUriBuilder(now);
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
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("rangeFiltered"), hasBasicParams()))
                    .thenReturn(timedValues);

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, expansions, resourceSnapshot, 0);

            assertNotNull(result);
            assertEquals(count.intValue(), result.count.intValue());
            verify(userSession).actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams());
            verify(userSession, times(3)).actOnResource(eq(historyProvider), eq("history"), eq("rangeFiltered"), hasBasicParams());
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
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams())).thenReturn(50L);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("rangeFiltered"), hasBasicParams()))
                    .thenReturn(Arrays.asList());

            HistoryResourceHelper.loadHistoricalObservations(userSession, application, mapper, uriInfo, expansions,
                    resourceSnapshot, localResultLimit);

            verify(userSession).actOnResource(eq(historyProvider), eq("history"), eq("rangeFiltered"), hasBasicParams());
        }
    }

    @Nested
    @DisplayName("Query Parameter Handling")
    class QueryParameterHandling {

        private void setupHistoryProvider() {
            setupResourceSnapshotMocks();
            String historyProvider = "test-history-provider";
            Map<String, Object> appProperties = Map.of("sensinact.history.provider", historyProvider,
                    "sensinact.history.result.limit", 1000);
            when(application.getProperties()).thenReturn(appProperties);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams())).thenReturn(50L);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("rangeFiltered"), hasBasicParams()))
                    .thenReturn(Arrays.asList());
        }

        @Test
        @DisplayName("Should extract and apply $top parameter")
        void topParameter() {
            // Given
            queryParameters.add("$top", "10");
            setupHistoryProvider();

            // When
            HistoryResourceHelper.loadHistoricalObservations(userSession, application, mapper, uriInfo, expansions,
                    resourceSnapshot, 0);

            // Then
            verify(userSession).actOnResource(eq("test-history-provider"), eq("history"), eq("rangeFiltered"),
                    hasParams("top", 10, "skip", 0));
        }

        @Test
        @DisplayName("Should extract and apply $skip parameter")
        void skipParameter() {
            // Given
            queryParameters.add("$skip", "5");
            setupHistoryProvider();

            // When
            HistoryResourceHelper.loadHistoricalObservations(userSession, application, mapper, uriInfo, expansions,
                    resourceSnapshot, 0);

            // Then
            verify(userSession).actOnResource(eq("test-history-provider"), eq("history"), eq("rangeFiltered"),
                    hasParams("top", 1000, "skip", 5));
        }

        @Test
        @DisplayName("Should extract and apply $orderby ascending")
        void orderByAscending() {
            // Given
            queryParameters.add("$orderby", "phenomenonTime asc");
            setupHistoryProvider();

            // When
            HistoryResourceHelper.loadHistoricalObservations(userSession, application, mapper, uriInfo, expansions,
                    resourceSnapshot, 0);

            // Then
            verify(userSession).actOnResource(eq("test-history-provider"), eq("history"), eq("rangeFiltered"),
                    hasParams("top", 1000, "skip", 0, "orderBy", "asc"));
        }

        @Test
        @DisplayName("Should extract and apply $orderby descending")
        void orderByDescending() {
            // Given
            queryParameters.add("$orderby", "phenomenonTime desc");
            setupHistoryProvider();

            // When
            HistoryResourceHelper.loadHistoricalObservations(userSession, application, mapper, uriInfo, expansions,
                    resourceSnapshot, 0);

            // Then
            verify(userSession).actOnResource(eq("test-history-provider"), eq("history"), eq("rangeFiltered"),
                    hasParams("top", 1000, "skip", 0, "orderBy", "desc"));
        }

        @Test
        @DisplayName("Should handle all query parameters together")
        void allParametersTogether() {
            // Given
            queryParameters.add("$top", "20");
            queryParameters.add("$skip", "10");
            queryParameters.add("$orderby", "phenomenonTime desc");
            setupHistoryProvider();

            // When
            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, expansions, resourceSnapshot, 0);

            // Then
            assertNotNull(result);
            assertEquals(50, result.count.intValue());
            verify(userSession).actOnResource(eq("test-history-provider"), eq("history"), eq("count"), hasBasicParams());
            verify(userSession).actOnResource(eq("test-history-provider"), eq("history"), eq("rangeFiltered"),
                    hasParams("top", 20, "skip", 10, "orderBy", "desc"));
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
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("rangeFiltered"), hasBasicParams()))
                    .thenReturn(Arrays.asList());

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, expansions, resourceSnapshot, 0);

            assertNotNull(result);
            assertEquals(Integer.MAX_VALUE, result.count.intValue());
        }

        @Test
        @DisplayName("Should handle null count from history provider")
        void nullCount() {
            setupResourceSnapshotMocks();
            String historyProvider = "test-history-provider";

            Map<String, Object> appProperties = Map.of("sensinact.history.provider", historyProvider,
                    "sensinact.history.result.limit", 1000);
            when(application.getProperties()).thenReturn(appProperties);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("count"), hasBasicParams())).thenReturn(null);
            when(userSession.actOnResource(eq(historyProvider), eq("history"), eq("rangeFiltered"), hasBasicParams()))
                    .thenReturn(Arrays.asList());

            ResultList<Observation> result = HistoryResourceHelper.loadHistoricalObservations(userSession,
                    application, mapper, uriInfo, expansions, resourceSnapshot, 0);

            assertNotNull(result);
            assertEquals(null, result.count);
        }
    }
}
