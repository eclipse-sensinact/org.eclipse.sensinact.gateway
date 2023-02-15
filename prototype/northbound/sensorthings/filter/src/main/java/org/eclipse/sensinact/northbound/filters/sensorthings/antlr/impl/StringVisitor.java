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

import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.Pchar_no_squoteContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.Squote_in_stringContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.String_1Context;

public class StringVisitor extends ODataFilterBaseVisitor<String> {

    @Override
    public String visitString_1(String_1Context ctx) {
        ODataFilterBaseVisitor<String> stringContentVisitor = new ODataFilterBaseVisitor<>() {
            @Override
            public String visitSquote_in_string(Squote_in_stringContext ctx) {
                return "'";
            }

            @Override
            public String visitPchar_no_squote(Pchar_no_squoteContext ctx) {
                return ctx.getText();
            }

            @Override
            protected String aggregateResult(String aggregate, String nextResult) {
                if (aggregate != null) {
                    if (nextResult != null) {
                        return aggregate + nextResult;
                    } else {
                        return aggregate;
                    }
                } else {
                    return nextResult;
                }
            };
        };

        return stringContentVisitor.visit(ctx);
    }
}
