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
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatastreamsMapper extends SensorthingsMapper<Datastream> {

    public DatastreamsMapper(String topicFilter, ObjectMapper mapper, GatewayThread thread) {
        super(topicFilter, mapper, thread);
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(LifecycleNotification notification) {
        if (notification.resource != null) {
            // This is a resource appearing
            return getDatastream(getResource(notification.provider, notification.service, notification.resource));
        }
        return emptyStream();
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(ResourceMetaDataNotification notification) {
        return getDatastream(getResource(notification.provider, notification.service, notification.resource));
    }

    public Promise<Stream<Datastream>> toPayload(ResourceDataNotification notification) {
        if ("admin".equals(notification.service)) {
            if ("friendlyName".equals(notification.resource) || "description".equals(notification.resource)) {
                // These values are used in all Datastreams for this provider
                return mapProvider(getProvider(notification.provider), this::getDatastream);
            }
        }
        return getDatastream(getResource(notification.provider, notification.service, notification.resource));
    }

    protected Promise<Stream<Datastream>> getDatastream(Promise<ResourceSnapshot> resourceSnapshot) {
        return decorate(resourceSnapshot.map(r -> DtoMapper.toDatastream(jsonMapper, r)));
    }
}