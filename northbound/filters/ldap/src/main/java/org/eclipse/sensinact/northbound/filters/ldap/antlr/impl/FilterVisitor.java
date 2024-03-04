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
package org.eclipse.sensinact.northbound.filters.ldap.antlr.impl;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterLexer;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.AndFilterContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.AttrContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.ComparatorContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.ComparisonContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.FilterContentContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.FilterContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.NotFilterContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.OrFilterContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.Valid_attrContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParserBaseVisitor;
import org.eclipse.sensinact.northbound.filters.ldap.impl.LdapFilter;

/**
 * LDAP filter visitor
 */
public class FilterVisitor extends LdapFilterParserBaseVisitor<ILdapCriterion> {

    /**
     * Visitor for compared values
     */
    private final ValueVisitor valueVisitor;

    final Parser parser;

    public FilterVisitor(final Parser parser) {
        valueVisitor = new ValueVisitor();
        this.parser = parser;
    }

    @Override
    public ILdapCriterion visitFilter(FilterContext ctx) {
        // Visit the contained filter
        return visitFilterContent(ctx.getRuleContexts(FilterContentContext.class).get(0));
    }

    private ILdapCriterion subVisitOpFilter(LdapOperator operator, ParserRuleContext ctx) {
        final List<ILdapCriterion> subCriteria = new ArrayList<>();
        for (final FilterContext filterContext : ctx.getRuleContexts(FilterContext.class)) {
            final ILdapCriterion subCriterion = visitFilter(filterContext);
            if (subCriterion != null) {
                subCriteria.add(subCriterion);
            }
        }

        if (subCriteria.isEmpty()) {
            return null;
        }
        return new LdapFilter(operator, subCriteria);
    }

    @Override
    public ILdapCriterion visitAndFilter(AndFilterContext ctx) {
        return subVisitOpFilter(LdapOperator.AND, ctx);
    }

    @Override
    public ILdapCriterion visitOrFilter(OrFilterContext ctx) {
        return subVisitOpFilter(LdapOperator.OR, ctx);
    }

    @Override
    public ILdapCriterion visitNotFilter(NotFilterContext ctx) {
        ILdapCriterion criterion = visitFilter(ctx.filter());
        criterion.negate();
        return criterion;
    }

    /**
     * Converts parsed comparator to an {@link LdapComparator} representation
     *
     * @param comparator Parsed rule
     * @return The matching {@link LdapComparator}
     * @throws IllegalArgumentException Unsupported comparator
     */
    private LdapComparator extractComparator(final ComparatorContext comparator) {
        final TerminalNode childNode = (TerminalNode) comparator.getChild(0);
        switch (childNode.getSymbol().getType()) {
        case LdapFilterLexer.EQUAL:
            return LdapComparator.EQUAL;

        case LdapFilterLexer.GREATER_EQ:
            return LdapComparator.GREATER_EQ;

        case LdapFilterLexer.LESS_EQ:
            return LdapComparator.LESS_EQ;

        case LdapFilterLexer.APPROX:
            return LdapComparator.APPROX;

        default:
            throw new IllegalArgumentException("Unexpected comparator: " + comparator.getText());
        }
    }

