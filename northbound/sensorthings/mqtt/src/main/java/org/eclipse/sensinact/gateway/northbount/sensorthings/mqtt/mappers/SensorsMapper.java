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
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification.Status;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SensorsMapper extends SensorthingsMapper<Sensor> {

    public SensorsMapper(String topicFilter, ObjectMapper mapper, GatewayThread thread) {
        super(topicFilter, mapper, thread);
    }

    @Override
    public Promise<Stream<Sensor>> toPayload(LifecycleNotification notification) {
        if (notification.resource() != null && notification.status() != Status.RESOURCE_DELETED) {
            // This is a resource appearing
            return getSensorResource(
                    getResource(notification.provider(), notification.service(), notification.resource()));
        }
        return emptyStream();
    }

    @Override
    public Promise<Stream<Sensor>> toPayload(ResourceMetaDataNotification notification) {
        return getSensorResource(getResource(notification.provider(), notification.service(), notification.resource()));
    }

    protected Promise<Stream<Sensor>> getSensorProvider(Promise<ProviderSnapshot> providerSnapshot) {
        return decorate(providerSnapshot.map(p -> {
            if (DtoMapperSimple.isSensorthingModel(p)) {
                if (DtoMapperSimple.isSensor(p))
                    return DtoMapperSensorthing.toSensor(p);
            }
            return null;
        }));
    }

    protected Promise<Stream<Sensor>> getSensorResource(Promise<ResourceSnapshot> resourceSnapshot) {
        return decorate(resourceSnapshot.map(r -> {
            ProviderSnapshot p = r.getService().getProvider();
            if (DtoMapperSimple.isSensorthingModel(p)) {
                if (DtoMapperSimple.isSensor(p))
                    return DtoMapperSensorthing.toSensor(r);
            } else {
                return DtoMapperSensinact.toSensor(r);
            }
            return null;

        }));
    }

    @Override
    protected Class<Sensor> getPayloadType() {
        return Sensor.class;
    }
}
