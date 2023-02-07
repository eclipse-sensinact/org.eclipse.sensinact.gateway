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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;

/**
 * @author thoma
 *
 */
public class ThingPathHandler {

    private final ProviderSnapshot provider;
    private final List<ResourceSnapshot> resources;

    private final Map<String, Function<String[], Object>> subPartHandlers = Map.of("datastreams", this::subDatastreams,
            "locations", this::subLocations);

    public ThingPathHandler(final ProviderSnapshot provider, final List<ResourceSnapshot> resources) {
        this.provider = provider;
        this.resources = resources;
    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        if (parts.length == 1) {
            switch (path) {
            case "id":
                // Provider
                return provider.getName();

            default:
                return PathUtils.getProviderLevelField(provider, resources, path);
            }
        } else {
            final Function<String[], Object> handler = subPartHandlers.get(parts[0]);
            if (handler == null) {
                throw new UnsupportedRuleException("Unsupported path: " + path);
            }
            return handler.apply(Arrays.copyOfRange(parts, 1, parts.length));
        }
    }

    private Object subDatastreams(final String[] parts) {
        if (parts.length == 1) {
            switch (parts[0]) {
            case "id":
                // Any datastream with ID Provider~Service~Resource
                return new MultiMatch<String>(resources.stream()
                        .map(r -> String.join("~", provider.getName(), r.getService().getName(), r.getName()))
                        .collect(Collectors.toList()));

            default:
                return new MultiMatch<Object>(resources.stream()
                        .map(r -> PathUtils.getResourceLevelField(provider, r, parts[0])).collect(Collectors.toList()));
            }
        } else {
            switch (parts[0]) {
            case "thing":
                // Back to the top
                return handle(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));

            default:
                throw new UnsupportedRuleException(
                        "Unexpected field in Observation Datastream: " + String.join("/", parts));
            }
        }
    }

    private Object subLocations(final String[] parts) {
        if (parts.length == 1) {
            switch (parts[0]) {
            case "id":
                // Provider
                return provider.getName();

            default:
                return PathUtils.getProviderLevelField(provider, resources, parts[0]);
            }
        } else {
            throw new UnsupportedRuleException(
                    "Unexpected field in Observation Datastream: " + String.join("/", parts));
        }
    }
}
