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
package org.eclipse.sensinact.northbound.query.dto.notification.jackson;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sensinact.northbound.query.dto.notification.AbstractResourceNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceDataNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceLifecycleNotificationDTO;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.util.TokenBuffer;

/**
 * Deserialization of the content of a notification
 */
public class ResourceNotificationDeserializer extends StdDeserializer<AbstractResourceNotificationDTO> {

    public ResourceNotificationDeserializer() {
        this(AbstractResourceNotificationDTO.class);
    }

    protected ResourceNotificationDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public AbstractResourceNotificationDTO deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws JacksonException {
        // Use a buffer and a temporary parser to allow us to introspect and identify the right subclass
        TokenBuffer buffer = ctxt.bufferAsCopyOfValue(parser);
        JsonParser tmpParser = buffer.asParserOnFirstToken(parser.objectReadContext(), parser);

        switch(tmpParser.currentToken()) {
        case VALUE_NULL:
            return null;
        case START_OBJECT:
            return buffer.asParser(parser.objectReadContext())
                    .readValueAs(determineTargetType(tmpParser, ctxt));
        default:
            ctxt.reportWrongTokenException(AbstractResourceNotificationDTO.class,
                    JsonToken.START_OBJECT, "Invalid token type for a resource notification");
            return null;
        }
    }

    private Class<? extends AbstractResourceNotificationDTO> determineTargetType(JsonParser parser,
            DeserializationContext ctxt) {
        Set<String> knownProps = new HashSet<>();
        JsonToken token;
        loop: while((token = parser.nextToken()) != null) {
            switch(token) {
            case PROPERTY_NAME:
                knownProps.add(parser.currentName());
                break;
            case START_OBJECT:
            case START_ARRAY:
                parser.skipChildren();
                continue loop;
            default:
                continue loop;
            }
            if(knownProps.contains("status") && knownProps.contains("initialValue")) {
                return ResourceLifecycleNotificationDTO.class;
            } else if (knownProps.contains("oldValue") && knownProps.contains("newValue")) {
                return ResourceDataNotificationDTO.class;
            }
        }
        ctxt.reportInputMismatch(AbstractResourceNotificationDTO.class,
                "Unable to determine the correct type of AbstractResourceNotificationDTO from properties {}",
                knownProps);
        return null;
    }
}
