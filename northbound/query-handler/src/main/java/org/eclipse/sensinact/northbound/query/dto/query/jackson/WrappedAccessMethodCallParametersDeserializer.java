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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.sensinact.northbound.query.dto.query.AccessMethodCallParameterDTO;
import org.eclipse.sensinact.northbound.query.dto.query.WrappedAccessMethodCallParametersDTO;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * Handles the deserialization of method parameters
 */
public class WrappedAccessMethodCallParametersDeserializer
        extends StdNodeBasedDeserializer<WrappedAccessMethodCallParametersDTO> {

    private static final long serialVersionUID = 1L;

    public WrappedAccessMethodCallParametersDeserializer() {
        super(WrappedAccessMethodCallParametersDTO.class);
    }

    @Override
    public WrappedAccessMethodCallParametersDTO convert(JsonNode node, DeserializationContext ctxt) throws IOException, JacksonException {

        WrappedAccessMethodCallParametersDTO dto = new WrappedAccessMethodCallParametersDTO();

        if (node.getNodeType() == JsonNodeType.OBJECT) {
            Iterator<Entry<String, JsonNode>> fieldNames = node.fields();
            while(fieldNames.hasNext()) {
                Entry<String, JsonNode> entry = fieldNames.next();
                if ("parameters".equals(entry.getKey())) {
                    // Look for the array
                    node = entry.getValue();
                } else {
                    throw MismatchedInputException.from(ctxt, "Expected a single field named \"parameters\" but found " + entry.getKey());
                }
            }
        }
        if (node.getNodeType() == JsonNodeType.ARRAY) {
            dto.parameters = ctxt.readTreeAsValue(node,
                    ctxt.getTypeFactory().constructCollectionLikeType(List.class, AccessMethodCallParameterDTO.class));
        } else {
            throw MismatchedInputException.from(ctxt, "Invalid node " + node.getNodeType() + " expected ARRAY");
        }
        return dto;
    }
}
