/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.common.constraint;

import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Constraint applying on the size of a set of data
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ConstraintOnCollectionSize implements Constraint {
    protected static final Logger LOGGER = Logger.getLogger(ConstraintOnCollectionSize.class.getCanonicalName());

    /**
     * Checks whether the length of the evaluated collection complies to this
     * constraint or not
     *
     * @param length the length of the evaluated collection
     * @return <ul>
     * <li>true if the collection's length complies this constraint</li>
     * <li>false otherwise</li>
     * </ul>
     */
    protected abstract boolean doComplies(int length);


    /**
     * String operator of this Constraint
     */
    protected final String operator;
    /**
     * Size of the collection
     */
    protected final int length;
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
     * Constructor
     *
     * @param operator String operator of this constraint
     * @param length   integer value on which will be based the compliance evaluation
     *                 of the size of a collection
     */
    public ConstraintOnCollectionSize(ClassLoader classloader, String operator, int length, boolean complement) {
        this.classloader = classloader;
        this.operator = operator;
        this.length = length;
        this.complement = complement;
    }

    /**
     * Constructor
     *
     * @param operator String operator of this constraint
     * @param length   value object on which will be based the compliance evaluation
     *                 of the size of a collection
     * @throws InvalidConstraintDefinitionException if the length object cannot be cast into an integer
     */
    public ConstraintOnCollectionSize(ClassLoader classloader, String operator, Object length, boolean complement) throws InvalidConstraintDefinitionException {
        this.classloader = classloader;
        this.operator = operator;
        this.complement = complement;
        try {
            this.length = CastUtils.cast(this.classloader, int.class, length);
        } catch (ClassCastException e) {
            throw new InvalidConstraintDefinitionException(e);
        }
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
     * @see Constraint#complies(java.lang.Object)
     */
    @Override
    public boolean complies(Object value) {
        boolean complies = false;
        if (value == null) {
            return complies;
        }
        if (Collection.class.isAssignableFrom(value.getClass())) {
            complies = this.doComplies(((Collection<?>) value).size());
        } else if (value.getClass().isArray()) {
            complies = this.doComplies(Array.getLength(value));
        } else {
            try {
                Object[] array = CastUtils.castArray(this.classloader, Object[].class, value);

                complies = this.complies(array);

            } catch (ClassCastException e) {
                if (value.getClass() == String.class) {
                    complies = this.doComplies(((String) value).length());
                }
                // do nothing
            }
        }
        return complies ^ isComplement();
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
        builder.append(JSONUtils.toJSONFormat(this.length));
        builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }
}
