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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.EncodingUtils;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingParser;
import org.eclipse.sensinact.gateway.southbound.device.factory.IDeviceMappingRecord;
import org.eclipse.sensinact.gateway.southbound.device.factory.ParserException;
import org.osgi.service.component.annotations.Component;

/**
 * CSV parser provider
 */
@Component(immediate = true, service = IDeviceMappingParser.class, property = IDeviceMappingParser.PARSER_ID + "="
        + "csv")
public class CsvParser implements IDeviceMappingParser {

    @Override
    public List<? extends IDeviceMappingRecord> parseRecords(byte[] rawInput, Map<String, Object> parserConfiguration)
            throws ParserException {

        // Read CSV file
        final Charset charset;
        final String strEncoding = (String) parserConfiguration.get("encoding");
        if (strEncoding != null && !strEncoding.isBlank()) {
            charset = Charset.forName(strEncoding);
        } else {
            charset = StandardCharsets.UTF_8;
        }

        final InputStream input;
        if (StandardCharsets.UTF_8.equals(charset) && rawInput.length > 3) {
            input = EncodingUtils.removeBOM(rawInput);
        } else {
            input = new ByteArrayInputStream(rawInput);
        }

        // Prepare parser
        CSVFormat.Builder format = CSVFormat.DEFAULT.builder();
        final String delimiter = (String) parserConfiguration.get("delimiter");
        if (delimiter != null) {
            format = format.setDelimiter(delimiter);
        }

        final Boolean withHeader = (Boolean) parserConfiguration.get("header");
        if (withHeader != null && withHeader) {
            format = format.setHeader().setSkipHeaderRecord(true);
        }

        final List<CsvRecord> records = new ArrayList<>();
        try (CSVParser parser = format.build().parse(new InputStreamReader(input, charset))) {
            for (CSVRecord record : parser) {
                records.add(new CsvRecord(record));
            }
        } catch (IllegalStateException | IOException e) {
            throw new ParserException("Error reading CSV content", e);
        }
        return records;
    }
}
