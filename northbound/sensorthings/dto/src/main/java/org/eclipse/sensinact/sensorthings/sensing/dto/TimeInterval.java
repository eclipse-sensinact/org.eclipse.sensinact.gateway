/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.dto;

import java.time.Instant;

import org.eclipse.sensinact.sensorthings.sensing.dto.jackson.TimeIntervalDeserializer;
import org.eclipse.sensinact.sensorthings.sensing.dto.jackson.TimeIntervalSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize(using = TimeIntervalDeserializer.class)
@JsonSerialize(using = TimeIntervalSerializer.class)
public record TimeInterval(Instant start, Instant end) {
}
