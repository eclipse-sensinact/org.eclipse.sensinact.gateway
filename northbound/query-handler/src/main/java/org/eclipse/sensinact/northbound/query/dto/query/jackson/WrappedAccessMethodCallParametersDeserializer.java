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
import java.util.List;

import org.eclipse.sensinact.northbound.query.dto.query.AccessMethodCallParameterDTO;
import org.eclipse.sensinact.northbound.query.dto.query.WrappedAccessMethodCallParametersDTO;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Handles the deserialization of method parameters
 */
public class WrappedAccessMethodCallParametersDeserializer
        extends StdDeserializer<WrappedAccessMethodCallParametersDTO> {

    private static final long serialVersionUID = 1L;

    public WrappedAccessMethodCallParametersDeserializer() {
        this(null);
    }

    protected WrappedAccessMethodCallParametersDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public WrappedAccessMethodCallParametersDTO deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException, JacksonException {

        WrappedAccessMethodCallParametersDTO dto = new WrappedAccessMethodCallParametersDTO();
        if (parser.currentToken() == JsonToken.START_OBJECT) {
            if ("parameters".equals(parser.nextFieldName())) {
                // Look for the array
                parser.nextToken();
            } else {
                throw new JsonMappingException(parser, "Expected the field name to be \"parameters\"",
                        parser.currentTokenLocation());
            }
        }
        if (parser.currentToken() == JsonToken.START_ARRAY) {
            dto.parameters = parser.readValueAs(new TypeReference<List<AccessMethodCallParameterDTO>>() {
            });
        } else {
            throw new JsonMappingException(parser, "Invalid token " + parser.currentToken() + " expected ARRAY_START",
                    parser.currentTokenLocation());
        }
        return dto;
    }
}
