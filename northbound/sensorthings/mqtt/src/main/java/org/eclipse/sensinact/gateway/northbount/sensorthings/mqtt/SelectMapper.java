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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.osgi.util.promise.Promise;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SelectMapper extends SensorthingsMapper<JsonNode> {

    private final SensorthingsMapper<?> mapper;
    private final Set<String> selected;

    public SelectMapper(String topicFilter, String selectFilter, SensorthingsMapper<?> mapper, ObjectMapper jsonMapper,
            GatewayThread thread) {
        super(topicFilter, jsonMapper, thread);
        this.mapper = mapper;
        selected = Arrays.stream(selectFilter.split(",")).collect(Collectors.toSet());
    }

    @Override
    public Promise<Stream<JsonNode>> toPayload(ResourceNotification notification) {
        return mapper.toPayload(notification)
                .map(s -> s.map(o -> jsonMapper.convertValue(o, ObjectNode.class).retain(selected)));
    }

    @Override
    protected Class<JsonNode> getPayloadType() {
        return JsonNode.class;
    }
}
