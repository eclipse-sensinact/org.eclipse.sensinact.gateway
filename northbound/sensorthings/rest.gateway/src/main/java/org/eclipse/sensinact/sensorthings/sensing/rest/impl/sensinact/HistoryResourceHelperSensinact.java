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
import java.util.Set;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryCapability;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.eclipse.sensinact.northbound.filters.sensorthings.TimeRangeExtractor;
import org.eclipse.sensinact.northbound.filters.sensorthings.TimeRangeExtractor.Constraints;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.PaginationConstants;

import tools.jackson.databind.ObjectMapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;

/**
 * Helper class for accessing historical observation data through the
 * {@link HistoryProvider} service published by the application.
 *
 * When the request carries an effective $top, at most a single time-field
 * $orderby and no $filter — or one that reduces entirely to a time range plus,
 * capability permitting, numeric value conditions — the pagination and the
 * constraints are pushed down into the history query and
 * {@link PaginationConstants#PAGINATION_APPLIED} tells the response-side
 * query option filters to leave the page alone. Otherwise the results are the
 * newest values up to the configured result limit, in chronological order,
 * paginated in memory by the filters; the returned count covers the full
 * dataset minus entries removed by row-level filtering.
 */
class HistoryResourceHelperSensinact {

    /** $orderby fields of an Observation carrying the history timestamp */
    private static final Set<String> OBSERVATION_TIME_FIELDS = Set.of("phenomenonTime", "resultTime");

    /** $filter fields of an Observation carrying the stored value */
    private static final Set<String> OBSERVATION_VALUE_FIELDS = Set.of("result");

    /** $orderby fields of a HistoricalLocation carrying the history timestamp */
    private static final Set<String> HISTORICAL_LOCATION_TIME_FIELDS = Set.of("time");

    private HistoryResourceHelperSensinact() {
    }

    static ResultList<Observation> loadHistoricalObservations(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext,
            ExpansionSettings expansions, ResourceSnapshot resourceSnapshot, ICriterion filter, int localResultLimit) {
        HistoryProvider history = historyProvider(application);
        if (history == null) {
            return new ResultList<>(List.of());
        }

        ResourcePath path = new ResourcePath(resourceSnapshot.getService().getProvider().getName(),
                resourceSnapshot.getService().getName(), resourceSnapshot.getName());

        HistoryQuery pushed = pushDownQuery(history, path, requestContext, filter, OBSERVATION_TIME_FIELDS,
                OBSERVATION_VALUE_FIELDS, getMaxResult(application, localResultLimit));
        if (pushed != null) {
            HistoryPage page = history.getValues(pushed);
            List<Observation> observations = DtoMapper.toObservationList(userSession, application, mapper, uriInfo,
                    expansions, filter, resourceSnapshot, page.values());
            return pushedDownResultList(history, pushed, requestContext, page, observations);
        }

        long count = history.getValueCount(path, TimeRange.ALL);
        List<TimedValue<?>> timed = newestChronological(history, path, getMaxResult(application, localResultLimit));

        List<Observation> observations = DtoMapper.toObservationList(userSession, application, mapper, uriInfo,
                expansions, filter, resourceSnapshot, timed);
        return resultList(count - (timed.size() - observations.size()), observations);
    }

    static ResultList<HistoricalLocation> loadHistoricalLocations(SensiNactSession userSession,
            Application application, ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext,
            ExpansionSettings expansions, ICriterion filter, ProviderSnapshot provider, int localResultLimit) {
        HistoryProvider history = historyProvider(application);
        if (history == null) {
            return new ResultList<>(List.of());
        }

        ResourcePath path = new ResourcePath(provider.getName(), "admin", "location");

        HistoryQuery pushed = pushDownQuery(history, path, requestContext, filter, HISTORICAL_LOCATION_TIME_FIELDS,
                Set.of(), getMaxResult(application, localResultLimit));
        if (pushed != null) {
            HistoryPage page = history.getValues(pushed);
            List<HistoricalLocation> locations = DtoMapper.toHistoricalLocationList(userSession, application, mapper,
                    uriInfo, expansions, filter, provider, page.values());
            return pushedDownResultList(history, pushed, requestContext, page, locations);
        }

        long count = history.getValueCount(path, TimeRange.ALL);
        List<TimedValue<?>> timed = newestChronological(history, path, getMaxResult(application, localResultLimit));

        List<HistoricalLocation> locations = DtoMapper.toHistoricalLocationList(userSession, application, mapper,
                uriInfo, expansions, filter, provider, timed);
        return resultList(count - (timed.size() - locations.size()), locations);
    }

