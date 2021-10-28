/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.common.constraint;

import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

/**
 * Constraint based on a regular expression
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Pattern implements Constraint {
    protected static final Logger LOGGER = Logger.getLogger(Pattern.class.getCanonicalName());

    public static final String OPERATOR = "regex";
    /**
     * Operand on which to base the constraint compliance evaluation
     */
    protected final String operand;

    protected final boolean complement;

    /**
     * @param operator
     * @param operand
     * @throws InvalidConstraintDefinitionException
     */
    public Pattern(String operand, boolean complement) throws InvalidConstraintDefinitionException {
        this.complement = complement;

        if ((operand == null) || (operand.length() == 0)) {
            throw new InvalidConstraintDefinitionException("Unable to create a regular expression using an empty or null string");
        }
        try {
            java.util.regex.Pattern.compile(operand);
        } catch (PatternSyntaxException e) {
            throw new InvalidConstraintDefinitionException(e);
        }
        this.operand = operand;
    }

    /**
     * @inheritDoc
     * @see Constraint#getOperator()
     */
    @Override
    public String getOperator() {
        return OPERATOR;
    }

    /**
     * @inheritDoc
     * @see Constraint#isComplement()
     */
    @Override
    public boolean isComplement() {
        return this.complement;
    }

    /**
     * @inheritDoc
     * @see Constraint#complies(java.lang.Object)
     */
    @Override
    public boolean complies(Object value) {
        boolean complies = false;
        if (value == null) {
            return complies;
        }
        String sequence = null;
        try {
            sequence = CastUtils.cast(String.class, value);
        } catch (ClassCastException e) {
            return complies;
        }
        return sequence.matches(this.operand) ^ isComplement();
    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.QUOTE);
        builder.append(OPERATOR_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(this.getOperator());
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(COMPLEMENT_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(this.complement);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(OPERAND_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(this.operand);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }

    /**
     * @inheritDoc
     * @see Constraint#getComplement()
     */
    @Override
    public Constraint getComplement() {
        Pattern complement = null;
        try {
            complement = new Pattern(this.operand, !this.complement);
        } catch (InvalidConstraintDefinitionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return complement;
    }
}
