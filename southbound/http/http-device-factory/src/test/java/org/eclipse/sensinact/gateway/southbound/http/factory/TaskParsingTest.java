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
package org.eclipse.sensinact.gateway.southbound.http.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.temporal.ChronoUnit;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.DeviceMappingConfigurationDTO;
import org.eclipse.sensinact.gateway.southbound.http.factory.config.HttpDeviceFactoryConfigurationPeriodicDTO;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class TaskParsingTest {

    @Test
    void testPeriodUnit() throws Exception {
        final HttpDeviceFactoryConfigurationPeriodicDTO dto = new HttpDeviceFactoryConfigurationPeriodicDTO();
        dto.url = "sample";
        dto.mapping = new DeviceMappingConfigurationDTO();
        dto.period = 10;
        assertEquals(10, new ParsedHttpPeriodicTask(dto).period);

        dto.periodUnit = ChronoUnit.SECONDS;
        assertEquals(10, new ParsedHttpPeriodicTask(dto).period);

        dto.periodUnit = ChronoUnit.MINUTES;
        assertEquals(10 * 60, new ParsedHttpPeriodicTask(dto).period);

        dto.periodUnit = ChronoUnit.HOURS;
        assertEquals(10 * 3600, new ParsedHttpPeriodicTask(dto).period);
    }
}
