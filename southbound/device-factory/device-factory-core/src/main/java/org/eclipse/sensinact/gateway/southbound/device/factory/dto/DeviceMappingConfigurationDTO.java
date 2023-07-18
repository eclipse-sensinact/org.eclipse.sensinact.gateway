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
package org.eclipse.sensinact.gateway.southbound.device.factory.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root of the device mapping configuration
 */
public class DeviceMappingConfigurationDTO {

    /**
     * ID of the parser to use
     */
    public String parser;

    /**
     * Options to give to the payload parser
     */
    @JsonProperty("parser.options")
    public Map<String, Object> parserOptions = Map.of();

    /**
     * Definition of the mapping
     */
    public Map<String, Object> mapping = Map.of();

    /**
     * Options to give to the mapper
     */
    @JsonProperty("mapping.options")
    public DeviceMappingOptionsDTO mappingOptions = new DeviceMappingOptionsDTO();
}
