/*********************************************************************
* Copyright (c) 2024 Kentyou.
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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonSubTypes({ @Type(value = ArraySchema.class, name = "array"), @Type(value = BooleanSchema.class, name = "boolean"),
        @Type(value = NumberSchema.class, name = "number"), @Type(value = IntegerSchema.class, name = "integer"),
        @Type(value = ObjectSchema.class, name = "object"), @Type(value = StringSchema.class, name = "string"),
        @Type(value = NullSchema.class, name = "null"), })
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "type")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DataSchema {
    @JsonProperty("@type")
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
    public List<String> semanticType = null;

    public String title;
    public String description;

    @JsonProperty("const")
    public Object constantValue;

    @JsonProperty("default")
    public Object defaultValue;

    public String unit;

    public List<DataSchema> oneOf = new ArrayList<>();

    @JsonProperty("enum")
    public List<Object> enumOfAllowedValues;

    public boolean readOnly;
    public boolean writeOnly;

    public String format;

    /**
     * This field is not deserialized, but populated by the constructor This field
     * also defines the type of the object, and is used to map into a Java type when
     * deserializing
     */
    @JsonIgnoreProperties(allowGetters = true)
    public final String type;

    public DataSchema(String type) {
        this.type = type;
    }
}
