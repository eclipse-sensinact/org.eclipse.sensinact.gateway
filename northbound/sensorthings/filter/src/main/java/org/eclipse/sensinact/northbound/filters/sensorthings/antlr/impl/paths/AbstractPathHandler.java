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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;

public class AbstractPathHandler {

    protected final ProviderSnapshot provider;
    protected final SensiNactSession session;

    public AbstractPathHandler(final ProviderSnapshot provider, SensiNactSession session) {
        this.provider = provider;
        this.session = session;
    }

    public ProviderSnapshot getThingProviderFromDatastream(ProviderSnapshot datastremaProvider) {
        ServiceSnapshot service = UtilDto.getDatastreamService(datastremaProvider);
        String thingId = UtilDto.getResourceField(service, "thingId", String.class);

        return session.providerSnapshot(thingId, EnumSet.noneOf(SnapshotOption.class));
    }

    public List<ProviderSnapshot> getDatastreamsProviderFromThing(ProviderSnapshot thingProvider) {
        ServiceSnapshot service = UtilDto.getThingService(thingProvider);
        List<?> datastreamIds = UtilDto.getResourceField(service, "datastreamIds", List.class);

        return datastreamIds.stream()
                .map(id -> session.providerSnapshot((String) id, EnumSet.noneOf(SnapshotOption.class))).toList();
    }

    public List<ProviderSnapshot> getLocationsProviderFromThing(ProviderSnapshot thingProvider) {
        ServiceSnapshot service = UtilDto.getThingService(thingProvider);
        List<?> locationIds = UtilDto.getResourceField(service, "locationIds", List.class);
        return locationIds.stream()
                .map(id -> session.providerSnapshot((String) id, EnumSet.noneOf(SnapshotOption.class))).toList();
    }
}
