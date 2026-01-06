/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.filters.resource.selector.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.filters.resource.selector.api.Selection;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;

public abstract class AbstractSelectionDeserializer<T> extends StdNodeBasedDeserializer<T> {

    private static final long serialVersionUID = 2805370682011614734L;

    protected AbstractSelectionDeserializer(Class<T> clazz) {
        super(clazz);
    }

    protected String toString(JsonNode root, String key, DeserializationContext ctxt) throws JsonMappingException {
        JsonNode value = root.get(key);
        String result;
        if(value == null) {
            result = null;
        } else {
            result = switch(value.getNodeType()) {
                case NULL: yield null;
                case BOOLEAN:
                case NUMBER:
                case STRING: yield value.asText();
                default:
                    ctxt.reportPropertyInputMismatch(handledType(), key, "The %s property of a %s must be one of [String, Number, Boolean, null]; the deserializer encountered %s", key, handledType(), value.getNodeType());
                    yield null;
            };
        }
        return result;
    }

    protected String toString(JsonNode node, DeserializationContext ctxt) throws JsonMappingException {
        String result;
        if(node == null) {
            result = null;
        } else {
            result = switch(node.getNodeType()) {
            case NULL: yield null;
            case BOOLEAN:
            case NUMBER:
            case STRING: yield node.asText();
            default:
                ctxt.reportInputMismatch(handledType(), "A %s must be one of [String, Number, Boolean, null]; the deserializer encountered %s", handledType(), node.getNodeType());
                yield null;
            };
        }
        return result;
    }

    protected List<String> toListOfString(JsonNode value, DeserializationContext ctxt) throws IOException {
        if (value == null || value.isNull()) {
            return null;
        }

        if (value.isArray()) {
            final List<String> result = new ArrayList<>();
            for (int i = 0; i < value.size(); i++) {
                result.add(toString(value.get(i), ctxt));
            }
            return result;
        } else {
            return List.of(toString(value, ctxt));
        }
    }

    protected boolean toBoolean(JsonNode root, String key, DeserializationContext ctxt) throws JsonMappingException {
        JsonNode value = root.get(key);
        boolean result;
        if(value == null) {
            result = false;
        } else {
            result = switch(value.getNodeType()) {
            case NULL: yield false;
            case BOOLEAN: yield value.asBoolean();
            default:
                ctxt.reportPropertyInputMismatch(Selection.class, key, "The %s property of a %s must be a Boolean or null; the deserializer encountered %s", key, handledType(), value.getNodeType());
                yield false;
            };
        }
        return result;
    }
}
