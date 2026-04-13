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

import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class TimeIntervalSerializer extends ValueSerializer<TimeInterval> {

    @Override
    public void serialize(TimeInterval value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        gen.writeString(value.start().toString() + "/" + value.end().toString());
    }

}
