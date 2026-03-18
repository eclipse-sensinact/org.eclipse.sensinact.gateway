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
package org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.mappers;

import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObservationsMapper extends SensorthingsMapper<Observation> {

    public ObservationsMapper(String topicFilter, ObjectMapper mapper, GatewayThread thread) {
        super(topicFilter, mapper, thread);
    }

    @Override
    public Promise<Stream<Observation>> toPayload(ResourceDataNotification notification) {
        return getObservation(getResource(notification.provider(), notification.service(), notification.resource()));

    }

    protected Promise<Stream<Observation>> getObservation(Promise<ResourceSnapshot> resourceSnapshot) {
        return decorate(resourceSnapshot.map(r -> {
            ProviderSnapshot p = r.getService().getProvider();
            if (DtoMapperSimple.isSensorthingModel(p)) {
                if (DtoMapperSimple.isDatastream(p) && r.getName().equals("lastObservation"))
                    return DtoMapperSensorthing.toObservation(jsonMapper, r);
            } else {
                return DtoMapperSensinact.toObservation(r.getService().getProvider().getName(),
                        r.getService().getName(), r.getName(), r.getValue());
            }
            return null;

        }));
    }

    @Override
    protected Class<Observation> getPayloadType() {
        return Observation.class;
    }
}
