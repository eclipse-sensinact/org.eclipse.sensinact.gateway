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

import org.eclipse.sensinact.prototype.annotation.verb.ACT;
import org.eclipse.sensinact.prototype.annotation.verb.ActParam;
import org.eclipse.sensinact.prototype.twin.TimedValue;

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
    TimedValue<?> getSingleValue(@ActParam(name = "provider") String provider,
            @ActParam(name = "service") String service, @ActParam(name = "resource") String resource,
            @ActParam(name = "time") ZonedDateTime time);

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
     * @return A {@link List&lt;TimedValue&gt;} of results in chronological order.
     *         There will be a maximum of 500 data values in the list. If more than
     *         500 data points exist then the 501st value in the list will be a
     *         {@link TimedValue} with a null timestamp and value to indicate that
     *         the data continues.
     */
    @ACT(model = "sensiNactHistory", service = "history", resource = "range")
    List<TimedValue<?>> getValueRange(@ActParam(name = "provider") String provider,
            @ActParam(name = "service") String service, @ActParam(name = "resource") String resource,
            @ActParam(name = "fromTime") ZonedDateTime fromTime, @ActParam(name = "toTime") ZonedDateTime toTime);
}
