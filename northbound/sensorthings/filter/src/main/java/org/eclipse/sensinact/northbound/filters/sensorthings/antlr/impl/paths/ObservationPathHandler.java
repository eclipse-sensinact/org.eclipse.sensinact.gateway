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

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;

public class ObservationPathHandler {

    private final ProviderSnapshot provider;
    private final ResourceSnapshot resource;

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("datastream", this::subDatastream,
            "featureofinterest", this::subFeatureOfInterest);

    public ObservationPathHandler(final ProviderSnapshot provider, final ResourceSnapshot resource) {
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

        ExpandedObservation obs = UtilDto.getResourceField(service, "lastObservation", ExpandedObservation.class);

        switch (path) {
        case "id":
        case "@iot.id":

            if (obs != null) {
                return obs.id();
            }
            return null;
        case "result":
            if (obs != null) {
                return obs.result();
            }
            return null;

        case "resulttime":
            if (obs != null) {
                return obs.resultTime();
            }
            return null;
        case "phenomenontime":
            if (obs != null) {
                return obs.phenomenonTime();
            }
            return null;

        case "validtime":
            if (obs != null) {
                return obs.validTime();
            }
            return null;

        case "resultquality":
            if (obs != null) {
                return obs.resultQuality();
            }
            return null;
        case "properties":
            if (obs != null) {
                return obs.properties();
            }
            return null;

        default:
            throw new UnsupportedRuleException("Unexpected resource level field: " + path);
        }

    }

    private Object subDatastream(final String path) {
        return new DatastreamPathHandler(provider, resource).handle(path);
    }

    private Object subFeatureOfInterest(final String path) {
        return new FeatureOfInterestPathHandler(provider, List.of(resource)).handle(path);
    }
}
