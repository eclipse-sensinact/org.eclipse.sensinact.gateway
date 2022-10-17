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

import org.eclipse.sensinact.gateway.util.CastUtils;

import java.lang.reflect.Array;

/**
 * {@link ConstraintFactory} for the {@link Constraint}s defined
 * by the system
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DefaultConstraintFactory implements ConstraintFactory {
    /**
     * Condition possible types enumeration
     */
    public static enum Type {
        ABSOLUTE(Absolute.OPERATOR), CHANGED(Changed.OPERATOR), DELTA(Delta.OPERATOR), DIFFERENT(Different.OPERATOR), ENUMERATION(Contains.OPERATOR), EXISTS(Exists.OPERATOR), FIXED(Fixed.OPERATOR), LENGTH(Length.OPERATOR), MAXEXCLUSIVE(MaxExclusive.OPERATOR), MAXINCLUSIVE(MaxInclusive.OPERATOR), MINEXCLUSIVE(MinExclusive.OPERATOR), MININCLUSIVE(MinInclusive.OPERATOR), MAXLENGTH(MaxLength.OPERATOR), MINLENGTH(MinLength.OPERATOR), PATTERN(Pattern.OPERATOR), TRUE(True.OPERATOR);

        /**
         * Returns the Restriction.Type whose string
         * operator is passed as parameter
         *
         * @param operator the string operator of the Restriction.Type
         *                 to retrieve
         * @return the Restriction.Type whose string
         * operator is passed as parameter
         */
        public static Type fromOperator(String operator) {
            DefaultConstraintFactory.Type[] values = DefaultConstraintFactory.Type.values();

            int index = 0;
            int length = values.length;

            for (; index < length; index++) {
                if (values[index].getOperator().intern() == operator.intern()) {
                    return values[index];
                }
            }
            return null;
        }

        private final String operator;

        /**
         * Constructor
         *
         * @param operator
         */
        Type(String operator) {
            this.operator = operator;
        }

        /**
         * @return
         */
        public String getOperator() {
            return this.operator;
        }
    }

    /**
     * @inheritDoc
     * @see ConstraintFactory#
     * newInstance(java.lang.String, java.lang.Class, java.lang.Object, boolean)
     */
    @Override
    public Constraint newInstance(String constraint, Class<?> referenceType, Object reference, boolean complement) throws InvalidConstraintDefinitionException {
        Type type = null;

        if ((type = valueOf(constraint.toUpperCase())) == null && (type = Type.fromOperator(constraint)) == null) {
            throw new InvalidConstraintDefinitionException(new StringBuilder().append("Unknown constraint :").append(constraint).toString());
        }
        Constraint instance = null;
        switch (type) {
            case ABSOLUTE:
                if (!reference.getClass().isArray() || Array.getLength(reference) != 2) {
                    throw new InvalidConstraintDefinitionException(new StringBuilder().append("Invalid constraint definition :  ").append("array of Numbers expected").toString());
                }
                instance = new Absolute(Array.get(reference, 0), Array.get(reference, 1), complement);
                break;
            case CHANGED:
                instance = new Changed(complement);
                break;
            case DELTA:
                if (!reference.getClass().isArray() || Array.getLength(reference) != 2) {
                    throw new InvalidConstraintDefinitionException(new StringBuilder().append("Invalid constraint definition :  ").append("array of Numbers expected").toString());
                }
                instance = new Delta(Array.get(reference, 0), Array.get(reference, 1), complement);
                break;
            case DIFFERENT:
                instance = new Different(referenceType, reference, complement);
                break;
            case EXISTS:
                instance = new Exists(complement);
                break;
            case FIXED:
                instance = new Fixed(referenceType, reference, complement);
                break;
            case MAXEXCLUSIVE:
                instance = new MaxExclusive(referenceType, reference, complement);
                break;
            case MAXINCLUSIVE:
                instance = new MaxInclusive(referenceType, reference, complement);
                break;
            case MINEXCLUSIVE:
                instance = new MinExclusive(referenceType, reference, complement);
                break;
            case MININCLUSIVE:
                instance = new MinInclusive(referenceType, reference, complement);
                break;
            case ENUMERATION:
                instance = new Contains(referenceType, reference, complement);
                break;
            case LENGTH:
                referenceType = int.class;
                instance = new Length(CastUtils.cast(int.class, reference), complement);
                break;
            case MAXLENGTH:
                referenceType = int.class;
                instance = new MaxLength(CastUtils.cast(int.class, reference), complement);
                break;
            case MINLENGTH:
                referenceType = int.class;
                instance = new MinLength(CastUtils.cast(int.class, reference), complement);
                break;
            case PATTERN:
                referenceType = String.class;
                instance = new Pattern(String.valueOf(reference), complement);
                break;
            case TRUE:
                instance = new True(complement);
                break;
            default:
                return null;
        }
        return instance;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.constraint.common.constraint.ConstraintFactory#
     * handle(java.lang.String)
     */
    public boolean handle(String constraint) {

        return (valueOf(constraint.toUpperCase()) != null || Type.fromOperator(constraint) != null);
    }

    private final static Type valueOf(String constraint) {
        Type type = null;
        try {
            type = Type.valueOf(constraint);

        } catch (IllegalArgumentException e) {
            //do nothing
        }
        return type;
    }

}
