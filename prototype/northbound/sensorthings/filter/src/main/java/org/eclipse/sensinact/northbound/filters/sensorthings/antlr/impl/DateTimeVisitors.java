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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.DatetimeoffsetvalueContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.DatevalueContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.DurationContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.DurationvalueContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.TimeofdayvalueContext;

public class DateTimeVisitors {

    public static int parseInt(ParserRuleContext ctx) {
        return Integer.parseInt(ctx.getText());
    }

    public static OffsetDateTime dateTimeOffset(ParserRuleContext ruleCtx) {
        final DatetimeoffsetvalueContext ctx = (DatetimeoffsetvalueContext) ruleCtx;
        return OffsetDateTime.parse(ctx.getText());
    }

    public static LocalDate date(ParserRuleContext ruleCtx) {
        final DatevalueContext ctx = (DatevalueContext) ruleCtx;
        return LocalDate.of(parseInt(ctx.year()), parseInt(ctx.month()), parseInt(ctx.day()));
    }

    public static LocalTime timeOfDay(ParserRuleContext ruleCtx) {
        final TimeofdayvalueContext ctx = (TimeofdayvalueContext) ruleCtx;
        final int hour = parseInt(ctx.hour());
        final int minute = parseInt(ctx.minute());
        if (ctx.second() != null) {
            final int second = parseInt(ctx.second());
            return LocalTime.of(hour, minute, second);
        } else {
            return LocalTime.of(hour, minute);
        }
    }

    public static Duration duration(ParserRuleContext ruleCtx) {
        // See http://www.datypic.com/sc/xsd/t-xsd_dayTimeDuration.html for examples
        final DurationvalueContext ctx = ((DurationContext) ruleCtx).durationvalue();
        return Duration.parse(ctx.getText());
    }
}
