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
package org.eclipse.sensinact.gateway.southbound.history.api;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.eclipse.sensinact.core.annotation.verb.ActParam;
import org.eclipse.sensinact.core.twin.TimedValue;

public interface HistoricalQueries {

    /**
     * Return the value that a resource had at the given time. If no exact match
     * exists then the most recent value <em>before</em> the supplied time will be
     * returned.
     *
     * @param provider
     * @param service
     * @param resource
     * @param time     the time to check. If null then the earliest possible result
     *                 will be returned
     * @return A TimedValue result. The timestamp and value will be null if no value
     *         exists prior to the supplied time
     */
    @ACT(model = "sensiNactHistory", service = "history", resource = "single")
    TimedValue<?> getSingleValue(@ActParam("provider") String provider, @ActParam("service") String service,
            @ActParam("resource") String resource, @ActParam("time") ZonedDateTime time);

    /**
     * Return a list of values that a resource had between the given times.
     *
     * All data values will have timestamps equal to or after <code>fromTime</code>
     * and equal to or before <code>toTime</code>. If <code>fromTime</code> is
     * <code>null</code> then the latest possible results will be returned. If
     * <code>toTime</code> is <code>null</code> then results will be returned up to
     * the present.
     *
     * A maximum of 500 results will be returned. If more than 500 results exist for
     * the query then the 501st result will be empty (null timestamp and value) to
     * indicate that an additional query is needed to see the full dataset.
     *
     * @param provider
     * @param service
     * @param resource
     * @param fromTime the time to start from. If <code>null</code> then the latest
     *                 values before <code>toTime</code> will be returned
     * @param toTime   the time to finish at. If <code>null</code> then there is no
     *                 finishing time limit.
     * @param skip     the number of values to skip in the result set. If fromTime
     *                 is <code>null</code> then this will be skipped from the end
     *                 not the start of the results.
     * @return A {@link List&lt;TimedValue&gt;} of results in chronological order.
     *         There will be a maximum of 500 data values in the list. If a from
     *         time is provided and more than 500 data points exist then the 501st
     *         value in the list will be a {@link TimedValue} with a null timestamp
     *         and value to indicate that the data continues.
     */
    @ACT(model = "sensiNactHistory", service = "history", resource = "range")
    List<TimedValue<?>> getValueRange(@ActParam("provider") String provider, @ActParam("service") String service,
            @ActParam("resource") String resource, @ActParam("fromTime") ZonedDateTime fromTime,
            @ActParam("toTime") ZonedDateTime toTime, @ActParam("skip") Integer skip);

    /**
     * Return a list of values that a resource had between the given times with
     * SensorThings API query parameter support for efficient database-level filtering.
     *
     * This method extends the basic range query with support for:
     * - $top (limit): Maximum number of results to return
     * - $orderby: Ordering of results (asc/desc by time)
     * - Combined with existing fromTime/toTime/skip parameters
     *
     * @param provider the provider name
     * @param service  the service name
     * @param resource the resource name
     * @param fromTime the time to start from. If <code>null</code> then the latest
     *                 values before <code>toTime</code> will be returned
     * @param toTime   the time to finish at. If <code>null</code> then there is no
     *                 finishing time limit.
     * @param skip     the number of values to skip in the result set
     * @param top      the maximum number of results to return (SensorThings $top)
     * @param orderBy  the ordering directive, "asc" or "desc" (SensorThings $orderby)
     * @return A {@link List&lt;TimedValue&gt;} of results ordered according to orderBy parameter
     */
    @ACT(model = "sensiNactHistory", service = "history", resource = "rangeFiltered")
    List<TimedValue<?>> getValueRangeFiltered(@ActParam("provider") String provider,
            @ActParam("service") String service,
            @ActParam("resource") String resource,
            @ActParam("fromTime") ZonedDateTime fromTime,
            @ActParam("toTime") ZonedDateTime toTime,
            @ActParam("skip") Integer skip,
            @ActParam("top") Integer top,
            @ActParam("orderBy") String orderBy);

    /**
     * Get the number of stored values for a given resource
     *
     * @param provider
     * @param service
     * @param resource
     * @param fromTime the time to start from. If <code>null</code> then all values
     *                 before <code>toTime</code> will be counted
     * @param toTime   the time to finish at. If <code>null</code> then there is no
     *                 finishing time limit.
     * @return
     */
    @ACT(model = "sensiNactHistory", service = "history", resource = "count")
    Long getStoredValueCount(@ActParam("provider") String provider, @ActParam("service") String service,
            @ActParam("resource") String resource, @ActParam("fromTime") ZonedDateTime fromTime,
            @ActParam("toTime") ZonedDateTime toTime);
}
