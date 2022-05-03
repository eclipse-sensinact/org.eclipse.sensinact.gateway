/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.constraint;

import org.eclipse.sensinact.gateway.util.JSONUtils;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Combination of constraints tool
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Expression extends LinkedList<Constraint> implements Constraint {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static enum LogicalOperator {
        AND, OR;
    }

    private final boolean complement;
    private final LogicalOperator operator;

    /**
     * Constructor
     *
     * @param operator the {@link LogicalOperator} of the
     *                 {@link Constraint} Expression to instantiate
     */
    public Expression(LogicalOperator operator) {
        this(operator, false);
    }

    /**
     * Constructor
     *
     * @param operator   the {@link LogicalOperator} of the
     *                   {@link Constraint} Expression to instantiate
     * @param complement
     */
    public Expression(LogicalOperator operator, boolean complement) {
        this.operator = operator;
        this.complement = complement;
    }

    /**
     * @inheritDoc
     * @see Constraint#getOperator()
     */
    public String getOperator() {
        return this.operator.name();
    }

    /**
     * @inheritDoc
     * @see Constraint#complies(java.lang.Object)
     */
    public boolean complies(Object value) {
        Boolean result = null;
        Iterator<Constraint> iterator = super.iterator();
        while (iterator.hasNext()) {
            Constraint constraint = iterator.next();
            boolean complies = constraint.complies(value);

            if (complies) {
                if (this.operator.equals(LogicalOperator.OR)) {
                    result = new Boolean(true);
                    break;
                }
            } else {
                if (this.operator.equals(LogicalOperator.AND)) {
                    result = new Boolean(false);
                    break;
                }
            }
        }
        if (result == null) {
            result = new Boolean(!this.operator.equals(LogicalOperator.OR));
        }
        return result.booleanValue() ^ complement;
    }

    /**
     * @inheritDoc
     * @see Constraint#getComplement()
     */
    public Constraint getComplement() {
        Expression complement = new Expression(this.operator.equals(LogicalOperator.AND) ? LogicalOperator.OR : LogicalOperator.AND
                /*, !this.isComplement()*/);

        Iterator<Constraint> iterator = super.iterator();
        while (iterator.hasNext()) {
            Constraint constraint = iterator.next();
            complement.add(constraint.getComplement());
        }
        return complement;

    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        int index = 0;
        StringBuilder builder = new StringBuilder();
//		builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.OPEN_BRACKET);
//		builder.append(JSONUtils.QUOTE);
//		builder.append("operator");
//		builder.append(JSONUtils.QUOTE);
//		builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(this.operator.name());
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
//		builder.append(JSONUtils.QUOTE);
//		builder.append("complement");
//		builder.append(JSONUtils.QUOTE);
//		builder.append(JSONUtils.COLON);
//		builder.append(this.isComplement());
//		builder.append(JSONUtils.COMMA);
//		builder.append(JSONUtils.QUOTE);
//		builder.append("constraints");
//		builder.append(JSONUtils.QUOTE);
//		builder.append(JSONUtils.COLON);
//		builder.append(JSONUtils.OPEN_BRACKET);		
        Iterator<Constraint> iterator = super.iterator();
        while (iterator.hasNext()) {
            builder.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
            builder.append(iterator.next().getJSON());
            index++;
        }
        builder.append(JSONUtils.CLOSE_BRACKET);
//		builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }

    /**
     * @inheritDoc
     * @see Constraint#isComplement()
     */
    @Override
    public boolean isComplement() {
        return this.complement;
    }
}
