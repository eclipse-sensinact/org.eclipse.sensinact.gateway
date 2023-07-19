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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.ParserException;

/**
 * Emulates a parser
 */
public class FakeDeviceMappingParser implements IDeviceMappingParser {

    final List<MapRecord> records = new ArrayList<>();

    public void addRecord(final MapRecord record) {
        this.records.add(record);
    }

    public void setRecords(final Map<String, Object> record) {
        setRecords(new MapRecord(record));
    }

    public void setRecords(final MapRecord record) {
        this.records.clear();
        if (record != null) {
            this.records.add(record);
        }
    }

    public void setRecords(final List<MapRecord> records) {
        this.records.clear();
        if (records != null) {
            this.records.addAll(records);
        }
    }

    @Override
    public List<? extends IDeviceMappingRecord> parseRecords(final byte[] rawInput,
            final Map<String, Object> parserConfiguration, final Map<String, String> context) throws ParserException {
        return records;
    }
}
