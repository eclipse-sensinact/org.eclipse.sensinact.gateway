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

package org.eclipse.sensinact.gateway.southbound.wot.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.wot.api.security.NoSecurityScheme;
import org.eclipse.sensinact.gateway.southbound.wot.api.security.SecurityScheme;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Thing {

    public String id;
    public String title;
    public String description;
    public String support;

    public String base;

    @JsonProperty("@type")
    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
    public List<String> semanticType;

    @JsonProperty("@context")
    public Namespaces context;

    public Map<String, PropertyAffordance> properties;
    public Map<String, ActionAffordance> actions;
    public Map<String, EventAffordance> events;
    public List<Form> forms;
    public List<Link> links;

    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
    public List<String> security = new ArrayList<>(List.of("nosec_sc"));
    public Map<String, SecurityScheme> securityDefinitions = new HashMap<>(Map.of("nosec_sc", new NoSecurityScheme()));

    private final Map<String, Object> extraProperties = new HashMap<>();

    @JsonAnySetter
    public void addAdditionalProperty(String name, Object value) {
        extraProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return extraProperties;
    }
}
