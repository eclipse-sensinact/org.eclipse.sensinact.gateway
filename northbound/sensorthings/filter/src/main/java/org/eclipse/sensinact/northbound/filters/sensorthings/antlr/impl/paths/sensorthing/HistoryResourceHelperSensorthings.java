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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;

import tools.jackson.databind.ObjectMapper;

/**
 * Helper class for accessing historical observation data from the history
 * provider
 */
public class HistoryResourceHelperSensorthings {

    private HistoryResourceHelperSensorthings() {
    }

    @SuppressWarnings("unchecked")
    public static List<ExpandedObservation> loadHistoricalObservations(SensiNactSession userSession,
            ObjectMapper mapper, ResourceSnapshot resourceSnapshot, String historyProvider, int localResultLimit,
            IDtoMemoryCache<ExpandedObservation> cacheObs) {
        List<ExpandedObservation> values = new ArrayList<>();
        values.add(DtoMapperSimple.parseExpandObservation(mapper, resourceSnapshot.getValue().getValue()));
        if (cacheObs != null) {
            cacheObs.keySet().stream()
                    .filter(obsId -> obsId.startsWith(resourceSnapshot.getService().getProvider().getName()))
                    .map(obsId -> {
                        return cacheObs.getDto(obsId);
                    }).forEach(o -> values.add(o));

        }

        if (historyProvider == null) {
            return values;
        }

        Integer maxResults = localResultLimit;
        Map<String, Object> params = initParameter(resourceSnapshot);
        // Get count for the full dataset (for pagination metadata)
        Long count = (Long) userSession.actOnResource(historyProvider, "history", "count", params);
        int skip = 0;

        List<TimedValue<?>> timed;

        do {
            params.put("skip", skip);

            timed = (List<TimedValue<?>>) userSession.actOnResource(historyProvider, "history", "range", params);

            values.addAll(0,
                    timed.stream().map(tv -> DtoMapperSimple.parseExpandObservation(mapper, tv.getValue())).toList());
            if (timed.isEmpty()) {
                break;
            }
            skip += timed.size();
            // Keep going until the list is as full as count, or it hits maxResults
        } while ((count == null || values.size() < count) && values.size() < maxResults);
        return values;
    }

    @SuppressWarnings("unchecked")
    static List<Instant> loadHistoricalLocations(SensiNactSession userSession, ObjectMapper mapper,
            List<ProviderSnapshot> providerThings, String historyProvider, int localResultLimit,
            IDtoMemoryCache<Instant> cacheHl) {
        List<Instant> values = new ArrayList<>();

        if (cacheHl != null) {
            values.addAll(providerThings.stream().map(p -> p.getName()).flatMap(idProv -> cacheHl.keySet().stream()
                    .filter(id -> id.startsWith(idProv)).map(id -> cacheHl.getDto(id))).toList());
            values.addAll(providerThings.stream()
                    .map(prov -> DtoMapperSimple.getLocation(prov, mapper, true).getTimestamp()).toList());
        }

        if (historyProvider == null) {
            return values;
        }
        AtomicLong totalCount = new AtomicLong(0);

        Integer maxResults = localResultLimit;
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
                List<Instant> historicalLocationList = timed.stream().map(t -> t.getTimestamp()).toList();
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
        return values;
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
