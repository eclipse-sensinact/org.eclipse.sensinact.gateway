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

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

public class UnsupportedRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedRuleException(final String message) {
        super(message);
    }

    public UnsupportedRuleException(final String message, final Parser parser, final int ruleIdx) {
        this(String.format("%s: %s", message, parser.getRuleNames()[ruleIdx]));
    }

    public UnsupportedRuleException(final Parser parser, final int ruleIdx) {
        this("Unsupported parser rule", parser, ruleIdx);
    }

    public UnsupportedRuleException(final String message, final Parser parser, final ParserRuleContext ctx) {
        this(message, parser, ctx.getRuleIndex());
    }

    public UnsupportedRuleException(final Parser parser, final ParserRuleContext ctx) {
        this(parser, ctx.getRuleIndex());
    }

    public UnsupportedRuleException(final String message, final Parser parser, final ParseTree ctx) {
        this(message, parser, ((ParserRuleContext) ctx).getRuleIndex());
    }

    public UnsupportedRuleException(final Parser parser, final ParseTree ctx) {
        this(parser, ((ParserRuleContext) ctx).getRuleIndex());
    }
}
