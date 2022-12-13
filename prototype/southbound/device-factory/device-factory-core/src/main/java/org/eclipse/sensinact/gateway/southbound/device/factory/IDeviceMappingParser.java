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
package org.eclipse.sensinact.gateway.southbound.device.factory;

import java.util.List;
import java.util.Map;

/**
 * Definition of a parser for device mapping content
 */
public interface IDeviceMappingParser {

    /**
     * ID of the parser
     */
    String PARSER_ID = "sensinact.southbound.mapping.parser";

    /**
     * MIME types supported by this parser
     */
    String PARSER_SUPPORTED_TYPES = "sensinact.southbound.mapping.types";

    List<? extends IDeviceMappingRecord> parseRecords(byte[] rawInput, Map<String, Object> parserConfiguration)
            throws ParserException;
}
