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
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.PathHandler.PathContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;

public class ObservationPathHandlerSensorthings extends AbstractPathHandlerSensorthings {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("datastream", this::subDatastream,
            "featureofinterest", this::subFeatureOfInterest);

    private ExpandedObservation obs;

    public ObservationPathHandlerSensorthings(final PathContext pathContext, ExpandedObservation obs) {
        super(pathContext);
        this.obs = obs;
    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        ProviderSnapshot provider = pathContext.provider();
        // get list of resource from history as live observation

        if (parts.length == 1) {
            return getResourceLevelField(provider, obs, parts[0]);

        } else {
            final Function<String, Object> handler = subPartHandlers.get(parts[0]);
            if (handler == null) {
                throw new UnsupportedRuleException("Unsupported path: " + path);
            }
            return handler.apply(String.join("/", Arrays.copyOfRange(parts, 1, parts.length)));
        }
    }

    public Object getResourceLevelField(final ProviderSnapshot provider, final ExpandedObservation obs,
            final String path) {

        if (obs == null) {
            return null;
        }
        switch (path) {
        case "id":
        case "@iot.id":

            return obs.id();

        case "result":
            return obs.result();

        case "resulttime":
            return obs.resultTime();

        case "phenomenontime":

            return obs.phenomenonTime();

        case "validtime":
            return obs.validTime();

        case "resultquality":
            return obs.resultQuality();

        case "properties":
            return obs.properties();

        default:
            throw new UnsupportedRuleException("Unexpected resource level field: " + path);
        }

    }

    private Object subDatastream(final String path) {

        return new DatastreamPathHandlerSensorthings(pathContext).handle(path);
    }

    private Object subFeatureOfInterest(final String path) {
        return new FeatureOfInterestPathHandlerSensorthings(pathContext, obs).handle(path);
    }
}
