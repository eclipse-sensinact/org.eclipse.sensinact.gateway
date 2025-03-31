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
package org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.AbstractResourceNotification;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PropertyMapper extends SensorthingsMapper<JsonNode> {

    private final SensorthingsMapper<?> mapper;
    private final Set<String> property;
    private final String provider;
    private final String service;
    private final String resource;

    public PropertyMapper(String topicFilter, String property, String provider, String service, String resource,
            SensorthingsMapper<?> mapper, ObjectMapper jsonMapper, GatewayThread thread) {
        super(topicFilter, jsonMapper, thread);
        this.provider = provider;
        this.service = service;
        this.resource = resource;
        this.mapper = mapper;
        this.property = Set.of(property);
    }

    @Override
    public Promise<Stream<JsonNode>> toPayload(AbstractResourceNotification notification) {
        if (match(provider, notification.provider()) && match(service, notification.service())
                && match(resource, notification.resource())) {
            return mapper.toPayload(notification)
                    .map(s -> s.map(o -> jsonMapper.convertValue(o, ObjectNode.class).retain(property)));
        }
        return emptyStream();
    }

    private boolean match(String require, String value) {
        return require == null || require.equals(value);
    }

    @Override
    protected Class<JsonNode> getPayloadType() {
        return JsonNode.class;
    }
}
