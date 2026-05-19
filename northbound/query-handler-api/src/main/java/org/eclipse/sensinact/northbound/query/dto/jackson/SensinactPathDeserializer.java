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

import java.util.Set;

import org.eclipse.sensinact.northbound.query.dto.SensinactPath;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Handles a {@link SensinactPath} deserialization
 */
public class SensinactPathDeserializer extends StdDeserializer<SensinactPath> {

    public SensinactPathDeserializer() {
        this(SensinactPath.class);
    }

    protected SensinactPathDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public SensinactPath deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws JacksonException {

        SensinactPath path;
        switch (parser.currentToken()) {
            case VALUE_STRING:
                path = SensinactPath.fromUri(parser.getString());
                break;
            case VALUE_NULL:
                path = null;
                break;
            case START_OBJECT:
                path = parseObject(parser);
                break;
            default:
                throw MismatchedInputException.from(ctxt,
                        "Invalid node type " + parser.currentToken() + " expected text or null");
        }
        return path;
    }

    private SensinactPath parseObject(JsonParser parser) throws JacksonException {
        final SensinactPath path = new SensinactPath();

        for (;;) {
            switch (parser.nextToken()) {
                case PROPERTY_NAME:
                    String fieldName = parser.getString();
                    JsonToken valueType = parser.nextToken();
                    if (valueType == JsonToken.VALUE_NULL || valueType == JsonToken.VALUE_STRING) {
                        switch (fieldName) {
                            case "provider":
                                path.provider = parser.getString();
                                break;
                            case "service":
                                path.service = parser.getString();
                                break;
                            case "resource":
                                path.resource = parser.getString();
                                break;
                            case "metadata":
                                path.metadata = parser.getString();
                                break;
                            default:
                                throw UnrecognizedPropertyException.from(parser, SensinactPath.class, fieldName,
                                        Set.of("provider", "service", "resource", "metadata"));
                        }
                    } else {
                        throw MismatchedInputException.from(parser, SensinactPath.class,
                                "Invalid value type " + parser.currentToken() + " for field " + fieldName);
                    }
                    break;
                case END_OBJECT:
                    return path;
                default:
                    throw MismatchedInputException.from(parser, SensinactPath.class,
                            "Invalid node type " + parser.currentToken() + " expected FIELD_NAME or END_OBJECT");
            }
        }
    }
}
