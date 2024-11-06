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
package org.eclipse.sensinact.filters.ldap.antlr.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.AlphaContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.AnyValueContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.BooleanValueContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.Escaped_hexContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.Escaped_quoteContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.NullValueContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.NumericValueContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.PureStringContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.QuotedStringContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParser.RegexStringContext;
import org.eclipse.sensinact.filters.ldap.antlr.LdapFilterParserBaseVisitor;

/**
 * Parses string-related rules
 */
public class ValueVisitor extends LdapFilterParserBaseVisitor<Object> {

    @Override
    public Object visitAlpha(AlphaContext ctx) {
        return ctx.getText();
    }

    @Override
    public String visitEscaped_hex(Escaped_hexContext ctx) {
        return String.valueOf((char) Integer.parseInt(ctx.getText().substring(1), 16));
    }

    @Override
    public String visitEscaped_quote(Escaped_quoteContext ctx) {
        return "\"";
    }

    private IStringValue stringFromParseTree(final ParseTree tree) {
        final Object result = visit(tree);
        if (result == null) {
            return new PureString(tree.getText());
        } else if (result instanceof IStringValue) {
            return (IStringValue) result;
        } else {
            return new PureString(String.valueOf(result));
        }
    }

    @Override
    public String visitPureString(PureStringContext ctx) {
        final StringBuilder builder = new StringBuilder();

        final int nbChildren = ctx.getChildCount();
        for (int i = 0; i < nbChildren; i++) {
            // Either ParserRuleContext or TerminalNode
            final ParseTree child = ctx.getChild(i);
            builder.append(stringFromParseTree(child).getString());
        }
        return builder.toString();
    }

    @Override
    public String visitQuotedString(QuotedStringContext ctx) {
        final StringBuilder builder = new StringBuilder();

        final int nbChildren = ctx.getChildCount();
        // Ignore first and last quote children
        for (int i = 1; i < nbChildren - 1; i++) {
            final ParseTree child = ctx.getChild(i);
            builder.append(stringFromParseTree(child).getString());
        }
        return builder.toString();
    }

    @Override
    public Number visitNumericValue(NumericValueContext ctx) {
        final String strValue = ctx.getText();
        if (strValue.contains(".")) {
            // Decimal value
            return Double.parseDouble(strValue);
        } else {
            return Integer.parseInt(strValue);
        }
    }

    @Override
    public Boolean visitBooleanValue(BooleanValueContext ctx) {
        return Boolean.valueOf(ctx.getText().toLowerCase());
    }

    @Override
    public Object visitNullValue(NullValueContext ctx) {
        return null;
    }

    @Override
    public Constants visitAnyValue(AnyValueContext ctx) {
        return Constants.ANY;
    }

    @Override
    public IStringValue visitRegexString(RegexStringContext ctx) {
        // Visit the "pure" part, if any (can be null if we start with a "*")
        final String pureString;
        if (ctx.pureString() != null) {
            pureString = visitPureString(ctx.pureString());
        } else {
            pureString = null;
        }

        // Handle the queue
        final List<RegexStringContext> regexStrings = ctx.regexString();
        if (regexStrings.isEmpty()) {
            // No right part...
            if (!ctx.STAR().isEmpty()) {
                // ... but ends with a star
                return new RegexString(Pattern.compile(Pattern.quote(pureString) + ".*"));
            } else {
                // Return the string as is
                return new PureString(pureString);
            }
        }

        // Quote the pure string
        final List<String> patterns = new ArrayList<>(regexStrings.size() + 1);
        if(pureString != null) {
            // Started with a pure string
        patterns.add(Pattern.quote(pureString));
        } else {
            // We got a star first if we're here, so an empty head (join() will add ".*")
            patterns.add("");
        }
        for (final RegexStringContext subContext : regexStrings) {
            final IStringValue subValue = visitRegexString(subContext);
            if (subValue.isRegex()) {
                patterns.add(subValue.getPattern());
            } else {
                patterns.add(subValue.getString());
            }
        }

        return new RegexString(String.join(".*", patterns));
    }
}
