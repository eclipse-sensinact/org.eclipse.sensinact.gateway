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
import org.eclipse.sensinact.core.notification.AbstractResourceNotification;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObservationMapper extends ObservationsMapper {

    private final String provider;
    private final String service;
    private final String resource;

    public ObservationMapper(String topicFilter, String id, ObjectMapper mapper, GatewayThread thread) {
        super(topicFilter, mapper, thread);

        // TODO What do we do about the timestamp?
        String[] segments = id.split("~");

        if (segments.length != 3) {
            throw new IllegalArgumentException("The Observation id " + id + " is not valid");
        }
        this.provider = segments[0];
        this.service = segments[1];
        this.resource = segments[2];
    }

    public Promise<Stream<Observation>> toPayload(AbstractResourceNotification notification) {
        if (provider.equals(notification.provider()) && service.equals(notification.service())
                && resource.equals(notification.resource())) {
            return super.toPayload(notification);
        }
        return emptyStream();
    }
}
