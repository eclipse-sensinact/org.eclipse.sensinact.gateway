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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;

/**
 * Basic record
 */
public class MapRecord implements IDeviceMappingRecord {

    private final Map<String, Object> root;

    public MapRecord(final Map<String, Object> root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return this.root.toString();
    }

    private Object getPath(Object node, RecordPath pathPart) {
        if (node instanceof List) {
            return ((List<?>) node).get(pathPart.asInt());
        }

        return ((Map<?, ?>) node).get(pathPart.asString());
    }

    private Object walkPath(RecordPath path) {
        Object current = root;
        for (RecordPath part : path.parts()) {
            current = getPath(current, part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    private Object getRawField(RecordPath field) {
        final Object node = walkPath(field);
        if (node == null) {
            if (field.hasDefaultValue()) {
                return field.getDefaultValue();
            } else {
                return null;
            }
        }

        return node;
    }

    @Override
    public Object getField(RecordPath field, final DeviceMappingOptionsDTO options) {
        final Object rawValue = getRawField(field);
        if (rawValue == null) {
            return null;
        }

        return field.convertValue(rawValue, options);
    }

    @Override
    public String getFieldString(RecordPath field, final DeviceMappingOptionsDTO options) {
        final Object node = walkPath(field);
        if (node != null) {
            return String.valueOf(node);
        }

        if (field.hasDefaultValue()) {
            final Object defaultValue = field.getDefaultValue();
            return defaultValue != null ? String.valueOf(defaultValue) : null;
        } else {
            return null;
        }
    }
}
