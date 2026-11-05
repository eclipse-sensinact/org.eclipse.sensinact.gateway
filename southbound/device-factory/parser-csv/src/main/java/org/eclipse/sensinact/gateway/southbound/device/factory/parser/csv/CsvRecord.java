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

/**
 * Device factory CSV record handler
 */
public class CsvRecord implements IDeviceMappingRecord {

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
            // Missing column: the device factory applies the policies
            return null;
        }
    }

    @Override
    public boolean hasField(final RecordPath path) {
        try {
            if (path.isInt() || columnIndexOnly) {
                final int index = path.asInt();
                return index >= 0 && index < record.size();
            } else {
                // isMapped() only checks the header, isSet() also the length of the row
                return record.isSet(path.asString());
            }
        } catch (IllegalArgumentException e) {
            // Not a column index
            return false;
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
