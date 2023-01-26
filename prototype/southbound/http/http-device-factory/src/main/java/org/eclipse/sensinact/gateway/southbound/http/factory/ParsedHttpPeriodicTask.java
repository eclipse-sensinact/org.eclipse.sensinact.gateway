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
package org.eclipse.sensinact.gateway.southbound.http.factory;

import org.eclipse.sensinact.gateway.southbound.http.factory.config.HttpDeviceFactoryConfigurationPeriodicDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Parsed HTTP periodic task configuration
 */
public class ParsedHttpPeriodicTask extends ParsedHttpTask {

    /**
     * Update period in seconds
     */
    public final int period;

    public ParsedHttpPeriodicTask(final HttpDeviceFactoryConfigurationPeriodicDTO task)
            throws JsonMappingException, JsonProcessingException {
        super(task);
        this.period = task.period;
    }
}