    /**
     * Handles LDAP comparison with a constant left part (MODEL, PROVIDER)
     *
     * @param node       Parsed terminal node (constant)
     * @param comparator Comparator to use
     * @param value      Value to compare to
     * @return The comparison as an {@link ILdapCriterion}
     * @throws IllegalArgumentException Invalid comparison
     */
    private ILdapCriterion handleComparisonWithConstant(final TerminalNode node, final LdapComparator comparator,
            final Object value) throws IllegalArgumentException {

        final IStringValue strValue;
        if (value instanceof IStringValue) {
            strValue = (IStringValue) value;
        } else {
            strValue = new PureString(String.valueOf(value));
        }

        switch (node.getSymbol().getType()) {
        case LdapFilterLexer.PACKAGE:
            // Target a model package URI
            if (value == null) {
                throw new IllegalArgumentException("Package URIs can't be null");
            } else if (value == Constants.ANY) {
                // Ignore test: model package URI are always set
                return null;
            }

            switch (comparator) {
                case APPROX:
                case EQUAL:
                    return new CriterionProviderPackageUri(strValue, comparator);

                default:
                    throw new IllegalArgumentException(
                            "Model package URIs can only be tested with ~= or =, not " + comparator);
            }

        case LdapFilterLexer.MODEL:
            // Target a model
            if (value == null) {
                throw new IllegalArgumentException("Models can't be null");
            } else if (value == Constants.ANY) {
                // Ignore test: model are always set
                return null;
            }

            switch (comparator) {
            case APPROX:
            case EQUAL:
                return new CriterionProviderModel(strValue, comparator);

            default:
                throw new IllegalArgumentException("Models can only be tested with ~= or =, not " + comparator);
            }

        case LdapFilterLexer.PROVIDER:
            // Target a provider
            if (value == null) {
                throw new IllegalArgumentException("Provider name can't be null");
            } else if (value == Constants.ANY) {
                // Ignore test: provider name is always set
                return null;
            }

            switch (comparator) {
            case APPROX:
            case EQUAL:
                return new CriterionProviderName(strValue, comparator);

            default:
                throw new IllegalArgumentException("Providers can only be tested with ~= or =, not " + comparator);
            }

        default:
            throw new IllegalArgumentException("Unsupported attribute type: " + node.getText());
        }
    }

    /**
     * Handles LDAP comparison with a resource path
     *
     * @param attr       Parsed attribute (resource path) context
     * @param comparator Comparator to use
     * @param value      Value to compare to
     * @return The comparison as an {@link ILdapCriterion}
     * @throws IllegalArgumentException Invalid comparison
     */
    private ILdapCriterion handlePathComparison(final AttrContext attr, final LdapComparator comparator,
            final Object value) throws IllegalArgumentException {

        // Walk through the resource parts
        final List<Valid_attrContext> ruleContexts = attr.getRuleContexts(Valid_attrContext.class);
        final String[] parts = new String[ruleContexts.size()];
        int i = 0;
        for (final Valid_attrContext partContext : ruleContexts) {
            if (partContext.STAR() != null) {
                // Replace "*" by null (any match)
                parts[i++] = null;
            } else {
                parts[i++] = partContext.getText();
            }
        }

        final SensiNactPath rcPath;
        switch (parts.length) {
        case 1:
            // Resource name
            rcPath = new SensiNactPath(null, parts[0]);
            break;

        case 2:
            // Service + resource name
            rcPath = new SensiNactPath(parts[0], parts[1]);
            break;

        default:
            throw new IllegalArgumentException("Unsupported LDAP target: " + attr.getText());
        }

        if (value == Constants.ANY) {
            return new CriterionResourcePresence(rcPath);
        } else {
            return new CriterionResourceOperator(rcPath, value, comparator);
        }
    }

    @Override
    public ILdapCriterion visitComparison(ComparisonContext ctx) {
        // Parse comparator
        final LdapComparator comparator = extractComparator(ctx.comparator());

        // Parse value
        final Object value = valueVisitor.visit(ctx);

        // Parse target
        final AttrContext attr = ctx.attr();
        final ParseTree attrFirstChild = attr.getChild(0);

        if (attrFirstChild instanceof TerminalNode) {
            // Got a constant keyword
            return handleComparisonWithConstant((TerminalNode) attrFirstChild, comparator, value);
        } else if (attrFirstChild instanceof Valid_attrContext) {
            // Got the "valid_attr" parser rule for the first child: get them all
            return handlePathComparison(attr, comparator, value);
        } else {
            throw new IllegalArgumentException("Unsupported parser context: " + attr.getText());
        }
    }
}
