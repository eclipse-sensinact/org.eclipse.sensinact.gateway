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

import java.util.function.BiFunction;
import java.util.function.Function;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolmethodcallexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.CommonexprContext;

/**
 * @author thoma
 *
 */
public class BooleanMethodCallExprVisitor extends ODataFilterBaseVisitor<Function<Object, Boolean>> {

    final Parser parser;
    final CommonExprVisitor visitor;

    public BooleanMethodCallExprVisitor(Parser parser) {
        this.parser = parser;
        this.visitor = new CommonExprVisitor(parser);
    }

    @Override
    public Function<Object, Boolean> visitBoolmethodcallexpr(BoolmethodcallexprContext ctx) {
        final ParserRuleContext child = ctx.getChild(ParserRuleContext.class, 0);
        // TODO: intersectsmethodcallexpr
        switch (child.getRuleIndex()) {
        case ODataFilterParser.RULE_containsmethodcallexpr:
        case ODataFilterParser.RULE_startswithmethodcallexpr:
        case ODataFilterParser.RULE_endswithmethodcallexpr:
            // 2 string arguments
            return runDualString(child);

        default:
            throw new UnsupportedRuleException("Unsupported method call", parser, child);
        }
    }

    private Function<Object, Boolean> runDualString(ParserRuleContext ctx) {
        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);

        final Function<Object, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<Object, Object> rightFun = visitor.visitCommonexpr(rightExpr);
        final BiFunction<String, String, Boolean> operation;

        switch (ctx.getRuleIndex()) {
        case ODataFilterParser.RULE_containsmethodcallexpr:
            operation = (l, r) -> l.contains(r);
            break;

        case ODataFilterParser.RULE_startswithmethodcallexpr:
            operation = (l, r) -> l.startsWith(r);
            break;

        case ODataFilterParser.RULE_endswithmethodcallexpr:
            operation = (l, r) -> l.endsWith(r);
            break;

        default:
            throw new UnsupportedRuleException("Unsupported method call", parser, ctx);
        }

        return x -> {
            Object left = leftFun.apply(x);
            Object right = rightFun.apply(x);

            if (!(left instanceof String)) {
                throw new InvalidResultTypeException("Unsupported left operand for \"" + ctx.getText() + "\"", "string",
                        left);
            } else if (!(right instanceof String)) {
                throw new InvalidResultTypeException("Unsupported right operand for \"" + ctx.getText() + "\"",
                        "string", right);
            } else {
                return operation.apply((String) left, (String) right);
            }
        };
    }
}
