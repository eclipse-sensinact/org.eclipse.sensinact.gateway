/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import java.util.EnumSet;
import java.util.List;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.PathHandler.PathContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AbstractPathHandlerSensorthings {

    private static final String SENSINACT_HISTORY_PROVIDER = "sensinact.history.provider";
    private static final String SENSINACT_HISTORY_MAX_RESULT = "sensinact.history.max.result";
    protected PathContext pathContext;

    public AbstractPathHandlerSensorthings(final PathContext pathContext) {
        this.pathContext = pathContext;

    }

    public static List<ExpandedObservation> getListExpandedObservationWithHistory(PathContext pathContext) {
        ProviderSnapshot provider = pathContext.provider();
        // get list of observation resource
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(provider);
        ResourceSnapshot resource = service.getResource("lastObservation");
        String historyProvider = getHistoryProvider(pathContext);
        int maxResult = getMaxResult(pathContext);
        List<ExpandedObservation> listHistory = HistoryResourceHelperSensorthings.loadHistoricalObservations(
                pathContext.session(), pathContext.mapper(), resource, historyProvider, maxResult,
                pathContext.cacheObs());
        return listHistory;
    }

    public ProviderSnapshot getThingProviderFromDatastream(ProviderSnapshot datastremaProvider) {
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(datastremaProvider);
        if (service == null) {
            return null;
        }
        String thingId = DtoMapperSimple.getResourceField(service, "thingId", String.class);

        return pathContext.session().providerSnapshot(thingId, EnumSet.noneOf(SnapshotOption.class));
    }

    protected static int getMaxResult(PathContext pathContext) {
        if (pathContext.configProperties() != null
                && pathContext.configProperties().containsKey(SENSINACT_HISTORY_MAX_RESULT)) {
            int maxResult = (int) pathContext.configProperties().get(SENSINACT_HISTORY_MAX_RESULT);
            return maxResult;
        }
        return 0;
    }

    protected static String getHistoryProvider(PathContext pathContext) {
        if (pathContext.configProperties() != null
                && pathContext.configProperties().containsKey(SENSINACT_HISTORY_PROVIDER)) {
            String historyProvider = (String) pathContext.configProperties().get(SENSINACT_HISTORY_PROVIDER);
            return historyProvider;
        }
        return null;
    }

    public List<ProviderSnapshot> getDatastreamsProviderFromThing(ProviderSnapshot thingProvider) {
        ServiceSnapshot service = DtoMapperSimple.getThingService(thingProvider);
        if (service == null) {
            return List.of();
        }
        List<?> datastreamIds = DtoMapperSimple.getResourceField(service, "datastreamIds", List.class);

        return datastreamIds.stream()
                .map(id -> pathContext.session().providerSnapshot((String) id, EnumSet.noneOf(SnapshotOption.class)))
                .toList();
    }

    protected ExpandedObservation getObservationFromResource(final ResourceSnapshot resource) {
        String obsStr = DtoMapperSimple.getResourceField(resource.getService(), "lastObservation", String.class);
        ExpandedObservation obs;
        try {
            obs = pathContext.mapper().readValue(obsStr, ExpandedObservation.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return obs;
    }

    public List<ProviderSnapshot> getLocationsProviderFromThing(ProviderSnapshot thingProvider) {
        ServiceSnapshot service = DtoMapperSimple.getThingService(thingProvider);
        if (service == null) {
            return List.of();
        }
        List<?> locationIds = DtoMapperSimple.getResourceField(service, "locationIds", List.class);
        return locationIds.stream()
                .map(id -> pathContext.session().providerSnapshot((String) id, EnumSet.noneOf(SnapshotOption.class)))
                .toList();
    }
}
