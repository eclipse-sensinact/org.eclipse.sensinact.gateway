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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UnknownDataTypeSchema extends DataSchema {

    @JsonAnyGetter
    @JsonAnySetter
    public Map<String, Object> extraProperties = new HashMap<>();

    @JsonCreator(mode = Mode.PROPERTIES)
    public UnknownDataTypeSchema(@JsonProperty("type") String type) {
        super(type);
    }
}
