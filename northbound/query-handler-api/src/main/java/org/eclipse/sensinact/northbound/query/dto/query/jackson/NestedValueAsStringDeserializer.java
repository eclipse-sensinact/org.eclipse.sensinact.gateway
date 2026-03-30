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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdNodeBasedDeserializer;

/**
 * Handles the deserialization of nested values which should be strings
 */
public class NestedValueAsStringDeserializer extends StdNodeBasedDeserializer<String> {

    public NestedValueAsStringDeserializer() {
        super(String.class);
    }

    @Override
    public String convert(JsonNode node, DeserializationContext ctxt) throws JacksonException {

        System.out.println("Deserializing node " + node);

        if (node.isNull()) {
            return null;
        } else if(node.isValueNode()) {
            return node.asString();
        } else {
            return node.toString();
        }
    }
}
