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

import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.device.factory.IResourceMapping;
import org.eclipse.sensinact.gateway.southbound.device.factory.InvalidResourcePathException;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;
import org.eclipse.sensinact.gateway.southbound.device.factory.ValueType;

public class ResourceMappingHandler {

    /**
     * Literal value to return in a literal-based record. If set, {@link #KEY_PATH}
     * and {@link #KEY_DEFAULT} are ignored.
     */
    private final static String KEY_LITERAL = "literal";

    /**
     * Name of the resource path entry in a path-based record. Ignored if
     * {@link #KEY_LITERAL} is set.
     */
    private final static String KEY_PATH = "path";

    /**
     * Name of the literal entry to use in path-based record if the path is not
     * found. Works only if {@link #KEY_PATH} is set.
     */
    private final static String KEY_DEFAULT = "default";

    /**
     * Type of resource behind the path
     */
    private final static String KEY_TYPE = "type";

    /**
     * Parses a mapping configuration
     *
     * @param key        Resource path
     * @param rawMapping Mapping configuration
     * @return Parsed mapping
     * @throws InvalidResourcePathException Invalid resource path
     * @throws IllegalArgumentException     Invalid configuration
     */
    public IResourceMapping parseMapping(final String key, final Object rawMapping)
            throws InvalidResourcePathException, IllegalArgumentException {

        if (rawMapping instanceof String) {
            // String path (JSON path or CSV column name)
            return new ResourceRecordMapping(key, new RecordPath((String) rawMapping));
        } else if (rawMapping instanceof Integer) {
            // Integer path (CSV column index)
            return new ResourceRecordMapping(key, new RecordPath((Integer) rawMapping));
        } else if (!(rawMapping instanceof Map)) {
            // Not an object
            throw new IllegalArgumentException(String.format("Unsupported mapping definition: %s (%s)", rawMapping,
                    rawMapping != null ? rawMapping.getClass() : "null"));
        }

        final Map<?, ?> mapping = (Map<?, ?>) rawMapping;

        // Parse value type, if any
        final Object rawValueType = mapping.get(KEY_TYPE);
        final ValueType valueType;
        if (rawValueType != null) {
            valueType = ValueType.fromString(String.valueOf(rawValueType));
        } else {
            valueType = ValueType.AS_IS;
        }

        if (mapping.containsKey(KEY_LITERAL)) {
            return new ResourceLiteralMapping(key, valueType, mapping.get(KEY_LITERAL));
        } else if (mapping.containsKey(KEY_PATH)) {
            return loadRecordMapping(key, valueType, mapping);
        } else {
            throw new IllegalArgumentException(
                    String.format("Mapping definition must contain define %s or %s", KEY_PATH, KEY_LITERAL));
        }
    }

    private ResourceRecordMapping loadRecordMapping(final String key, final ValueType valueType,
            final Map<?, ?> rawMapping) throws InvalidResourcePathException, IllegalArgumentException {
        final Object rawPath = rawMapping.get(KEY_PATH);
        if (rawPath == null) {
            throw new IllegalArgumentException("No record path given");
        }

        final RecordPath recordPath = RecordPath.fromObject(rawPath, valueType);
        if (rawMapping.containsKey(KEY_DEFAULT)) {
            recordPath.setDefaultValue(rawMapping.get(KEY_DEFAULT));
        }

        return new ResourceRecordMapping(key, recordPath);
    }
}
