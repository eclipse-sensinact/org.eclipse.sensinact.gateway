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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.sensorthing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;

import tools.jackson.databind.ObjectMapper;

/**
 * Helper class for accessing historical observation data through the
 * {@link HistoryProvider} service configured for the filter component
 */
public class HistoryResourceHelperSensorthings {

    /**
     * The page size the legacy ACT-based lookup used; kept as the fetch size
     * when no result limit is configured
     */
    private static final int LEGACY_PAGE_SIZE = 500;

    private HistoryResourceHelperSensorthings() {
    }

    public static List<ExpandedObservation> loadHistoricalObservations(ObjectMapper mapper,
            ResourceSnapshot resourceSnapshot, HistoryProvider history, int localResultLimit,
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

        if (history == null) {
            return values;
        }

        ResourcePath path = new ResourcePath(resourceSnapshot.getService().getProvider().getName(),
                resourceSnapshot.getService().getName(), resourceSnapshot.getName());
        int maxResults = localResultLimit > 0 ? localResultLimit : LEGACY_PAGE_SIZE;

        List<TimedValue<?>> timed = new ArrayList<>(history
                .streamValues(HistoryQuery.builder(path).order(SortOrder.DESCENDING).build(), maxResults).toList());
        Collections.reverse(timed);

        values.addAll(0,
                timed.stream().map(tv -> DtoMapperSimple.parseExpandObservation(mapper, tv.getValue())).toList());
        return values;
    }
}
