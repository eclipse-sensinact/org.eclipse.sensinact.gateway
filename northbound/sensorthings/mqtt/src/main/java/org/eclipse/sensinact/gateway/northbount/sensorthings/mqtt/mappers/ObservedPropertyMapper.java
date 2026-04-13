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
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.osgi.util.promise.Promise;

import tools.jackson.databind.ObjectMapper;

public class ObservedPropertyMapper extends ObservedPropertiesMapper {

    private String provider;
    private String service;
    private String resource;

    public ObservedPropertyMapper(String topicFilter, String id, ObjectMapper mapper, GatewayThread thread) {
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
    public Promise<Stream<ObservedProperty>> toPayload(ResourceNotification notification) {
        if (isOurResource(notification)) {
            return service == null ? getObservedPropertyProvider(getProvider(notification.provider()))
                    : super.toPayload(notification);
        }
        return emptyStream();
    }

    private boolean isOurResource(ResourceNotification notification) {
        return provider.equals(notification.provider())
                && (service == null || service.equals(notification.service()))
                && (resource == null || resource.equals(notification.resource()));
    }
}
