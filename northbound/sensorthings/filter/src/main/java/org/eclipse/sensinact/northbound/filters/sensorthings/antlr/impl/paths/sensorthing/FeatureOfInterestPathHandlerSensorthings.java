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
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths.PathHandler.PathContext;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FeatureOfInterestPathHandlerSensorthings extends AbstractPathHandlerSensorthings {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("observations", this::subObservations);
    ObjectMapper mapper = new ObjectMapper();

    private ExpandedObservation obs;

    public FeatureOfInterestPathHandlerSensorthings(final PathContext pathContext, ExpandedObservation obs) {
        super(pathContext);
        this.obs = obs;
    }

    public Object handle(final String path) {
        ProviderSnapshot provider = pathContext.provider();
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(provider);
        if (service == null) {
            return null;
        }
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

        FeatureOfInterest foi = obs.featureOfInterest();
        if (foi == null) {
            return null;
        }
        String foiId = obs.featureOfInterest().id().toString();
        ProviderSnapshot providerFoi = pathContext.session().providerSnapshot(foiId,
                EnumSet.noneOf(SnapshotOption.class));
        ServiceSnapshot serviceAdmin = DtoMapperSimple.getAdminService(providerFoi);
        ServiceSnapshot service = DtoMapperSimple.getFeatureOfInterestService(providerFoi);
        if (service == null || serviceAdmin == null) {
            return null;
        }
        switch (path.toLowerCase()) {
        case "id":
        case "@iot.id":

            return providerFoi.getName();
        case "name":
            return DtoMapperSimple.getResourceField(serviceAdmin, "friendlyName", String.class);
        case "description":
            return DtoMapperSimple.getResourceField(serviceAdmin, "description", String.class);
        case "encodingtype":

            return DtoMapperSimple.getResourceField(service, "encodingType", String.class);
        case "feature":
            return DtoMapperSimple.getResourceField(serviceAdmin, "location", String.class);
        default:
            throw new UnsupportedRuleException("Unexpected resource level field: " + path);
        }
    }

    private Object subObservations(final String path) {
        // get history for the resource

        return new ObservationPathHandlerSensorthings(pathContext, obs).handle(path);
    }
}
