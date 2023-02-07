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

import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.DatetimeoffsetvalueContext;

/**
 * @author thoma
 *
 */
public class DateTimeVisitor extends ODataFilterBaseVisitor<Instant> {

    @Override
    public Instant visitDatetimeoffsetvalue(DatetimeoffsetvalueContext ctx) {
        ZonedDateTime parse = ZonedDateTime.parse(ctx.getText());
        return parse.toInstant();
    }
}
