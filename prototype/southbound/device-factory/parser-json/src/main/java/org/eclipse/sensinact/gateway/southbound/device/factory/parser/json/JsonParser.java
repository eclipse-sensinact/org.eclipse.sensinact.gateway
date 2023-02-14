/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.device.factory.parser.json;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.device.factory.EncodingUtils;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.ParserException;
import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON parser provider
 */
@Component(immediate = true, service = IDeviceMappingParser.class, property = IDeviceMappingParser.PARSER_ID + "="
        + "json")
public class JsonParser implements IDeviceMappingParser {

    /**
     * Object mapper
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<? extends IDeviceMappingRecord> parseRecords(byte[] rawInput, Map<String, Object> parserConfiguration)
            throws ParserException {

        // Configured base entry
        String base = (String) parserConfiguration.get("base");
        if (base == null || base.isBlank()) {
            base = null;
        }

        try {
            // Use the configured encoding
            final String strEncoding = (String) parserConfiguration.get("encoding");
            final JsonNode root;
            if (strEncoding != null && !strEncoding.isBlank()) {
                final Charset charset = Charset.forName(strEncoding);
                final byte[] noBomInput;
                if (StandardCharsets.UTF_8.equals(charset) && rawInput.length > 3) {
                    noBomInput = EncodingUtils.removeBOM(rawInput);
                } else {
                    noBomInput = rawInput;
                }

                final String input = new String(noBomInput, charset).trim();
                root = objectMapper.readValue(input, JsonNode.class);
            } else {
                root = objectMapper.readValue(rawInput, JsonNode.class);
            }

            return parseRecords(root, base);
        } catch (IllegalArgumentException e) {
            throw new ParserException("Invalid JSON input", e);
        } catch (IOException e) {
            throw new ParserException("Error parsing JSON input", e);
        }
    }

    private JsonNode getPath(JsonNode node, String pathPart) {

        if (node.isArray()) {
            return node.get(Integer.valueOf(pathPart));
        }

        return node.get(pathPart);
    }

    /**
     * Creates records based on the parsed JSON node
     *
     * @param root JSON root object/array
     * @param base Base path to access the record(s)
     * @return The list of parsed records
     * @throws ParserException Error reading input
     */
    private List<JsonRecord> parseRecords(final JsonNode root, final String base) throws ParserException {

        final JsonNode subRoot;
        if (base == null) {
            subRoot = root;
        } else {
            // Walk to the base
            final String[] parts = base.split("/");

            JsonNode current = root;
            for (String part : parts) {
                if (!part.isBlank()) {
                    current = getPath(current, part);
                }
            }

            subRoot = current;
        }

        if (subRoot.isObject()) {
            return List.of(new JsonRecord(objectMapper, subRoot));
        } else if (subRoot.isArray()) {
            final List<JsonRecord> result = new ArrayList<>(subRoot.size());
            for (JsonNode child : subRoot) {
                result.add(new JsonRecord(objectMapper, child));
            }
            return result;
        } else {
            throw new ParserException("Unsupported JSON content: " + subRoot.getNodeType());
        }
    }
}
