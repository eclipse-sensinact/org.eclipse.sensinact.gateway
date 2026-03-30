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
package org.eclipse.sensinact.northbound.query.dto.notification;

import java.util.Map;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Deserialization of the content of a notification
 */
public class ResourceNotificationDeserializer extends StdDeserializer<AbstractResourceNotificationDTO> {

    public ResourceNotificationDeserializer() {
        this(null);
    }

    protected ResourceNotificationDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public AbstractResourceNotificationDTO deserialize(final JsonParser parser, final DeserializationContext ctxt)
            throws JacksonException {

        final Map<?, ?> rawNotif = parser.readValueAs(Map.class);
        if (rawNotif == null) {
            return null;
        }

        final ObjectMapper mapper = JsonMapper.builder().build();
        if (rawNotif.containsKey("status") && rawNotif.containsKey("initialValue")) {
            return mapper.convertValue(rawNotif, ResourceLifecycleNotificationDTO.class);
        } else if (rawNotif.containsKey("oldValue") && rawNotif.containsKey("newValue")) {
            return mapper.convertValue(rawNotif, ResourceDataNotificationDTO.class);
        } else {
            throw new RuntimeException("Unsupported notification DTO");
        }
    }
}
