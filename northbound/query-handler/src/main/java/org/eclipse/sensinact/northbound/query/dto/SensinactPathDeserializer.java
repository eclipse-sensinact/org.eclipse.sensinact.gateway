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
package org.eclipse.sensinact.northbound.query.dto;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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

        final JsonNode node = parser.getCodec().readTree(parser);
        if (node.isTextual()) {
            return SensinactPath.fromUri(node.asText());
        } else if (node.isNull()) {
            return null;
        } else if (!node.isObject()) {
            throw new IOException("Sensinact path should be an object or a string");
        }

        final SensinactPath path = new SensinactPath();
        path.provider = getValue(node, "provider");
        path.service = getValue(node, "service");
        path.resource = getValue(node, "resource");
        path.metadata = getValue(node, "metadata");
        return path;
    }

    private String getValue(final JsonNode node, final String key) {
        if (node.has(key)) {
            return node.get(key).asText();
        }
        return null;
    }
}
