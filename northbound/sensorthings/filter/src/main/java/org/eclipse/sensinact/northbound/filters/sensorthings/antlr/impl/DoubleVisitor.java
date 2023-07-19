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

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.DecimalvalueContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.DoublevalueContext;

public class DoubleVisitor extends ODataFilterBaseVisitor<Double> {

    private Double parseDouble(ParserRuleContext ctx) {
        return Double.parseDouble(ctx.getText());
    }

    @Override
    public Double visitDoublevalue(DoublevalueContext ctx) {
        return parseDouble(ctx);
    }

    @Override
    public Double visitDecimalvalue(DecimalvalueContext ctx) {
        return parseDouble(ctx);
    }
}
