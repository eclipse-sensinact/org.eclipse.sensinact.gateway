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
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification.Status;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.osgi.util.promise.Promise;

import tools.jackson.databind.ObjectMapper;

public class DatastreamsMapper extends SensorthingsMapper<Datastream> {

    public DatastreamsMapper(String topicFilter, ObjectMapper mapper, GatewayThread thread) {
        super(topicFilter, mapper, thread);
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(LifecycleNotification notification) {
        if (notification.resource() != null && notification.status() != Status.RESOURCE_DELETED) {
            // This is a resource appearing
            return getDatastreamResource(
                    getResource(notification.provider(), notification.service(), notification.resource()));
        }
        return emptyStream();
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(ResourceMetaDataNotification notification) {
        return getDatastreamResource(
                getResource(notification.provider(), notification.service(), notification.resource()));
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(ResourceDataNotification notification) {
        if (isRelevantAdminResource(notification)) {
            // These are used in all Datastreams for this provider so all have been updated
            return mapProvider(getProvider(notification.provider()), this::getDatastreamResource);
        }
        return emptyStream();
    }

    protected Promise<Stream<Datastream>> getDatastreamResource(Promise<ResourceSnapshot> resourceSnapshot) {
        return decorate(resourceSnapshot.map(r -> {
            ProviderSnapshot p = r.getService().getProvider();
            if (DtoMapperSimple.isSensorthingModel(p)) {
                if (DtoMapperSimple.isDatastream(p))
                    return DtoMapperSensorthing.toDatastream(jsonMapper, r);
            } else {
                return DtoMapperSensinact.toDatastream(jsonMapper, r);
            }
            return null;

        }));
    }

    protected Promise<Stream<Datastream>> getDatastreamProvider(Promise<ProviderSnapshot> providerSnapshot) {
        return decorate(providerSnapshot.map(p -> {
            if (DtoMapperSimple.isSensorthingModel(p)) {
                if (DtoMapperSimple.isDatastream(p))
                    return DtoMapperSensorthing.toDatastream(jsonMapper, p);
            }
            return null;
        }));
    }

    @Override
    protected Class<Datastream> getPayloadType() {
        return Datastream.class;
    }

    protected boolean isRelevantAdminResource(ResourceNotification notification) {
        return "admin".equals(notification.service()) && "location".equals(notification.resource());
    }
}
