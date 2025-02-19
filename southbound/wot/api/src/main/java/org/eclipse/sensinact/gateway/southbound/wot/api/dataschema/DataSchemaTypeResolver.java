/*********************************************************************
* Copyright (c) 2025 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - Initial contribution
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.api.dataschema;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class DataSchemaTypeResolver extends TypeIdResolverBase {

    public static final String JSON_SCHEMA_NS_PREFIX = "jsonschema";
    public static final String JSON_SCHEMA_NS_URI = "https://www.w3.org/2019/wot/json-schema#";

    private JavaType superType;

    @Override
    public void init(JavaType baseType) {
        superType = baseType;
    }

    @Override
    public JavaType typeFromId(final DatabindContext context, final String id) throws IOException {
        // Look for a namespace
        final String typeName;
        final String[] parts = id.split(":");
        if (parts.length == 2 && !parts[1].startsWith("/")) {
            if (JSON_SCHEMA_NS_PREFIX.equalsIgnoreCase(parts[0])) {
                typeName = JSON_SCHEMA_NS_URI + parts[1];
            } else {
                typeName = id;
            }
        } else {
            typeName = id;
        }

        switch (typeName) {
        case "object":
        case "https://www.w3.org/2019/wot/json-schema#ObjectSchema":
            return context.constructSpecializedType(superType, ObjectSchema.class);

        case "array":
        case "https://www.w3.org/2019/wot/json-schema#ArraySchema":
            return context.constructSpecializedType(superType, ArraySchema.class);

        case "boolean":
        case "https://www.w3.org/2019/wot/json-schema#BooleanSchema":
            return context.constructSpecializedType(superType, BooleanSchema.class);

        case "string":
        case "https://www.w3.org/2019/wot/json-schema#StringSchema":
            return context.constructSpecializedType(superType, StringSchema.class);

        case "number":
        case "https://www.w3.org/2019/wot/json-schema#NumberSchema":
            return context.constructSpecializedType(superType, NumberSchema.class);

        case "integer":
        case "https://www.w3.org/2019/wot/json-schema#IntegerSchema":
            return context.constructSpecializedType(superType, IntegerSchema.class);

        case "null":
        case "https://www.w3.org/2019/wot/json-schema#NullSchema":
            return context.constructSpecializedType(superType, NullSchema.class);

        default:
            return context.constructSpecializedType(superType, UnknownDataTypeSchema.class);
        }
    }

    @Override
    public String idFromValue(final Object value) {
        if (value instanceof DataSchema ds) {
            // Don't return null here, or Jackson will complain on OneOfDataSchema
            return Optional.ofNullable(ds.type).orElse("oneOf");
        }

        return null;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public Id getMechanism() {
        return Id.CUSTOM;
    }
}
