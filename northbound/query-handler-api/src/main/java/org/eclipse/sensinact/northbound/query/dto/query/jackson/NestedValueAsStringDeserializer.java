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
package org.eclipse.sensinact.northbound.query.dto.query.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;

/**
 * Handles the deserialization of nested values which should be strings
 */
public class NestedValueAsStringDeserializer extends StdNodeBasedDeserializer<String> {

    private static final long serialVersionUID = 1L;

    public NestedValueAsStringDeserializer() {
        super(String.class);
    }

    @Override
    public String convert(JsonNode node, DeserializationContext ctxt) throws IOException, JacksonException {

        if (node.isNull()) {
            return null;
        } else if(node.isValueNode()) {
            return node.asText();
        } else {
            return node.toString();
        }
    }
}
