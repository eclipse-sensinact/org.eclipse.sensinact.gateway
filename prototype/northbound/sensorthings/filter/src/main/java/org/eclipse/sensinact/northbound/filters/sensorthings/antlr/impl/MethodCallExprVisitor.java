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
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.CommonexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.MethodcallexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.SubstringmethodcallexprContext;

/**
 * @author thoma
 *
 */
public class MethodCallExprVisitor extends ODataFilterBaseVisitor<Function<ResourceValueFilterInputHolder, Object>> {

    final Parser parser;
    final CommonExprVisitor visitor;

    public MethodCallExprVisitor(Parser parser) {
        this.parser = parser;
        this.visitor = new CommonExprVisitor(parser);
    }

    @Override
    public Function<ResourceValueFilterInputHolder, Object> visitMethodcallexpr(MethodcallexprContext ctx) {
        final ParserRuleContext child = ctx.getChild(ParserRuleContext.class, 0);
        switch (child.getRuleIndex()) {
        case ODataFilterParser.RULE_tolowermethodcallexpr:
        case ODataFilterParser.RULE_touppermethodcallexpr:
        case ODataFilterParser.RULE_lengthmethodcallexpr:
        case ODataFilterParser.RULE_trimmethodcallexpr:
            // 1 string argument
            return runSingleString(child);

        case ODataFilterParser.RULE_concatmethodcallexpr:
        case ODataFilterParser.RULE_indexofmethodcallexpr:
            // 2 string arguments
            return runDualString(child);

        case ODataFilterParser.RULE_substringmethodcallexpr:
            // 2 or 3 arguments
            return runSubstring((SubstringmethodcallexprContext) child);

        default:
            throw new RuntimeException("Unsupported method call: " + parser.getRuleNames()[child.getRuleIndex()]);
        }
    }

    private Function<ResourceValueFilterInputHolder, Object> runSingleString(ParserRuleContext ctx) {
        final CommonexprContext subExpr = ctx.getChild(CommonexprContext.class, 0);
        final Function<ResourceValueFilterInputHolder, Object> targetExpr = visitor.visitCommonexpr(subExpr);
        final Function<String, Object> operation;

        switch (ctx.getRuleIndex()) {
        case ODataFilterParser.RULE_tolowermethodcallexpr:
            operation = s -> s.toLowerCase();
            break;

        case ODataFilterParser.RULE_touppermethodcallexpr:
            operation = s -> s.toUpperCase();
            break;

        case ODataFilterParser.RULE_lengthmethodcallexpr:
            operation = s -> s.length();
            break;

        case ODataFilterParser.RULE_trimmethodcallexpr:
            operation = s -> s.trim();
            break;

        default:
            throw new RuntimeException("Unsupported rule: " + parser.getRuleNames()[ctx.getRuleIndex()]);
        }

        return x -> {
            Object res = targetExpr.apply(x);
            if (res instanceof String) {
                return operation.apply((String) res);
            }

            throw new RuntimeException(String.format("Result is not a string: %s (%s)", res,
                    res != null ? res.getClass().getName() : "null"));
        };
    }

    private Function<ResourceValueFilterInputHolder, Object> runDualString(ParserRuleContext ctx) {
        final CommonexprContext leftExpr = ctx.getChild(CommonexprContext.class, 0);
        final CommonexprContext rightExpr = ctx.getChild(CommonexprContext.class, 1);

        final Function<ResourceValueFilterInputHolder, Object> leftFun = visitor.visitCommonexpr(leftExpr);
        final Function<ResourceValueFilterInputHolder, Object> rightFun = visitor.visitCommonexpr(rightExpr);
        final BiFunction<String, String, Object> operation;

        switch (ctx.getRuleIndex()) {
        case ODataFilterParser.RULE_concatmethodcallexpr:
            operation = (l, r) -> l + r;
            break;

        case ODataFilterParser.RULE_indexofmethodcallexpr:
            operation = (l, r) -> l.indexOf(r);
            break;

        default:
            throw new UnsupportedRuleException(parser, ctx);
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

    private Function<ResourceValueFilterInputHolder, Object> runSubstring(SubstringmethodcallexprContext ctx) {
        final Function<ResourceValueFilterInputHolder, Object> stringFun = visitor.visitCommonexpr(ctx.commonexpr(0));
        final Function<ResourceValueFilterInputHolder, Object> startPosFun = visitor.visitCommonexpr(ctx.commonexpr(1));

        if (ctx.commonexpr().size() == 2) {
            // String + start
            return x -> {
                Object string = stringFun.apply(x);
                Object startPos = startPosFun.apply(x);

                if (!(string instanceof String)) {
                    throw new InvalidResultTypeException("Unsupported string argument for \"" + ctx.getText() + "\"",
                            "string", string);
                } else if (!(startPos instanceof Integer)) {
                    throw new InvalidResultTypeException("Unsupported start argument for \"" + ctx.getText() + "\"",
                            "int", startPos);
                } else {
                    return ((String) string).substring((Integer) startPos);
                }
            };
        } else {
            // String + start + length
            final Function<ResourceValueFilterInputHolder, Object> lengthFun = visitor.visitCommonexpr(ctx.commonexpr(2));
            return x -> {
                Object string = stringFun.apply(x);
                Object startPos = startPosFun.apply(x);
                Object length = lengthFun.apply(x);

                if (!(string instanceof String)) {
                    throw new InvalidResultTypeException("Unsupported string argument for \"" + ctx.getText() + "\"",
                            "string", string);
                } else if (!(startPos instanceof Integer)) {
                    throw new InvalidResultTypeException("Unsupported start argument for \"" + ctx.getText() + "\"",
                            "int", startPos);
                } else if (!(length instanceof Integer)) {
                    throw new InvalidResultTypeException("Unsupported length argument for \"" + ctx.getText() + "\"",
                            "int", length);
                } else {
                    int endPos = (Integer) startPos + (Integer) length;
                    return ((String) string).substring((Integer) startPos, endPos);
                }
            };
        }
    }
}
