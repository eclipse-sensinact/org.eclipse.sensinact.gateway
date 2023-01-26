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
package org.eclipse.sensinact.gateway.southbound.virtual.temperature;

import org.eclipse.sensinact.prototype.annotation.dto.Data;
import org.eclipse.sensinact.prototype.annotation.dto.Model;
import org.eclipse.sensinact.prototype.annotation.dto.Provider;
import org.eclipse.sensinact.prototype.annotation.dto.Service;

@Model(VirtualTemperatureDto.VIRTUAL_TEMPERATURE_MODEL)
public class VirtualTemperatureDto {

    public static final String VIRTUAL_TEMPERATURE_MODEL = "virtual.temperature";

    @Provider
    public String provider;

    @Service("sensor")
    @Data
    public double temperature;

}
