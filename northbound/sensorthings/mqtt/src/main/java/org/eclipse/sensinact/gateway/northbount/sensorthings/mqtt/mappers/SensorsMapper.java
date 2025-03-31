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
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
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
            return getSensor(getResource(notification.provider(), notification.service(), notification.resource()));
        }
        return emptyStream();
    }

    @Override
    public Promise<Stream<Sensor>> toPayload(ResourceMetaDataNotification notification) {
        return getSensor(getResource(notification.provider(), notification.service(), notification.resource()));
    }

    protected Promise<Stream<Sensor>> getSensor(Promise<ResourceSnapshot> resourceSnapshot) {
        return decorate(resourceSnapshot.map(DtoMapper::toSensor));
    }

    @Override
    protected Class<Sensor> getPayloadType() {
        return Sensor.class;
    }
}
