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

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Length extends ConstraintOnCollectionSize {
    public static final String OPERATOR = "len";

    /**
     * Constructor
     *
     * @param operator String operator of this constraint
     * @param length   integer value on which will be based the compliance evaluation
     *                 of the size of a collection
     */
    public Length(int length, boolean complement) {
        super(OPERATOR, length, complement);
    }

    /**
     * @inheritDoc
     * @see ConstraintOnCollectionSize#
     * doComplies(int)
     */
    @Override
    protected boolean doComplies(int length) {
        return this.length == length;
    }

    /**
     * @inheritDoc
     * @see Constraint#getComplement()
     */
    @Override
    public Constraint getComplement() {
        Length complement = null;
        complement = new Length(super.length, !this.complement);
        return complement;
    }
}
