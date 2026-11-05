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
package org.eclipse.sensinact.gateway.southbound.device.factory;

import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;

/**
 * Definition of the device mapping handler
 */
public interface IDeviceMappingHandler {

    /**
     * Context entry describing the source of a payload, for example the MQTT
     * handler and topic or the HTTP URL: the handler names it in its log
     * messages. Namespaced so that it does not hide an HTTP response header.
     */
    String CONTEXT_SOURCE = "sensinact.source";

    /**
     * Handles the content of the given payload and updates resources accordingly.
     * <p>
     * Callers must give the same configuration instance as long as the
     * configuration doesn't change: the reporting of errors (the first error of
     * each kind is always logged) is kept per instance. The device factory
     * implementation reports the errors itself, so its callers can log what
     * this method throws at debug level; they should describe the source of the
     * payload in the {@link #CONTEXT_SOURCE} entry of the context.
     *
     * @param configuration Mapping configuration (must contain the parser ID)
     * @param payload       Raw content to parse
     * @throws DeviceFactoryException Error handling records
     */
    void handle(DeviceMappingConfigurationDTO configuration, Map<String, String> context, byte[] payload)
            throws MissingParserException, InvalidResourcePathException, ParserException, DeviceFactoryException;

    /**
     * Checks a mapping configuration before using it, so that an invalid one can
     * be refused when it is set rather than at each payload
     *
     * @param configuration Mapping configuration
     * @throws IllegalArgumentException Invalid configuration
     */
    default void validate(DeviceMappingConfigurationDTO configuration) throws IllegalArgumentException {
    }

}
