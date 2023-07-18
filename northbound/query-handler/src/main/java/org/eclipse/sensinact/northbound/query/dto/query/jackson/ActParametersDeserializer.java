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
package org.eclipse.sensinact.northbound.query.dto.query.jackson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.northbound.query.dto.query.AccessMethodCallParameterDTO;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Handles the deserialization of method parameters
 */
public class ActParametersDeserializer extends StdDeserializer<Map<String, Object>> {

    private static final long serialVersionUID = 1L;

    public ActParametersDeserializer() {
        this(null);
    }

    protected ActParametersDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public Map<String, Object> deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException, JacksonException {

        final JsonNode node = parser.getCodec().readTree(parser);
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

        throw new IOException("Invalid arguments list/map");
    }
}
