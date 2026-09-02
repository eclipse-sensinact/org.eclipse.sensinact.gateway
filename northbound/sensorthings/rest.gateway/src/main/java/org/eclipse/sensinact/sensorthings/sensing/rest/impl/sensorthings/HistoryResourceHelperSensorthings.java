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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.PaginationConstants;

import tools.jackson.databind.ObjectMapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

/**
 * Helper class for accessing historical observation data through the
 * {@link HistoryProvider} service published by the application. Results are
 * the newest values up to the configured result limit, in chronological
 * order; entries from the optional in-memory cache follow the history data.
 */
public class HistoryResourceHelperSensorthings {

    private HistoryResourceHelperSensorthings() {
    }

    public static ResultList<Observation> loadHistoricalObservations(SensiNactSession userSession, DtoMapper dtoMapper,
            ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext, ExpansionSettings expansions,
            ResourceSnapshot resourceSnapshot, ICriterion filter, HistoryProvider history, int localResultLimit,
            IDtoMemoryCache<ExpandedObservation> cacheObs) {
        List<Observation> values = new ArrayList<>();

        if (cacheObs != null) {
            Optional<Observation> obsLive = dtoMapper.toObservation(userSession, mapper, uriInfo, expansions, filter,
                    resourceSnapshot);
            if (obsLive.isPresent())
                values.add(obsLive.get());
            cacheObs.keySet().stream()
                    .filter(obsId -> obsId.startsWith(resourceSnapshot.getService().getProvider().getName()))
                    .map(obsId -> {
                        ExpandedObservation expObs = cacheObs.getDto(obsId);
                        Instant stamp = DtoMapperSimple.getTimestampFromId(obsId);
                        return dtoMapper.toObservation(userSession, mapper, uriInfo, expansions, filter,
                                resourceSnapshot, stamp, expObs);
                    }).filter(opt -> opt.isPresent()).map(o -> o.get()).forEach(o -> values.add(o));
        }

        if (history == null) {
            return new ResultList<>(values);
        }

        ResourcePath path = new ResourcePath(resourceSnapshot.getService().getProvider().getName(),
                resourceSnapshot.getService().getName(), resourceSnapshot.getName());

        List<TimedValue<?>> timed = newestChronological(history, path, Math.max(0, localResultLimit - values.size()));

        // Filtering happens at a lower level, so we may not use all the
        // discovered history
        List<Observation> observationList = dtoMapper.toObservationList(userSession, mapper, uriInfo, expansions,
                filter, resourceSnapshot, timed);
        values.addAll(0, observationList);

        Long count = null;
        if (countRequested(requestContext)) {
            count = history.getValueCount(path, TimeRange.ALL) - (timed.size() - observationList.size());
        }
        return new ResultList<>(clamped(count), null, values);
    }

    public static ResultList<HistoricalLocation> loadHistoricalLocations(SensiNactSession userSession,
            DtoMapper dtoMapper, ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot providerThing, HistoryProvider history,
            int localResultLimit, IDtoMemoryCache<Instant> cacheHl) {

        return loadHistoricalLocations(userSession, dtoMapper, mapper, uriInfo, requestContext, expansions, filter,
                List.of(providerThing), null, history, localResultLimit, cacheHl);
    }

    static ResultList<HistoricalLocation> loadHistoricalLocations(SensiNactSession userSession, DtoMapper dtoMapper,
            ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext, ExpansionSettings expansions,
            ICriterion filter, List<ProviderSnapshot> providerThings, String locationId, HistoryProvider history,
            int localResultLimit, IDtoMemoryCache<Instant> cacheHl) {
        List<HistoricalLocation> values = new ArrayList<>();

        if (cacheHl != null) {
            values.addAll(providerThings.stream().flatMap(p -> {

                String idProv = p.getName();
                return cacheHl.keySet().stream().filter(id -> id.startsWith(idProv))
                        .map(id -> dtoMapper.toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter, id,
                                cacheHl.getDto(id), p));
            }).filter(hl -> hl.isPresent()).map(hl -> hl.get()).toList());
            values.addAll(providerThings.stream().flatMap(prov -> {
                TimedValue<GeoJsonObject> location = DtoMapperSimple.getLocation(prov, mapper, true);
                return dtoMapper.toHistoricalLocation(userSession, mapper, uriInfo, expansions, filter, prov,
                        Optional.of(location)).stream();
            }).toList());

        }

        if (history == null) {
            return new ResultList<>(values);
        }

        boolean withCount = countRequested(requestContext);
        long totalCount = 0;
        for (ProviderSnapshot providerThing : providerThings) {
            ResourcePath path = new ResourcePath(providerThing.getName(), "admin", "location");

            if (withCount) {
                totalCount += history.getValueCount(path, TimeRange.ALL);
            }
            List<TimedValue<?>> timed = newestChronological(history, path,
                    Math.max(0, localResultLimit - values.size()));

            values.addAll(0, dtoMapper.toHistoricalLocationList(userSession, mapper, uriInfo, expansions, filter,
                    providerThing, locationId, timed));
        }
        return new ResultList<>(withCount ? clamped(totalCount) : null, null, values);
    }

    /**
     * Whether the count must be computed: the client asked for it with
     * $count=true, or there is no request context — an $expand sub-list,
     * which always carries its count. Counting is skipped otherwise: it is
     * by far the most expensive part of a paginated request and the count
     * filter would discard it anyway.
     */
    private static boolean countRequested(ContainerRequestContext requestContext) {
        return requestContext == null
                || Boolean.TRUE.equals(requestContext.getProperty(PaginationConstants.COUNT_PROP));
    }

    private static Integer clamped(Long count) {
        return count == null ? null : (int) Math.min(Integer.MAX_VALUE, count);
    }

    /** The newest {@code maxResults} values, reversed to chronological order. */
    private static List<TimedValue<?>> newestChronological(HistoryProvider history, ResourcePath path,
            int maxResults) {
        List<TimedValue<?>> timed = new ArrayList<>(history
                .streamValues(HistoryQuery.builder(path).order(SortOrder.DESCENDING).build(), maxResults).toList());
        Collections.reverse(timed);
        return timed;
    }
}
