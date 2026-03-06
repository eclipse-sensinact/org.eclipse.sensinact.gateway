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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.PathHandler.PathContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

public class ThingPathHandlerSensorthings extends AbstractPathHandlerSensorthings {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("datastreams", this::subDatastreams,
            "locations", this::subLocations);

    public ThingPathHandlerSensorthings(final PathContext pathContext) {
        super(pathContext);
    }

    public Object handle(final String path) {
        ProviderSnapshot provider = pathContext.provider();
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot service = DtoMapperSimple.getThingService(provider);
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
                return DtoMapperSimple.getResourceField(serviceAdmin, "friendlyName", String.class);

            case "description":
                return DtoMapperSimple.getResourceField(serviceAdmin, "description", String.class);

            case "properties":
                return DtoMapperSimple.getResourceField(service, "properties", Map.class);

            case "location":
                return DtoMapperSimple.getResourceField(serviceAdmin, "location", GeoJsonObject.class);
            case "time":
                return service.getResource("location") != null && service.getResource("location").getValue() != null
                        ? service.getResource("location").getValue().getTimestamp()
                        : null;

            default:
                throw new UnsupportedRuleException("Unexpected resource level field: " + path);
            }

        } else {
            if (parts[0].equalsIgnoreCase("Locations") && parts[1].equalsIgnoreCase("location")) {
                return DtoMapperSimple.getResourceField(serviceAdmin, "location", GeoJsonObject.class);
            } else {
                final Function<String, Object> handler = subPartHandlers.get(parts[0]);
                if (handler == null) {
                    throw new UnsupportedRuleException("Unsupported path: " + path);
                }
                return handler.apply(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));
            }

        }
    }

    private Object subDatastreams(final String path) {
        ProviderSnapshot provider = pathContext.provider();

        return getDatastreamsProviderFromThing(provider).stream().map(p -> withProvider(pathContext, p)).flatMap(pc -> {
            Object result = new DatastreamPathHandlerSensorthings(pc).handle(path);

            if (result instanceof List<?>) {
                return ((List<?>) result).stream();
            } else if (result != null) {
                return Stream.of(result);
            } else {
                return Stream.empty();
            }
        }).collect(Collectors.toList());
    }

    private Object subLocations(final String path) {
        ProviderSnapshot provider = pathContext.provider();
        return getLocationsProviderFromThing(provider).stream().map(p -> withProvider(pathContext, p))
                .map(pc -> new LocationPathHandlerSensorthings(pc).handle(path)).collect(Collectors.toList());
    }
}
