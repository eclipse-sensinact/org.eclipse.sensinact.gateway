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
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt.SensorthingsMapper;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FeaturesOfInterestMapper extends SensorthingsMapper<FeatureOfInterest> {

    public FeaturesOfInterestMapper(final String topicFilter, final ObjectMapper mapper, final GatewayThread thread) {
        super(topicFilter, mapper, thread);
    }

    @Override
    public Promise<Stream<FeatureOfInterest>> toPayload(final ResourceDataNotification notification) {
        if ("admin".equals(notification.service()) && "location".equals(notification.resource())) {
            return this.decorate(this.getProvider(notification.provider()).map(DtoMapper::toFeatureOfInterest));
        }
        return this.emptyStream();
    }

    @Override
    protected Class<FeatureOfInterest> getPayloadType() {
        return FeatureOfInterest.class;
    }
}
