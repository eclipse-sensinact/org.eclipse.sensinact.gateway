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
import java.time.Duration;
import java.time.Instant;

import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class TimeIntervalDeserializer extends JsonDeserializer<TimeInterval> {

    @Override
    public TimeInterval deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        if(p.currentToken() != JsonToken.VALUE_STRING) {
            throw MismatchedInputException.from(p, TimeInterval.class,
                    "Must be serialized as an ISO 8601 Time Interval String, not " + p.currentToken());
        }
        String value = p.getText();

        int slash = value.indexOf('/');
        if(slash < 0) {
            failFormat(p, value);
        }

        String first = value.substring(0, slash);
        String second = value.substring(slash + 1, value.length());

        TimeInterval ti;
        if(first.startsWith("P")) {
            if(second.startsWith("P")) {
                throw failFormat(p, value);
            } else {
                try {
                    Duration d = Duration.parse(first);
                    Instant end = Instant.parse(second);
                    ti = new TimeInterval(end.minus(d), end);
                } catch (Exception e) {
                    throw failFormat(p, value);
                }
            }
        } else if(second.startsWith("P")) {
            try {
                Instant start = Instant.parse(first);
                Duration d = Duration.parse(second);
                ti = new TimeInterval(start, start.plus(d));
            } catch (Exception e) {
                throw failFormat(p, value);
            }
        } else {
            try {
                ti = new TimeInterval(Instant.parse(first), Instant.parse(second));
            } catch (Exception e) {
                throw failFormat(p, value);
            }
        }

        return ti;
    }

    InvalidFormatException failFormat(JsonParser p, String value) {
        return new InvalidFormatException(p, "Must be serialized as an ISO 8601 Time Interval String",
                value, TimeInterval.class);
    }
}
