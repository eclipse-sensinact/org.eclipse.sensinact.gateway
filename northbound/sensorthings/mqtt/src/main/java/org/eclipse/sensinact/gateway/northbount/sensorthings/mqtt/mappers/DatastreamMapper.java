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
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.osgi.util.promise.Promise;

import tools.jackson.databind.ObjectMapper;

public class DatastreamMapper extends DatastreamsMapper {

    private String provider;
    private String service;
    private String resource;

    public DatastreamMapper(String topicFilter, String id, ObjectMapper mapper, GatewayThread thread) {
        super(topicFilter, mapper, thread);

        String[] segments = id.split("~");

        if (segments.length != 3) {
            this.provider = segments[0];
        } else {
            this.provider = segments[0];
            this.service = segments[1];
            this.resource = segments[2];
        }
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(ResourceNotification notification) {
        if (provider.equals(notification.provider())) {
            return super.toPayload(notification);
        }
        return emptyStream();
    }

    private boolean isOurResource(ResourceNotification notification) {
        return provider.equals(notification.provider())
                && (service == null || service.equals(notification.service()))
                && (resource == null || resource.equals(notification.resource()));
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(LifecycleNotification notification) {
        // Force the required datastream when it appears
        return isOurResource(notification) && notification.status() == Status.RESOURCE_CREATED ? getDatastream()
                : emptyStream();
    }

    private Promise<Stream<Datastream>> getDatastream() {
        return service != null ? getDatastreamResource(getResource(provider, service, resource))
                : getDatastreamProvider(getProvider(provider));
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(ResourceDataNotification notification) {
        // Force the required datastream if a relevant admin topic changes
        return isOurResource(notification) ? getDatastream() : emptyStream();
    }

    @Override
    public Promise<Stream<Datastream>> toPayload(ResourceMetaDataNotification notification) {
        // Force the required datastream
        return isOurResource(notification) ? getDatastream() : emptyStream();
    }

}
