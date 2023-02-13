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
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BytevalueContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.Int16valueContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.Int32valueContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.Int64valueContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.SbytevalueContext;

public class IntegerVisitor extends ODataFilterBaseVisitor<Integer> {

    private Integer parseInt(ParserRuleContext ctx) {
        return Integer.parseInt(ctx.getText());
    }

    @Override
    public Integer visitSbytevalue(SbytevalueContext ctx) {
        return parseInt(ctx);
    }

    @Override
    public Integer visitBytevalue(BytevalueContext ctx) {
        return parseInt(ctx);
    }

    @Override
    public Integer visitInt16value(Int16valueContext ctx) {
        return parseInt(ctx);
    }

    @Override
    public Integer visitInt32value(Int32valueContext ctx) {
        return parseInt(ctx);
    }

    @Override
    public Integer visitInt64value(Int64valueContext ctx) {
        return parseInt(ctx);
    }
}
