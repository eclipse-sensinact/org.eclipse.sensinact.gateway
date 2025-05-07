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
package org.eclipse.sensinact.core.annotation.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

/**
 * Marks a field as containing a timestamp. The field must either be:
 * <ul>
 *  <li>A {@link Number} convertible to a long, which will be interpreted based on the epoch and {@link #value()}</li>
 *  <li>A {@link Temporal} type indicating a Date/Time, assumed to be UTC if no offset is provided</li>
 *  <li>A {@link String} parseable to a long, or with {@link DateTimeFormatter#ISO_DATE_TIME}.</li>
 *  <li><code>null</code> meaning that the current system time will be used when setting the data</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface Timestamp {
    /**
     * The unit of time this represents since the epoch, either
     * {@link ChronoUnit#MILLIS} or {@link ChronoUnit#SECONDS}
     *
     * @return
     */
    ChronoUnit value() default ChronoUnit.MILLIS;
}
