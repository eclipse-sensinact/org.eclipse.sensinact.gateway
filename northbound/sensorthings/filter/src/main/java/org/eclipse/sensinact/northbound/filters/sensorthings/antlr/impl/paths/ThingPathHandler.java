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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.AnyMatch;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;

public class ThingPathHandler extends AbstractPathHandler {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("datastreams", this::subDatastreams,
            "locations", this::subLocations);

    public ThingPathHandler(final ProviderSnapshot provider, SensiNactSession session) {
        super(provider, session);

    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot service = UtilDto.getThingService(provider);
        ServiceSnapshot serviceAdmin = UtilDto.getAdminService(provider);

        if (service == null) {
            return null;
        }
        if (parts.length == 1) {
            switch (path) {
            case "id":
            case "@iot.id":

                return provider.getName();
            case "name":
                return UtilDto.getResourceField(serviceAdmin, "friendlyName", String.class);

            case "description":
                return UtilDto.getResourceField(serviceAdmin, "description", String.class);

            case "properties":
                return UtilDto.getResourceField(service, "properties", Map.class);

            case "location":
                return UtilDto.getResourceField(serviceAdmin, "location", GeoJsonObject.class);

            default:
                throw new UnsupportedRuleException("Unexpected resource level field: " + path);
            }

        } else {
            if (parts[0].equalsIgnoreCase("Locations") && parts[1].equalsIgnoreCase("location")) {
                return UtilDto.getResourceField(serviceAdmin, "location", GeoJsonObject.class);
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

        return new AnyMatch(getDatastreamsProviderFromThing(provider).stream()
                .map(p -> new DatastreamPathHandler(p, session).handle(path)).collect(Collectors.toList()));

    }

    private Object subLocations(final String path) {

        return new AnyMatch(getLocationsProviderFromThing(provider).stream()
                .map(p -> new LocationPathHandler(p, session).handle(path)).collect(Collectors.toList()));
    }
}
