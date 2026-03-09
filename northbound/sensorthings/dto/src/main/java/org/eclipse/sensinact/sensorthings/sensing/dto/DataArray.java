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
package org.eclipse.sensinact.sensorthings.sensing.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record DataArray(@JsonIgnore String selfLink, @JsonIgnore String entityName, @JsonIgnore String entityLink,
        List<String> components, List<List<Object>> dataArray) implements Self {
    @JsonAnyGetter
    public Map<String, String> requestLink() {
        return Map.of(entityName, entityLink);
    }
}
