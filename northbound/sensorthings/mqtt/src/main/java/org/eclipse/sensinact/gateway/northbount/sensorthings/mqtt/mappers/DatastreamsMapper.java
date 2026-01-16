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
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DatastreamsMapper extends SensorthingsMapper<Datastream> {

    public DatastreamsMapper(final String topicFilter, final ObjectMapper mapper, final GatewayThread thread) {
        super(topicFilter, mapper, thread);
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(final LifecycleNotification notification) {
        if (notification.resource() != null && notification.status() != Status.RESOURCE_DELETED) {
            // This is a resource appearing
            return this.getDatastream(this.getProvider(notification.provider()));
        }
        return this.emptyStream();
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(final ResourceMetaDataNotification notification) {
        return this.getDatastream(this.getProvider(notification.provider()));
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(final ResourceDataNotification notification) {
        // These are used in all Datastreams for this provider so all have been updated
        return this.mapProviderIfProvider(this.getProvider(notification.provider()), DtoMapperSimple::isDatastream,
                this::getDatastream, provider -> this.emptyStream());

    }

    protected Promise<Stream<Datastream>> getDatastream(final Promise<ProviderSnapshot> providerSnapshot) {
        return this.decorate(providerSnapshot.map(p -> DtoMapper.toDatastream(this.jsonMapper, p)));
    }

    @Override
    protected Class<Datastream> getPayloadType() {
        return Datastream.class;
    }

}
