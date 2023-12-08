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
package org.eclipse.sensinact.gateway.southbound.device.factory.parser.csv;

import org.apache.commons.csv.CSVRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;
import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingOptionsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Device factory CSV record handler
 */
public class CsvRecord implements IDeviceMappingRecord {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Current CSV record
     */
    private final CSVRecord record;

    /**
     * Flag to indicate the paths must all be a column index
     */
    private final boolean columnIndexOnly;

    /**
     * Sets up the CSV record
     *
     * @param record          Current parsed CSV record
     * @param columnIndexOnly If true, all paths are considered column indices
     */
    public CsvRecord(final CSVRecord record, final boolean columnIndexOnly) {
        this.record = record;
        this.columnIndexOnly = columnIndexOnly;
    }

    /**
     * Returns the value of the CSV field
     *
     * @param path Record path
     * @return Record value as a string (can be null)
     */
    private String getValue(final RecordPath path) {
        try {
            if (path.isInt() || columnIndexOnly) {
                return this.record.get(path.asInt());
            } else {
                return this.record.get(path.asString());
            }
        } catch (IllegalArgumentException e) {
            // Couldn't parse value
            if (path.hasDefaultValue()) {
                final Object defaultValue = path.getDefaultValue();
                return defaultValue != null ? String.valueOf(defaultValue) : null;
            } else {
                logger.warn("Error reading CSV record: {} (path: {})", e.getMessage(), path);
                return null;
            }
        }
    }

    @Override
    public Object getField(final RecordPath field, final DeviceMappingOptionsDTO options) {
        return field.convertValue(getValue(field), options);
    }

    @Override
    public String getFieldString(final RecordPath field, final DeviceMappingOptionsDTO options) {
        return getValue(field);
    }
}
