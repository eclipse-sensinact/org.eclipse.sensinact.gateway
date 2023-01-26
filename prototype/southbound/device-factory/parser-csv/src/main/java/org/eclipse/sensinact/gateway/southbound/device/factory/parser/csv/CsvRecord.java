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
package org.eclipse.sensinact.gateway.southbound.device.factory.parser.csv;

import org.apache.commons.csv.CSVRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.RecordPath;

/**
 */
public class CsvRecord implements IDeviceMappingRecord {

    /**
     * Current CSV record
     */
    private final CSVRecord record;

    public CsvRecord(final CSVRecord record) {
        this.record = record;
    }

    /**
     * Returns the value of the CSV field
     *
     * @param path
     * @return
     */
    private String getValue(RecordPath path) {
        if (path == null) {
            return null;
        } else if (path.isInt()) {
            return this.record.get(path.asInt());
        } else {
            return this.record.get(path.asString());
        }
    }

    @Override
    public Object getField(RecordPath field) {
        return field.convertValue(getValue(field));
    }

    @Override
    public String getFieldString(RecordPath field) {
        return getValue(field);
    }

    @Override
    public Integer getFieldInt(RecordPath field) {
        return Integer.valueOf(getValue(field));
    }
}
