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
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObservedPropertiesMapper extends SensorthingsMapper<ObservedProperty> {

    public ObservedPropertiesMapper(final String topicFilter, final ObjectMapper mapper, final GatewayThread thread) {
        super(topicFilter, mapper, thread);
    }

    @Override
    public Promise<Stream<ObservedProperty>> toPayload(final LifecycleNotification notification) {
        if (notification.resource() != null && notification.status() != Status.RESOURCE_DELETED) {
            // This is a resource appearing
            return this.getObservedProperty(this.getProvider(notification.provider()));
        }
        return this.emptyStream();
    }

    @Override
    public Promise<Stream<ObservedProperty>> toPayload(final ResourceMetaDataNotification notification) {
        return this.getObservedProperty(this.getProvider(notification.provider()));
    }

    protected Promise<Stream<ObservedProperty>> getObservedProperty(final Promise<ProviderSnapshot> providerSnapshot) {
        return this.decorate(providerSnapshot.map(DtoMapper::toObservedProperty));
    }

    @Override
    protected Class<ObservedProperty> getPayloadType() {
        return ObservedProperty.class;
    }
}
