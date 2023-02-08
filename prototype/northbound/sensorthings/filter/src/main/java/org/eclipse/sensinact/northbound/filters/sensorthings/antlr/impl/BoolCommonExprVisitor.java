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

import java.time.OffsetDateTime;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterBaseVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.AndexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolcommonexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolmethodcallexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.CommonexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.EqexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GeexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.GtexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.IsofexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.LeexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.LtexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.NeexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.OrexprContext;

/**
 * @author thoma
 *
 */
public class BoolCommonExprVisitor extends ODataFilterBaseVisitor<Predicate<ResourceValueFilterInputHolder>> {

    private final static double ESPILON = Math.pow(10, -6);

    final Parser parser;

    public BoolCommonExprVisitor(Parser parser) {
        this.parser = parser;
    }

    @Override
    public Predicate<ResourceValueFilterInputHolder> visitBoolmethodcallexpr(BoolmethodcallexprContext ctx) {
        return new BooleanMethodCallExprVisitor(parser).visitBoolmethodcallexpr(ctx)::apply;
    }

    @Override
    public Predicate<ResourceValueFilterInputHolder> visitIsofexpr(IsofexprContext ctx) {
        // TODO Auto-generated method stub
        return super.visitIsofexpr(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Predicate<ResourceValueFilterInputHolder> visitBoolcommonexpr(BoolcommonexprContext ctx) {
        // Get the leftmost element of the expression
        final ParserRuleContext firstElement = ctx.getChild(ParserRuleContext.class, 0);
        final int nbChildren = ctx.getChildCount();

        Predicate<ResourceValueFilterInputHolder> predicate = null;
        switch (firstElement.getRuleIndex()) {
        case ODataFilterParser.RULE_isofexpr:
            predicate = visitIsofexpr(ctx.isofexpr());
            break;

        case ODataFilterParser.RULE_boolmethodcallexpr:
            predicate = visitBoolmethodcallexpr(ctx.boolmethodcallexpr());
            break;

        case ODataFilterParser.RULE_notexpr:
            predicate = Predicate.not(visitBoolcommonexpr(ctx.notexpr().boolcommonexpr()));
            break;

        case ODataFilterParser.RULE_commonexpr: {
            final Function<ResourceValueFilterInputHolder, Object> leftExpr = new CommonExprVisitor(parser)
                    .visitCommonexpr((CommonexprContext) firstElement);
            if (leftExpr == null) {
                throw new RuntimeException("Unsupported left common expression: "
                        + ctx.getChild(CommonexprContext.class, 0).toStringTree(parser));
            }

            if (nbChildren >= 2) {
                // Handle comparator
                final ParserRuleContext secondElement = ctx.getChild(ParserRuleContext.class, 1);
                final CommonExprVisitor rightVisitor = new CommonExprVisitor(parser);

                final int comparatorRuleIndex = secondElement.getRuleIndex();
                final BiFunction<Object, Object, Boolean> subPredicate;
                final Function<ResourceValueFilterInputHolder, Object> rightExpr;

                switch (comparatorRuleIndex) {
                case ODataFilterParser.RULE_eqexpr:
                    rightExpr = rightVisitor.visit(((EqexprContext) secondElement).commonexpr());
                    subPredicate = this::exprEqual;
                    break;
                case ODataFilterParser.RULE_neexpr:
                    rightExpr = rightVisitor.visit(((NeexprContext) secondElement).commonexpr());
                    subPredicate = this::exprNotEqual;
                    break;
                case ODataFilterParser.RULE_ltexpr:
                    rightExpr = rightVisitor.visit(((LtexprContext) secondElement).commonexpr());
                    subPredicate = (l, r) -> (l != null && ((Comparable<Object>) l).compareTo(r) < 0);
                    break;
                case ODataFilterParser.RULE_leexpr:
                    rightExpr = rightVisitor.visit(((LeexprContext) secondElement).commonexpr());
                    subPredicate = (l, r) -> (l != null && ((Comparable<Object>) l).compareTo(r) <= 0);
                    break;
                case ODataFilterParser.RULE_gtexpr:
                    rightExpr = rightVisitor.visit(((GtexprContext) secondElement).commonexpr());
                    subPredicate = (l, r) -> (l != null && ((Comparable<Object>) l).compareTo(r) > 0);
                    break;
                case ODataFilterParser.RULE_geexpr:
                    rightExpr = rightVisitor.visit(((GeexprContext) secondElement).commonexpr());
                    subPredicate = (l, r) -> (l != null && ((Comparable<Object>) l).compareTo(r) >= 0);
                    break;

                case ODataFilterParser.RULE_hasexpr:
                    // Uses an enumeration
                    throw new UnsupportedRuleException("HAS not yet implemented");

                default:
                    subPredicate = null;
                    rightExpr = null;
                    break;
                }

                if (rightExpr != null && subPredicate != null) {
                    predicate = x -> {
                        Object leftValue = leftExpr.apply(x);
                        Object rightValue = rightExpr.apply(x);

                        if (leftValue instanceof AnyMatch) {
                            return ((AnyMatch) leftValue).compare(rightValue, comparatorRuleIndex);
                        } else if (rightValue instanceof AnyMatch) {
                            return ((AnyMatch) rightValue).compare(leftValue, comparatorRuleIndex, true);
                        } else {
                            // Convert dates to Instant (to compare with resource timestamps)
                            if (leftValue instanceof OffsetDateTime) {
                                leftValue = ((OffsetDateTime) leftValue).toInstant();
                            }
                            if (rightValue instanceof OffsetDateTime) {
                                rightValue = ((OffsetDateTime) rightValue).toInstant();
                            }
                            return subPredicate.apply(leftValue, rightValue);
                        }
                    };
                }
            }

            if (predicate == null) {
                // Convert to predicate
                predicate = (x) -> {
                    Object result = leftExpr.apply(x);
                    if (result instanceof Boolean) {
                        return ((Boolean) result);
                    } else {
                        return result != null;
                    }
                };
            }

            break;
        }

        case ODataFilterParser.RULE_boolparenexpr:
            // Boolean expression between parenthesis
            predicate = visitBoolcommonexpr(ctx.boolparenexpr().boolcommonexpr());
            break;

        default:
            System.err.println("Unknown boolean common expression: " + firstElement.getRuleIndex());
            return null;
        }

        if (nbChildren > 1) {
            ParserRuleContext lastElement = ctx.getChild(ParserRuleContext.class, nbChildren - 1);
            switch (lastElement.getRuleIndex()) {
            case ODataFilterParser.RULE_andexpr: {
                if (predicate == null) {
                    throw new InvalidResultTypeException("AND requires a boolean expression on both sides");
                }

                AndexprContext and = (AndexprContext) lastElement;
                Predicate<ResourceValueFilterInputHolder> other = visitBoolcommonexpr(and.boolcommonexpr());
                return predicate.and(other);
            }

            case ODataFilterParser.RULE_orexpr: {
                if (predicate == null) {
                    System.err.println("AND requires a boolean expression on left side");
                    return null;
                }

                OrexprContext or = (OrexprContext) lastElement;
                Predicate<ResourceValueFilterInputHolder> other = visitBoolcommonexpr(or.boolcommonexpr());
                return predicate.or(other);
            }

            default:
                // Last child was a parameter for common expr
                break;
            }
        }

        return predicate;
    }

    @SuppressWarnings("unchecked")
    private boolean exprEqual(Object left, Object right) {
        if (left == null) {
            return right == null;
        } else if (right == null) {
            return false;
        } else if (left instanceof Number && right instanceof Number) {
            return Math.abs((((Number) left).doubleValue() - ((Number) right).doubleValue())) <= ESPILON;
        } else if (left instanceof Comparable) {
            return ((Comparable<Object>) left).compareTo(right) == 0;
        } else {
            return left.equals(right);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean exprNotEqual(Object left, Object right) {
        if (left == null) {
            return right != null;
        } else if (right == null) {
            return true;
        } else if (left instanceof Number && right instanceof Number) {
            return Math.abs((((Number) left).doubleValue() - ((Number) right).doubleValue())) > ESPILON;
        } else if (left instanceof Comparable) {
            return ((Comparable<Object>) left).compareTo(right) != 0;
        } else {
            return !left.equals(right);
        }
    }
}
