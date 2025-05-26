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
package org.eclipse.sensinact.northbound.query.dto.result.jackson;

import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static org.eclipse.sensinact.northbound.query.api.EResultType.DESCRIBE_PROVIDER;
import static org.eclipse.sensinact.northbound.query.api.EResultType.DESCRIBE_RESOURCE;
import static org.eclipse.sensinact.northbound.query.api.EResultType.DESCRIBE_SERVICE;
import static org.eclipse.sensinact.northbound.query.api.EResultType.ERROR;
import static org.eclipse.sensinact.northbound.query.api.EResultType.GET_RESPONSE;
import static org.eclipse.sensinact.northbound.query.api.EResultType.SET_RESPONSE;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeProviderDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeResourceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeServiceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseSetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.SubResult;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("rawtypes")
public class TypedResponseDeserializer extends StdDeserializer<TypedResponse> {

    private static final long serialVersionUID = -2337413302718463311L;

    public TypedResponseDeserializer() {
        super(TypedResponse.class);
    }

    @Override
    public TypedResponse<SubResult> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonNode root = ctxt.readTree(p);
        JsonNode typeNode = root.get("type");
        if (typeNode == null || !typeNode.isTextual()) {
            ctxt.reportWrongTokenException(this, VALUE_STRING,
                    "Unable to find the type property to identify this typed response");
        }

        String typeId = typeNode.asText();
        JsonNode responseNode = root.get("response");

        SubResult response;
        EResultType resultType;
        switch (typeId) {
        case "DESCRIBE_PROVIDER":
            response = ctxt.readTreeAsValue(responseNode, ResponseDescribeProviderDTO.class);
            resultType = DESCRIBE_PROVIDER;
            break;
        case "DESCRIBE_SERVICE":
            response = ctxt.readTreeAsValue(responseNode, ResponseDescribeServiceDTO.class);
            resultType = DESCRIBE_SERVICE;
            break;
        case "DESCRIBE_RESOURCE":
            response = ctxt.readTreeAsValue(responseNode, ResponseDescribeResourceDTO.class);
            resultType = DESCRIBE_RESOURCE;
            break;
        case "GET_RESPONSE":
            response = ctxt.readTreeAsValue(responseNode, ResponseGetDTO.class);
            resultType = GET_RESPONSE;
            break;
        case "SET_RESPONSE":
            response = ctxt.readTreeAsValue(responseNode, ResponseSetDTO.class);
            resultType = SET_RESPONSE;
            break;
        case "ERROR":
            response = null;
            resultType = ERROR;
        default:
            throw ctxt.invalidTypeIdException(ctxt.getContextualType(), typeId,
                    "Unrecognized type id - unable to identify a suitable SubResult type");
        }

        TypedResponse<SubResult> tr = new TypedResponse<>(resultType);
        tr.response = response;

        Iterator<Entry<String, JsonNode>> fields = ((ObjectNode) root).fields();

        boolean hasStatus = false, hasUri = false;
        while (fields.hasNext()) {
            Entry<String, JsonNode> next = fields.next();
            switch (next.getKey()) {
            case "type":
            case "response":
                // Already handled
                break;
            case "error":
                tr.error = ctxt.readTreeAsValue(next.getValue(), String.class);
                break;
            case "requestId":
                tr.requestId = ctxt.readTreeAsValue(next.getValue(), String.class);
                break;
            case "statusCode":
                Integer status = ctxt.readTreeAsValue(next.getValue(), Integer.class);
                if (status == null) {
                    throw ctxt.wrongTokenException(ctxt.getParser(), Integer.class, JsonToken.VALUE_NUMBER_INT,
                            "The status code must be a number");
                }
                tr.statusCode = status.intValue();
                hasStatus = true;
                break;
            case "uri":
                tr.uri = ctxt.readTreeAsValue(next.getValue(), String.class);
                hasUri = true;
                break;
            default:
                if (!ctxt.handleUnknownProperty(p, this, TypedResponse.class, next.getKey())) {
                    ctxt.reportPropertyInputMismatch(TypedResponse.class, next.getKey(),
                            "Unable to deserialize property {} for a TypedResponse", next.getKey());
                }
            }
        }

        if (!hasStatus) {
            ctxt.reportPropertyInputMismatch(TypedResponse.class, "statusCode",
                    "No status code was present for this TypedResponse");
        }
        if (!hasUri) {
            ctxt.reportPropertyInputMismatch(TypedResponse.class, "uri", "No uri was present for this TypedResponse");
        }

        return tr;
    }
}
