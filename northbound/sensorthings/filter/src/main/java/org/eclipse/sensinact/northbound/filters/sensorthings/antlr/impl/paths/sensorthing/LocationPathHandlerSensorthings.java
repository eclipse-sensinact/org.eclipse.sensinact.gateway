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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.PathHandler.PathContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

public class LocationPathHandlerSensorthings extends AbstractPathHandlerSensorthings {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("things", this::subThings,
            "historicallocations", this::subHistoricalLocations);

    public LocationPathHandlerSensorthings(final PathContext pathContext) {
        super(pathContext);
    }

    public Object handle(final String path) {
        ProviderSnapshot provider = pathContext.provider();
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot service = DtoMapperSimple.getLocationService(provider);
        ServiceSnapshot serviceAdmin = DtoMapperSimple.getAdminService(provider);

        if (service == null) {
            return null;
        }
        if (parts.length == 1) {
            switch (path) {
            case "id":
            case "@iot.id":

                return provider.getName();
            case "name":
                return DtoMapperSimple.getResourceField(serviceAdmin, "name", String.class);

            case "description":
                return DtoMapperSimple.getResourceField(serviceAdmin, "description", String.class);

            case "location":
                return DtoMapperSimple.getResourceField(serviceAdmin, "location", GeoJsonObject.class);
            case "encodingType":
                return DtoMapperSimple.getResourceField(service, "encodingType", String.class);

            case "properties":
                return DtoMapperSimple.getResourceField(service, "properties", Map.class);

            default:
                throw new UnsupportedRuleException("Unexpected resource level field: " + path);
            }

        } else {
            final Function<String, Object> handler = subPartHandlers.get(parts[0]);
            if (handler == null) {
                throw new UnsupportedRuleException("Unsupported path: " + path);
            }
            return handler.apply(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));
        }
    }

    private Object subThings(final String path) {
        SensiNactSession session = pathContext.session();
        ProviderSnapshot provider = pathContext.provider();
        List<ProviderSnapshot> thingProviders = session.filteredSnapshot(null).stream()
                .map(DtoMapperSimple::getThingService).filter(Objects::nonNull).filter(s -> DtoMapperSimple
                        .getResourceField(s, "locationIds", List.class).contains(provider.getName()))
                .map(s -> s.getProvider()).toList();
        return thingProviders.stream()
                .map(p -> new PathContext(pathContext.mapper(), p, pathContext.session(), pathContext.resource(),
                        pathContext.configProperties(), pathContext.cacheObs(), pathContext.cacheHl()))
                .map(pc -> new ThingPathHandlerSensorthings(pc).handle(path)).toList();
    }

    private Object subHistoricalLocations(final String path) {
        SensiNactSession session = pathContext.session();
        ProviderSnapshot provider = pathContext.provider();

        List<ProviderSnapshot> thingProviders = session.filteredSnapshot(null).stream()
                .map(DtoMapperSimple::getThingService).filter(Objects::nonNull).filter(s -> DtoMapperSimple
                        .getResourceField(s, "locationIds", List.class).contains(provider.getName()))
                .map(s -> s.getProvider()).toList();
        return thingProviders.stream()
                .map(p -> new PathContext(pathContext.mapper(), p, pathContext.session(), pathContext.resource(),
                        pathContext.configProperties(), pathContext.cacheObs(), pathContext.cacheHl()))

                .map(pc -> new HistoricalLocationPathHandlerSensorthings(pc).handle(path)).toList();

    }
}
