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
package org.eclipse.sensinact.northbound.query.dto.jackson;

import java.io.IOException;
import java.util.Set;

import org.eclipse.sensinact.northbound.query.dto.SensinactPath;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Handles a {@link SensinactPath} deserialization
 */
public class SensinactPathDeserializer extends StdDeserializer<SensinactPath> {

    private static final long serialVersionUID = 1L;

    public SensinactPathDeserializer() {
        this(null);
    }

    protected SensinactPathDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public SensinactPath deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws IOException, JacksonException {

        SensinactPath path;
        switch(parser.currentToken()) {
            case VALUE_STRING:
                path = SensinactPath.fromUri(parser.getText());
                parser.nextToken();
                break;
            case VALUE_NULL:
                path = null;
                parser.nextToken();
                break;
            case START_OBJECT:
                path = parseObject(parser);
                break;
            default:
                throw MismatchedInputException.from(ctxt, "Invalid node type " + parser.currentToken() + " expected text or null");
        }
        return path;
    }

    private SensinactPath parseObject(JsonParser parser) throws IOException {
        final SensinactPath path = new SensinactPath();

        for(;;) {
            switch(parser.nextToken()) {
                case FIELD_NAME:
                    String fieldName = parser.getText();
                    JsonToken valueType = parser.nextToken();
                    if(valueType == JsonToken.VALUE_NULL || valueType == JsonToken.VALUE_STRING) {
                        switch(fieldName) {
                            case "provider":
                                path.provider = parser.getText();
                                break;
                            case "service":
                                path.service = parser.getText();
                                break;
                            case "resource":
                                path.resource = parser.getText();
                                break;
                            case "metadata":
                                path.metadata = parser.getText();
                                break;
                            default:
                                throw UnrecognizedPropertyException.from(parser, SensinactPath.class, fieldName, Set.of("provider", "service", "resource", "metadata"));
                        }
                    } else {
                        throw MismatchedInputException.from(parser, SensinactPath.class, "Invalid value type " + parser.currentToken() + " for field " + fieldName);
                    }
                    break;
                case END_OBJECT:
                    return path;
                default:
                    throw MismatchedInputException.from(parser, SensinactPath.class, "Invalid node type " + parser.currentToken() + " expected FIELD_NAME or END_OBJECT");
            }
        }
    }
}
