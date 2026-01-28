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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

/**
 * Helper class for accessing historical observation data from the history
 * provider
 */
public class HistoryResourceHelperSensorthings {

    private HistoryResourceHelperSensorthings() {
    }

    @SuppressWarnings("unchecked")
    public static ResultList<Observation> loadHistoricalObservations(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ResourceSnapshot resourceSnapshot, ICriterion filter, int localResultLimit) {
        String historyProvider = (String) application.getProperties().get("sensinact.history.provider");

        if (historyProvider == null) {
            return new ResultList<>(null, null, List.of());
        }

        Integer maxResults = getMaxResult(application, localResultLimit);
        Map<String, Object> params = initParameter(resourceSnapshot);
        // Get count for the full dataset (for pagination metadata)
        Long count = (Long) userSession.actOnResource(historyProvider, "history", "count", params);
        List<Observation> values = new ArrayList<>();
        int skip = 0;

        List<TimedValue<?>> timed;

        do {
            params.put("skip", skip);

            timed = (List<TimedValue<?>>) userSession.actOnResource(historyProvider, "history", "range", params);
            // Filtering happens at a lower level, so we may not use all the discovered
            // history
            List<Observation> observationList = DtoMapper.toObservationList(userSession, application, mapper, uriInfo,
                    expansions, filter, resourceSnapshot, timed);

            if (count != null && count < Integer.MAX_VALUE && observationList.size() < timed.size()) {
                count -= (timed.size() - observationList.size());
            }
            values.addAll(0, observationList);
            if (timed.isEmpty()) {
                break;
            }
            skip += timed.size();
            // Keep going until the list is as full as count, or it hits maxResults
        } while ((count == null || values.size() < count) && values.size() < maxResults);
        return new ResultList<>(count == null ? null : count > Integer.MAX_VALUE ? Integer.MAX_VALUE : count.intValue(),
                null, values);
    }

    static ResultList<HistoricalLocation> loadHistoricalLocations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot providerThing, int localResultLimit) {
        return loadHistoricalLocations(userSession, application, mapper, uriInfo, expansions, filter,
                List.of(providerThing), null, localResultLimit);
    }

    @SuppressWarnings("unchecked")
    static ResultList<HistoricalLocation> loadHistoricalLocations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            List<ProviderSnapshot> providerThings, String locationId, int localResultLimit) {
        String historyProvider = (String) application.getProperties().get("sensinact.history.provider");
        if (historyProvider == null) {
            return new ResultList<>(null, null, List.of());
        }
        AtomicLong totalCount = new AtomicLong(0);
        List<HistoricalLocation> values = new ArrayList<>();

        Integer maxResults = getMaxResult(application, localResultLimit);
        for (ProviderSnapshot providerThing : providerThings) {
            Map<String, Object> params = initParameter(providerThing);
            // Get count for the full dataset (for pagination metadata)
            Long count = (Long) userSession.actOnResource(historyProvider, "history", "count", params);
            if (count != null)
                totalCount.addAndGet(count);
            int skip = 0;

            List<TimedValue<?>> timed;
            do {
                params.put("skip", skip);

                timed = (List<TimedValue<?>>) userSession.actOnResource(historyProvider, "history", "range", params);
                List<HistoricalLocation> historicalLocationList = DtoMapper.toHistoricalLocationList(userSession,
                        application, mapper, uriInfo, expansions, filter, providerThing, locationId, timed);
                if (count != null && count < Integer.MAX_VALUE && historicalLocationList.size() < timed.size()) {
                    count -= (timed.size() - historicalLocationList.size());
                }
                values.addAll(0, historicalLocationList);
                if (timed.isEmpty()) {
                    break;
                }
                skip += timed.size();

            } while ((count == null || values.size() < count) && values.size() < maxResults);
        }
        return new ResultList<>(
                totalCount == null ? null
                        : totalCount.get() > Integer.MAX_VALUE ? Integer.MAX_VALUE : totalCount.intValue(),
                null, values);
    }

    private static Integer getMaxResult(Application application, int localResultLimit) {
        Integer maxResults = (Integer) application.getProperties().get("sensinact.history.result.limit");
        if (localResultLimit > 0) {
            maxResults = Math.min(localResultLimit, maxResults);
        }
        return maxResults;
    }

    private static Map<String, Object> initParameter(ResourceSnapshot resourceSnapshot) {
        String provider = resourceSnapshot.getService().getProvider().getName();
        String service = resourceSnapshot.getService().getName();
        String resource = resourceSnapshot.getName();
        Map<String, Object> params = new HashMap<>();
        params.put("provider", provider);
        params.put("service", service);
        params.put("resource", resource);
        return params;
    }

    private static Map<String, Object> initParameter(ProviderSnapshot providerSnapshot) {
        String provider = providerSnapshot.getName();
        Map<String, Object> params = new HashMap<>();
        params.put("provider", provider);
        params.put("service", "admin");
        params.put("resource", "location");
        return params;
    }
}
