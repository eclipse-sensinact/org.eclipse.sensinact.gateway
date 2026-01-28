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
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FeatureOfInterestPathHandlerSensorthings extends AbstractPathHandlerSensorthings {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("observations", this::subObservations);
    ObjectMapper mapper = new ObjectMapper();

    public FeatureOfInterestPathHandlerSensorthings(final ProviderSnapshot provider, SensiNactSession session) {
        super(provider, session);

    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(provider);
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

        ExpandedObservation obs = getObservationFromService(service);

        FeatureOfInterest foi = obs.featureOfInterest();
        if (foi == null) {
            return null;
        }
        switch (path) {
        case "id":
        case "@iot.id":

            return foi.id();
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
        return new ObservationPathHandlerSensorthings(provider, session).handle(path);
    }
}
