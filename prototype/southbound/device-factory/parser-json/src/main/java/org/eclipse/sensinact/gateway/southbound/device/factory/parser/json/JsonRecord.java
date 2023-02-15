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
package org.eclipse.sensinact.gateway.southbound.device.factory.parser.json;

import java.awt.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Device factory JSON record handler
 */
public class JsonRecord implements IDeviceMappingRecord {

    /**
     * Root JSON node (can be an array or an object)
     */
    private final JsonNode root;

    /**
     * Object mapper configured by the parent
     */
    private final ObjectMapper mapper;

    public JsonRecord(final ObjectMapper mapper, final JsonNode root) {
        if (!root.isObject() && !root.isArray()) {
            throw new IllegalArgumentException("Root node is neither a JSON object nor array");
        }

        this.mapper = mapper;
        this.root = root;
    }

    /**
     * Get the value at the given sub path
     *
     * @param node     Current node
     * @param pathPart Direct path in the node
     * @return The value at that path
     */
    private JsonNode getPath(JsonNode node, RecordPath pathPart) {
        if (node.isArray()) {
            return node.get(pathPart.asInt());
        }

        return node.get(pathPart.asString());
    }

    /**
     * Walks down the given path
     */
    private JsonNode walkPath(RecordPath path) {
        JsonNode current = root;
        for (RecordPath part : path.parts()) {
            current = getPath(current, part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private Object getRawField(RecordPath field) {
        final JsonNode node = walkPath(field);
        if (node == null || node.isNull()) {
            if (field.hasDefaultValue()) {
                return field.getDefaultValue();
            } else {
                return null;
            }
        }

        if (node.isObject()) {
            return mapper.convertValue(node, Map.class);
        }

        if (node.isArray()) {
            return mapper.convertValue(node, List.class);
        }

        switch (node.getNodeType()) {
        case NUMBER:
            if (node.isDouble()) {
                return node.asDouble();
            } else {
                return node.asInt();
            }

        case STRING:
            return node.asText();

        case BOOLEAN:
            return node.asBoolean();

        default:
            return null;
        }
    }

    @Override
    public Object getField(RecordPath field) {
        final Object rawValue = getRawField(field);
        if (rawValue == null) {
            return null;
        }

        return field.convertValue(rawValue);
    }

    @Override
    public String getFieldString(RecordPath field) {
        final JsonNode node = walkPath(field);
        if (node != null && node.isValueNode()) {
            return node.asText();
        }

        if (field.hasDefaultValue()) {
            final Object defaultValue = field.getDefaultValue();
            return defaultValue != null ? String.valueOf(defaultValue) : null;
        } else {
            return null;
        }
    }
}
