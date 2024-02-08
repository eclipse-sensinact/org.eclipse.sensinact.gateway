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

import org.eclipse.sensinact.core.annotation.dto.NullAction;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Options of the device mapper
 */
public class DeviceMappingOptionsDTO {

    @JsonProperty("format.date")
    public String formatDate;

    @JsonProperty("format.time")
    public String formatTime;

    @JsonProperty("format.datetime")
    public String formatDateTime;

    @JsonProperty("format.datetime.locale")
    public String formatDateTimeLocale;

    @JsonProperty("format.date.style")
    public String formatDateStyle;

    @JsonProperty("format.time.style")
    public String formatTimeStyle;

    @JsonProperty("datetime.timezone")
    public String dateTimezone;

    @JsonProperty("numbers.locale")
    public String numbersLocale;

    @JsonProperty("null.action")
    public NullAction nullAction = NullAction.UPDATE;

    @JsonProperty("log.errors")
    public boolean logErrors = false;
}
