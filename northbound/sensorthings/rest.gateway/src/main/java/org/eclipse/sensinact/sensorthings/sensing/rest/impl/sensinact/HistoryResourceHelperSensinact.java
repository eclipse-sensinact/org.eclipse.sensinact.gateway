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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;

import tools.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

/**
 * Helper class for accessing historical observation data through the
 * {@link HistoryProvider} service published by the application. Results are
 * the newest values up to the configured result limit, in chronological
 * order; the returned count covers the full dataset minus entries removed by
 * row-level filtering.
 */
class HistoryResourceHelperSensinact {

    private HistoryResourceHelperSensinact() {
    }

    static ResultList<Observation> loadHistoricalObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resourceSnapshot,
            ICriterion filter, int localResultLimit) {
        HistoryProvider history = historyProvider(application);
        if (history == null) {
            return new ResultList<>(List.of());
        }

        ResourcePath path = new ResourcePath(resourceSnapshot.getService().getProvider().getName(),
                resourceSnapshot.getService().getName(), resourceSnapshot.getName());

        long count = history.getValueCount(path, TimeRange.ALL);
        List<TimedValue<?>> timed = newestChronological(history, path, getMaxResult(application, localResultLimit));

        List<Observation> observations = DtoMapper.toObservationList(userSession, application, mapper, uriInfo,
                expansions, filter, resourceSnapshot, timed);
        return resultList(count - (timed.size() - observations.size()), observations);
    }

    static ResultList<HistoricalLocation> loadHistoricalLocations(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions,
            ICriterion filter, ProviderSnapshot provider, int localResultLimit) {
        HistoryProvider history = historyProvider(application);
        if (history == null) {
            return new ResultList<>(List.of());
        }

        ResourcePath path = new ResourcePath(provider.getName(), "admin", "location");

        long count = history.getValueCount(path, TimeRange.ALL);
        List<TimedValue<?>> timed = newestChronological(history, path, getMaxResult(application, localResultLimit));

        List<HistoricalLocation> locations = DtoMapper.toHistoricalLocationList(userSession, application, mapper,
                uriInfo, expansions, filter, provider, timed);
        return resultList(count - (timed.size() - locations.size()), locations);
    }

    static HistoryProvider historyProvider(Application application) {
        return (HistoryProvider) application.getProperties().get("sensinact.history.service");
    }

    /** The newest {@code maxResults} values, reversed to chronological order. */
    private static List<TimedValue<?>> newestChronological(HistoryProvider history, ResourcePath path,
            int maxResults) {
        List<TimedValue<?>> timed = new ArrayList<>(history
                .streamValues(HistoryQuery.builder(path).order(SortOrder.DESCENDING).build(), maxResults).toList());
        Collections.reverse(timed);
        return timed;
    }

    private static int getMaxResult(Application application, int localResultLimit) {
        Integer maxResults = (Integer) application.getProperties().get("sensinact.history.result.limit");
        if (localResultLimit > 0) {
            maxResults = Math.min(localResultLimit, maxResults);
        }
        return maxResults;
    }

    private static <T extends Self> ResultList<T> resultList(long count, List<T> values) {
        return new ResultList<>((int) Math.min(Integer.MAX_VALUE, count), null, values);
    }
}
