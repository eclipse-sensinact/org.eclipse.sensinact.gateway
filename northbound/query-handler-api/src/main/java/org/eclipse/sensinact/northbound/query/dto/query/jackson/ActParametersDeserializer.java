/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.query.dto.query.jackson;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.northbound.query.dto.query.AccessMethodCallParameterDTO;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.deser.std.StdNodeBasedDeserializer;
import tools.jackson.databind.type.TypeFactory;

/**
 * Handles the deserialization of method parameters
 */
public class ActParametersDeserializer extends StdNodeBasedDeserializer<Map<String, Object>> {

    public ActParametersDeserializer() {
        super(TypeFactory.createDefaultInstance().constructMapLikeType(Map.class, String.class, Object.class));
    }

    @Override
    public Map<String, Object> convert(JsonNode node, DeserializationContext ctxt) throws JacksonException {

        if (node.isNull()) {
            return Map.of();
        }

        final ObjectMapper mapper = new ObjectMapper();
        if (node.isObject()) {
            // Direct mapping
            final Map<?, ?> rawParams = mapper.convertValue(node, Map.class);
            final Map<String, Object> parameters = new HashMap<>();
            for (final Entry<?, ?> entry : rawParams.entrySet()) {
                parameters.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return parameters;
        }

        if (node.isArray()) {
            // Convert array of arguments to named arguments
            final Map<String, Object> parameters = new HashMap<>();
            for (final JsonNode item : node) {
                final AccessMethodCallParameterDTO rawParam = mapper.convertValue(item,
                        AccessMethodCallParameterDTO.class);
                parameters.put(rawParam.name, rawParam.value);
            }
            return parameters;
        }

        throw new RuntimeException("Invalid arguments list/map");
    }
}
