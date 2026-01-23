/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import java.util.EnumSet;
import java.util.List;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class AbstractPathHandlerSensorthings {

    protected final ProviderSnapshot provider;
    protected final SensiNactSession session;
    private ObjectMapper mapper;

    public AbstractPathHandlerSensorthings(final ProviderSnapshot provider, SensiNactSession session) {
        this.provider = provider;
        this.session = session;
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public ProviderSnapshot getThingProviderFromDatastream(ProviderSnapshot datastremaProvider) {
        ServiceSnapshot service = DtoMapperSimple.getDatastreamService(datastremaProvider);
        if (service == null) {
            return null;
        }
        String thingId = DtoMapperSimple.getResourceField(service, "thingId", String.class);

        return session.providerSnapshot(thingId, EnumSet.noneOf(SnapshotOption.class));
    }

    public List<ProviderSnapshot> getDatastreamsProviderFromThing(ProviderSnapshot thingProvider) {
        ServiceSnapshot service = DtoMapperSimple.getThingService(thingProvider);
        if (service == null) {
            return List.of();
        }
        List<?> datastreamIds = DtoMapperSimple.getResourceField(service, "datastreamIds", List.class);

        return datastreamIds.stream()
                .map(id -> session.providerSnapshot((String) id, EnumSet.noneOf(SnapshotOption.class))).toList();
    }

    protected ExpandedObservation getObservationFromService(final ServiceSnapshot service) {
        String obsStr = DtoMapperSimple.getResourceField(service, "lastObservation", String.class);
        ExpandedObservation obs;
        try {
            obs = mapper.readValue(obsStr, ExpandedObservation.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return obs;
    }

    public List<ProviderSnapshot> getLocationsProviderFromThing(ProviderSnapshot thingProvider) {
        ServiceSnapshot service = DtoMapperSimple.getThingService(thingProvider);
        if (service == null) {
            return List.of();
        }
        List<?> locationIds = DtoMapperSimple.getResourceField(service, "locationIds", List.class);
        return locationIds.stream()
                .map(id -> session.providerSnapshot((String) id, EnumSet.noneOf(SnapshotOption.class))).toList();
    }
}
