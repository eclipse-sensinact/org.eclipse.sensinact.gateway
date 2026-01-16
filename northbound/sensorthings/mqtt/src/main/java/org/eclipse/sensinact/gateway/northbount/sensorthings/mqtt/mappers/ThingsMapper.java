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
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ThingsMapper extends SensorthingsMapper<Thing> {

    public ThingsMapper(final String topicFilter, final ObjectMapper mapper, final GatewayThread thread) {
        super(topicFilter, mapper, thread);
    }

    @Override
    public Promise<Stream<Thing>> toPayload(final LifecycleNotification notification) {
        if (notification.service() == null && notification.status() != Status.PROVIDER_DELETED) {
            // This is a provider appearing
            return this.getThing(notification.provider());
        }
        return this.emptyStream();
    }

    @Override
    public Promise<Stream<Thing>> toPayload(final ResourceDataNotification notification) {
        if ("admin".equals(notification.service())) {
            if ("friendlyName".equals(notification.resource()) || "description".equals(notification.resource())) {
                // These data values are used in the Thing
                return this.getThing(notification.provider());
            }
        }
        return this.emptyStream();
    }

    private Promise<Stream<Thing>> getThing(final String id) {
        return this.decorate(this.getProvider(id).filter(DtoMapperSimple::isThing)
                .map(p -> DtoMapper.toThing(this.jsonMapper, p)));
    }

    @Override
    protected Class<Thing> getPayloadType() {
        return Thing.class;
    }
}
