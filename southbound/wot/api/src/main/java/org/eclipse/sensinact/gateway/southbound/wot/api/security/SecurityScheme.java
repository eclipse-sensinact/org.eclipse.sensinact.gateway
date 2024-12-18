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

package org.eclipse.sensinact.gateway.southbound.wot.api.security;

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

@JsonSubTypes({ @Type(value = NoSecurityScheme.class, name = "nosec"),
        @Type(value = BasicSecurityScheme.class, name = "basic"), })
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "scheme")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SecurityScheme {

    @JsonProperty("@type")
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
    public List<String> semanticType;

    public String description;

    public String proxy;

    /**
     * This field is not deserialized, but populated by the constructor This field
     * also defines the type of the object, and is used to map into a Java type when
     * deserializing
     */
    @JsonIgnoreProperties(allowGetters = true)
    public final String scheme;

    public SecurityScheme(String scheme) {
        this.scheme = scheme;
    }
}
