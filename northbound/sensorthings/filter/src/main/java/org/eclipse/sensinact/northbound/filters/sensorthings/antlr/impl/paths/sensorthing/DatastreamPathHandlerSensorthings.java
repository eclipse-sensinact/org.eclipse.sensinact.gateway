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
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.UnsupportedRuleException;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

public class DatastreamPathHandlerSensorthings extends AbstractPathHandlerSensorthings {

    private final Map<String, Function<String, Object>> subPartHandlers = Map.of("observations", this::subObservations,
            "observedproperty", this::subObservedProperty, "sensor", this::subSensor, "thing", this::subThing);

    public DatastreamPathHandlerSensorthings(final ProviderSnapshot provider, SensiNactSession session) {
        super(provider, session);
    }

    public Object handle(final String path) {
        final String[] parts = path.toLowerCase().split("/");
        ServiceSnapshot serviceAdmin = DtoMapperSimple.getAdminService(provider);
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(provider);

        if (service == null) {
            return null; // not a historical location as it's not thing provider
        }
        if (parts.length == 1) {

            return getResourceLevelField(provider, serviceAdmin, parts[0]);

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
            return provider.getName();

        case "name":
            return DtoMapperSimple.getResourceField(service, "friendlyName", String.class);

        case "description":
            return DtoMapperSimple.getResourceField(service, "description", String.class);
        case "observedArea":
            return DtoMapperSimple.getResourceField(service, "location", GeoJsonObject.class);
        default:
            throw new UnsupportedRuleException("Unexpected resource level field: " + path);
        }

    }

    private Object subObservations(final String path) {
        return new ObservationPathHandlerSensorthings(provider, session).handle(path);
    }

    private Object subObservedProperty(final String path) {
        return new ObservedPropertyPathHandlerSensorthings(provider, session).handle(path);
    }

    private Object subSensor(final String path) {
        return new SensorPathHandlerSensorthings(provider, session).handle(path);
    }

    private Object subThing(final String path) {

        return new ThingPathHandlerSensorthings(getThingProviderFromDatastream(provider), session).handle(path);
    }
}
