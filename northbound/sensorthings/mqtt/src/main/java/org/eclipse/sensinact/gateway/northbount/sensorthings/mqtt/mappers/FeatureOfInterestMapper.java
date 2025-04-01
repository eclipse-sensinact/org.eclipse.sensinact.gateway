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
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FeatureOfInterestMapper extends FeaturesOfInterestMapper {

    private final String provider;

    public FeatureOfInterestMapper(String topicFilter, String id, ObjectMapper mapper, GatewayThread thread) {
        super(topicFilter, mapper, thread);

        int i = id.indexOf("~");
        if (i == -1)
            i = id.length();

        this.provider = id.substring(0, i);
    }

    @Override
    public Promise<Stream<FeatureOfInterest>> toPayload(AbstractResourceNotification notification) {
        // TODO what do we do about the timestamp? Do we have the wrong id mapping in
        // sensinact?
        if (provider.equals(notification.provider())) {
            return super.toPayload(notification);
        }
        return emptyStream();
    }

}
