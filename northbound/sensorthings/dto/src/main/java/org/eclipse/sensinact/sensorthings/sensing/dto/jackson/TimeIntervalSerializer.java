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
package org.eclipse.sensinact.sensorthings.sensing.dto.jackson;

import java.io.IOException;

import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class TimeIntervalSerializer extends JsonSerializer<TimeInterval> {

    @Override
    public void serialize(TimeInterval value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.start.toString() + "/" + value.end.toString());
    }

}
