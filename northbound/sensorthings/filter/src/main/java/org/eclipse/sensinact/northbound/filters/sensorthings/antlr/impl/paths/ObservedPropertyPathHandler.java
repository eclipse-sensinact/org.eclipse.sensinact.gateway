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

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;

public class ObservedPropertyPathHandler {

    private final ProviderSnapshot provider;
    private final ResourceSnapshot resource;

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("datastreams", this::subDatastreams);

    public ObservedPropertyPathHandler(final ProviderSnapshot provider, final ResourceSnapshot resource) {
        this.provider = provider;
        this.resource = resource;
    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot service = UtilDto.getDatastreamService(provider);
        if (service == null) {
            return null;
        }
        if (parts.length == 1) {
            return getResourceLevelField(provider, service, parts[0]);

        } else {
            final Function<String, Object> handler = subPartHandlers.get(parts[0]);
            if (handler == null) {
                throw new UnsupportedRuleException("Unsupported path: " + path);
            }
            return handler.apply(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));
        }
    }

    public Object getResourceLevelField(final ProviderSnapshot provider, final ServiceSnapshot service,
            final String path) {
        switch (path) {
        case "id":
        case "@iot.id":

            String id = UtilDto.getResourceField(service, "observedPropertyId", String.class);
            if (id != null)
                return String.join("~", provider.getName(), id);
            return null;

        case "name":
            return UtilDto.getResourceField(service, "observedPropertyName", String.class);

        case "description":
            return UtilDto.getResourceField(service, "observedPropertyDescription", String.class);

        case "definition":
            return UtilDto.getResourceField(service, "observedPropertyDefinition", String.class);

        case "properties":
            return UtilDto.getResourceField(service, "observedPropertyProperties", Map.class);

        default:
            throw new UnsupportedRuleException("Unexpected resource level field: " + path);
        }

    }

    private Object subDatastreams(final String path) {
        // Only one datastream per observed property
        return new DatastreamPathHandler(provider, resource).handle(path);
    }
}
