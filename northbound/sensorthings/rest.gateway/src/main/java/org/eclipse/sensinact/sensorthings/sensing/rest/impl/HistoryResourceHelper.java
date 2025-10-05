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
    static ResultList<Observation> loadHistoricalObservations(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ResourceSnapshot resourceSnapshot, int localResultLimit) {
        ResultList<Observation> list = new ResultList<>();
        list.value = new ArrayList<>();
        String historyProvider = (String) application.getProperties().get("sensinact.history.provider");

        if (historyProvider != null) {
            Integer maxResults = (Integer) application.getProperties().get("sensinact.history.result.limit");

            if (localResultLimit > 0) {
                maxResults = Math.min(localResultLimit, maxResults);
            }

            Map<String, Object> params = initParameter(resourceSnapshot);
            // Get count for the full dataset (for pagination metadata)
            Long count = (Long) userSession.actOnResource(historyProvider, "history", "count", params);
            list.count = count == null ? null : count > Integer.MAX_VALUE ? Integer.MAX_VALUE : count.intValue();

            int skip = 0;

            List<TimedValue<?>> timed;
            do {
                params.put("skip", skip);

                timed = (List<TimedValue<?>>) userSession.actOnResource(historyProvider, "history", "range",
                        params);
                list.value.addAll(0, DtoMapper.toObservationList(userSession, application, mapper, uriInfo, expansions,
                        resourceSnapshot, timed));
                if (timed.isEmpty()) {
                    break;
                } else if (timed.size() == 500) {
                    skip = list.value.size();
                }

            } while (list.value.size() < count && list.value.size() < maxResults);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    static ResultList<HistoricalLocation> loadHistoricalLocations(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ProviderSnapshot provider, int localResultLimit) {
        ResultList<HistoricalLocation> list = new ResultList<>();
        list.value = new ArrayList<>();
        String historyProvider = (String) application.getProperties().get("sensinact.history.provider");

        if (historyProvider != null) {
            Integer maxResults = (Integer) application.getProperties().get("sensinact.history.result.limit");

            if (localResultLimit > 0) {
                maxResults = Math.min(localResultLimit, maxResults);
            }

            Map<String, Object> params = initParameter(provider);
            // Get count for the full dataset (for pagination metadata)
            Long count = (Long) userSession.actOnResource(historyProvider, "history", "count", params);
            list.count = count == null ? null : count > Integer.MAX_VALUE ? Integer.MAX_VALUE : count.intValue();

            int skip = 0;

            List<TimedValue<?>> timed;
            do {
                params.put("skip", skip);

                timed = (List<TimedValue<?>>) userSession.actOnResource(historyProvider, "history", "range", params);
                list.value.addAll(0, DtoMapper.toHistoricalLocationList(userSession, application, mapper, uriInfo,
                        expansions, provider, timed));
                if (timed.isEmpty()) {
                    break;
                } else if (timed.size() == 500) {
                    skip = list.value.size();
                }

            } while (list.value.size() < count && list.value.size() < maxResults);
        }
        return list;
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
