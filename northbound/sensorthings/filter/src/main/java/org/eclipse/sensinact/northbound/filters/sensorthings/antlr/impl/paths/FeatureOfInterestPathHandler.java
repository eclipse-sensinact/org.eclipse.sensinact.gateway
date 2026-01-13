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

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.AnyMatch;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;

public class FeatureOfInterestPathHandler {

    private final ProviderSnapshot provider;
    private final List<? extends ResourceSnapshot> resources;

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("observations", this::subObservations);

    public FeatureOfInterestPathHandler(final ProviderSnapshot provider,
            final List<? extends ResourceSnapshot> resources) {
        this.provider = provider;
        this.resources = resources;
    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot serviceDatastream = UtilDto.getDatastreamService(provider);

        if (parts.length == 1) {
            return getResourceLevelField(provider, serviceDatastream, parts[0]);

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

        ExpandedObservation obs = UtilDto.getResourceField(service, "lastObservation", ExpandedObservation.class);
        FeatureOfInterest foi = obs.featureOfInterest();
        if (foi == null) {
            return null;
        }
        switch (path) {
        case "id":
        case "@iot.id":

            return String.join("~", provider.getName(), (String) obs.id(), (String) foi.id());
        case "name":
            return foi.name();
        case "description":
            return foi.description();
        case "encodingType":
            return foi.encodingType();
        case "feature":
            return foi.feature();
        default:
            throw new UnsupportedRuleException("Unexpected resource level field: " + path);
        }
    }

    private Object subObservations(final String path) {
        if (resources.size() == 1) {
            return new ObservationPathHandler(provider, resources.get(0)).handle(path);
        } else {
            return new AnyMatch(resources.stream().map(r -> new ObservationPathHandler(provider, r).handle(path))
                    .collect(Collectors.toList()));
        }
    }
}
