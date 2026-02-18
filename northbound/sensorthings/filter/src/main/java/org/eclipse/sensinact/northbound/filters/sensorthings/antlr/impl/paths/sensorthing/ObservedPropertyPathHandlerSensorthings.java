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
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.PathHandler.PathContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

public class ObservedPropertyPathHandlerSensorthings extends AbstractPathHandlerSensorthings {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("datastreams", this::subDatastreams);

    public ObservedPropertyPathHandlerSensorthings(final PathContext pathContext) {
        super(pathContext);
    }

    public Object handle(final String path) {
        ProviderSnapshot provider = pathContext.provider();
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot service = DtoMapperSimple.getObservedPropertyService(provider);
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
        ServiceSnapshot serviceAdmin = DtoMapperSimple.getAdminService(provider);

        switch (path) {
        case "id":
        case "@iot.id":

            return provider.getName();

        case "name":
            return DtoMapperSimple.getResourceField(serviceAdmin, "friendlyName", String.class);

        case "description":
            return DtoMapperSimple.getResourceField(serviceAdmin, "description", String.class);

        case "definition":
            return DtoMapperSimple.getResourceField(service, "observedPropertyDefinition", String.class);

        case "properties":
            return DtoMapperSimple.getResourceField(service, "observedPropertyProperties", Map.class);

        default:
            throw new UnsupportedRuleException("Unexpected resource level field: " + path);
        }

    }

    private Object subDatastreams(final String path) {
        return getDatastreamsProviderFromOp(pathContext.provider()).stream()
                .map(p -> new PathContext(pathContext.mapper(), p, pathContext.session(), pathContext.resource(),
                        pathContext.configProperties(), pathContext.cacheObs(), pathContext.cacheHl()))
                .flatMap(pc -> {
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
}
