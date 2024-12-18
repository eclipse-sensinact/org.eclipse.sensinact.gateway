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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Form {

    public String href;

    public String contentType = "application/json";

    public String contentCoding;

    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
    public List<String> security;

    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
    public List<String> scopes;

    public ExpectedResponse response;

    public List<AdditionalExpectedResponse> additionalResponses;

    public String subprotocol;

    @JsonFormat(with = { JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
    public List<String> op = new ArrayList<>();

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
