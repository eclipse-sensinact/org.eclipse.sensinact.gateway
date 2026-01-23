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
import java.util.Map;
import java.util.function.Function;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.AnyMatch;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

public class HistoricalLocationPathHandlerSensorthings extends AbstractPathHandlerSensorthings {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("things", this::subThings, "locations",
            this::subLocations);

    public HistoricalLocationPathHandlerSensorthings(final ProviderSnapshot provider, SensiNactSession session) {
        super(provider, session);
    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot service = DtoMapperSimple.getThingService(provider);
        ServiceSnapshot serviceAdmin = DtoMapperSimple.getAdminService(provider);

        if (service == null) {
            return null; // not a historical location as it's not thing provider
        }
        if (parts.length == 1) {
            switch (parts[0]) {
            case "id":
            case "@iot.id":

                // Provider
                return provider.getName();

            case "time":
                // Get time from the HistoricalLocationResourceSnapshot TimedValue
                ResourceSnapshot resource = serviceAdmin.getResource("location");
                if (resource != null) {
                    return getResourceLevelField(provider, resource, parts[0]);
                }
                return null;

            default:
                throw new UnsupportedRuleException("Unsupported path: " + path);
            }
        } else {
            final Function<String, Object> handler = subPartHandlers.get(parts[0]);
            if (handler == null) {
                throw new UnsupportedRuleException("Unsupported path: " + path);
            }
            return handler.apply(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));
        }
    }

    public Object getResourceLevelField(final ProviderSnapshot provider, final ResourceSnapshot resource,
            final String path) {
        if ("time".equals(path)) {
            if (resource.getValue() != null)
                return resource.getValue().getTimestamp();
            return null;
        }
        throw new UnsupportedRuleException("Unexpected resource level field: " + path);

    }

    private Object subThings(final String path) {
        return new ThingPathHandlerSensorthings(provider, session).handle(path);
    }

    private Object subLocations(final String path) {
        // todo need to call from location provider with reviewed path
        return new AnyMatch(getLocationsProviderFromThing(provider).stream()
                .map(p -> new LocationPathHandlerSensorthings(p, session).handle(path)).toList());

    }
}
