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

package org.eclipse.sensinact.gateway.southbound.wot.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.DataSchema;

class ParameterDescription {
    String name;
    Class<?> type;
    DataSchema schema;

    ParameterDescription(String name, Class<?> type, DataSchema schema) {
        this.name = name;
        this.type = type;
        this.schema = schema;
    }

    Entry<String, Class<?>> toEntry() {
        return Map.entry(name, type);
    }

    Map<String, Object> toMetadata() {
        Map<String, Object> paramMetadata = new HashMap<>();
        paramMetadata.put("description", schema.description);
        paramMetadata.put("defaultValue", schema.defaultValue);
        paramMetadata.put("constantValue", schema.constantValue);
        paramMetadata.put("format", schema.format);
        paramMetadata.put("unit", schema.unit);
        return paramMetadata;
    }
}
