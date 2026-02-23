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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;

public class HistoricalLocationPathHandler {

    private final ProviderSnapshot provider;
    private final List<? extends ResourceSnapshot> resources;

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("things", this::subThings, "locations",
            this::subLocations);

    public HistoricalLocationPathHandler(final ProviderSnapshot provider, final List<? extends ResourceSnapshot> resources) {
        this.provider = provider;
        this.resources = resources;
    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        if (parts.length == 1) {
            switch (parts[0]) {
            case "id":
                // Provider
                return provider.getName();

            case "time":
                // Get time from the HistoricalLocationResourceSnapshot TimedValue
                final Optional<? extends ResourceSnapshot> resource = resources.stream().filter(this::isAdminLocation)
                        .findFirst();
                if (resource.isPresent()) {
                    return PathUtils.getResourceLevelField(provider, resource.get(), parts[0]);
                }
                return null;

            default:
                return PathUtils.getProviderLevelField(provider, resources, parts[0]);
            }
        } else {
            final Function<String, Object> handler = subPartHandlers.get(parts[0]);
            if (handler == null) {
                throw new UnsupportedRuleException("Unsupported path: " + path);
            }
            return handler.apply(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));
        }
    }

    private boolean isAdminLocation(ResourceSnapshot r) {
        return "admin".equals(r.getService().getName()) && "location".equals(r.getName());
    }

    private Object subThings(final String path) {
        return new ThingPathHandler(provider, resources).handle(path);
    }

    private Object subLocations(final String path) {
        return new LocationPathHandler(provider, resources).handle(path);
    }
}
