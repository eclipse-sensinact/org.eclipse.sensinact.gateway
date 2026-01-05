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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
class HistoryResourceHelper {

    private HistoryResourceHelper() {
    }

    @SuppressWarnings("unchecked")
    static ResultList<Observation> loadHistoricalObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resourceSnapshot,
            ICriterion filter, int localResultLimit) {
        // TODO review
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
            List<Observation> observationList = DtoMapperGet.toObservationList(userSession, application, mapper,
                    uriInfo, expansions, filter, resourceSnapshot, timed);
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

    @SuppressWarnings("unchecked")
    static ResultList<HistoricalLocation> loadHistoricalLocations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ICriterion filter,
            ProviderSnapshot provider, int localResultLimit) {
        String historyProvider = (String) application.getProperties().get("sensinact.history.provider");
        if (historyProvider == null) {
            return new ResultList<>(null, null, List.of());
        }

        Integer maxResults = getMaxResult(application, localResultLimit);
        Map<String, Object> params = initParameter(provider);
        // Get count for the full dataset (for pagination metadata)
        Long count = (Long) userSession.actOnResource(historyProvider, "history", "count", params);
        List<HistoricalLocation> values = new ArrayList<>();
        int skip = 0;

        List<TimedValue<?>> timed;
        do {
            params.put("skip", skip);

            timed = (List<TimedValue<?>>) userSession.actOnResource(historyProvider, "history", "range", params);
            List<HistoricalLocation> historicalLocationList = DtoMapperGet.toHistoricalLocationList(userSession,
                    application, mapper, uriInfo, expansions, filter, provider, timed);
            if (count != null && count < Integer.MAX_VALUE && historicalLocationList.size() < timed.size()) {
                count -= (timed.size() - historicalLocationList.size());
            }
            values.addAll(0, historicalLocationList);
            if (timed.isEmpty()) {
                break;
            }
            skip += timed.size();

        } while ((count == null || values.size() < count) && values.size() < maxResults);
        return new ResultList<>(count == null ? null : count > Integer.MAX_VALUE ? Integer.MAX_VALUE : count.intValue(),
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
