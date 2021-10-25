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

import java.util.logging.Logger;

/**
 * Constraint applying on {@link Comparable} objects
 *
 * @param <T> the extended {@link Comparable} type
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ConstraintOnComparable<T> implements Constraint {
    protected static final Logger LOGGER = Logger.getLogger(ConstraintOnComparable.class.getCanonicalName());

    /**
     * Checks whether the value object passed as parameter
     * complies to this constraint or not
     *
     * @param castedValue the value object casted into the appropriate
     *                    type
     * @return <ul>
     * <li>true if the value complies to this
     * constraint</li>
     * <li>false otherwise</li>
     * </ul>
     */
    protected abstract boolean doComplies(T castedValue);

    /**
     * String operator of this Constraint
     */
    protected final String operator;
    /**
     * Operand on which to base the constraint compliance evaluation
     */
    protected Comparable<T> operand;

    /**
     * Defines whether this constraint is the
     * logical complement of the raw defined constraint
     */
    protected final boolean complement;
    /**
     * Mediator used to interact with the OSGi host
     * environment
     */
    protected final ClassLoader classloader;

    /**
     * @param operator
     * @param operand
     * @throws InvalidConstraintDefinitionException
     */
    public ConstraintOnComparable(ClassLoader classloader, String operator, Comparable<T> operand, boolean complement) {
        this.classloader = classloader;
        this.operator = operator;
        this.operand = operand;
        this.complement = complement;
    }

    /**
     * @param operator
     * @param operandClass
     * @param operand
     * @throws InvalidConstraintDefinitionException
     */
    @SuppressWarnings("unchecked")
    public ConstraintOnComparable(ClassLoader classloader, String operator, Class<?> operandClass, Object operand, boolean complement) throws InvalidConstraintDefinitionException {
        this.classloader = classloader;
        this.operator = operator;
        Class<?> comparableClass = operandClass;

        if (!Comparable.class.isAssignableFrom(comparableClass) && (comparableClass = CastUtils.primitiveToComparable(operandClass)) == null) {
            throw new InvalidConstraintDefinitionException(new StringBuilder().append(operand.getClass().getCanonicalName()).append(" cannot be casted to Comparable").toString());
        }
        this.operand = (Comparable<T>) CastUtils.cast(comparableClass, operand);
        this.complement = complement;
    }

    /**
     * @inheritDoc
     * @see Constraint#
     * complies(java.lang.Object)
     */
    @Override
    public boolean complies(Object value) {
        if (value == null) {
            return false;
        }
        T castedValue = null;
        try {
            castedValue = (T) CastUtils.cast(this.operand.getClass(), value);

        } catch (ClassCastException e) {
            return false;
        }
        return this.doComplies(castedValue);
    }

    /**
     * @inheritDoc
     * @see Constraint#getOperator()
     */
    @Override
    public String getOperator() {
        return this.operator;
    }

    /**
     * @inheritDoc
     * @see Constraint#isComplement()
     */
    public boolean isComplement() {
        return this.complement;
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
        builder.append(TYPE_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(CastUtils.writeClass(this.operand.getClass()));
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(COMPLEMENT_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(this.isComplement());
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(OPERAND_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.toJSONFormat(this.operand));
        builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }
}