    static HistoryProvider historyProvider(Application application) {
        return (HistoryProvider) application.getProperties().get("sensinact.history.service");
    }

    /**
     * Whether the list represents existing history data: a pushed-down page
     * can be empty (e.g. $skip beyond the dataset) while history exists, and
     * must not trigger the fall-back to the live value.
     */
    static boolean hasHistory(ResultList<?> list) {
        return !list.value().isEmpty() || (list.count() != null && list.count() > 0);
    }

    /**
     * Runs the query with $orderby, $skip, $top and the $filter's constraints
     * applied database-side when the request allows it: an effective $top, an
     * absent or single time-field $orderby, and no $filter or one that
     * reduces entirely to a time range plus — capability permitting — numeric
     * value conditions (making re-applying it to the returned rows a no-op).
     * Returns {@code null} when the request is not pushable and pagination
     * stays with the response filters.
     */
    private static HistoryQuery pushDownQuery(HistoryProvider history, ResourcePath path,
            ContainerRequestContext requestContext, ICriterion filter, Set<String> timeFields,
            Set<String> valueFields, int maxResults) {
        if (requestContext == null) {
            return null;
        }
        TimeRange range = TimeRange.ALL;
        ValueFilter valueFilter = null;
        if (filter != null) {
            String rawFilter = (String) requestContext.getProperty(IFilterConstants.PROP_FILTER_STRING);
            Constraints constraints = TimeRangeExtractor.extractConstraints(rawFilter, timeFields, valueFields)
                    .orElse(null);
            if (constraints == null) {
                return null;
            }
            if (constraints.valueFilter() != null
                    && !history.getCapabilities().contains(HistoryCapability.VALUE_FILTERING)) {
                return null;
            }
            range = constraints.range();
            valueFilter = constraints.valueFilter();
        }
        Integer top = (Integer) requestContext.getProperty(PaginationConstants.TOP_PROP);
        if (top == null) {
            return null;
        }
        SortOrder order = pushableOrder(requestContext, timeFields);
        if (order == null) {
            return null;
        }
        Integer skip = (Integer) requestContext.getProperty(PaginationConstants.SKIP_PROP);
        return HistoryQuery.builder(path).range(range).valueFilter(valueFilter).order(order)
                .offset(skip == null ? 0 : skip).limit(Math.min(top, maxResults)).build();
    }

    /**
     * The sort order matching the request's $orderby, or {@code null} when
     * the ordering cannot be pushed down. An absent $orderby is pushable: the
     * default ordering (by id) is chronological for history entities, whose
     * ids end in the fixed-length hex timestamp.
     */
    private static SortOrder pushableOrder(ContainerRequestContext requestContext, Set<String> timeFields) {
        List<String> parameters = requestContext.getUriInfo().getQueryParameters().getOrDefault("$orderby",
                List.of());
        if (parameters.isEmpty()) {
            return SortOrder.ASCENDING;
        }
        if (parameters.size() > 1 || parameters.get(0).contains(",")) {
            return null;
        }

        String clause = parameters.get(0).trim();
        SortOrder order = SortOrder.ASCENDING;
        if (clause.endsWith(" desc")) {
            order = SortOrder.DESCENDING;
            clause = clause.substring(0, clause.length() - 5).trim();
        } else if (clause.endsWith(" asc")) {
            clause = clause.substring(0, clause.length() - 4).trim();
        }
        return timeFields.contains(clause) ? order : null;
    }

    private static <T extends Self> ResultList<T> pushedDownResultList(HistoryProvider history, HistoryQuery query,
            ContainerRequestContext requestContext, HistoryPage page, List<T> values) {
        String nextLink = null;
        if (page.hasMore()) {
            long nextSkip = page.offset() + page.values().size();
            nextLink = requestContext.getUriInfo().getRequestUriBuilder().replaceQueryParam("$skip", nextSkip).build()
                    .toString();
        }
        long count = page.totalCount()
                .orElseGet(() -> history.getValueCount(query.path(), query.range(), query.valueFilter()));
        requestContext.setProperty(PaginationConstants.PAGINATION_APPLIED, Boolean.TRUE);
        return new ResultList<>((int) Math.min(Integer.MAX_VALUE, count), nextLink, values);
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
