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

import jakarta.json.JsonValue;

/**
 * Maps a constant object value to a {@link Constraint} instance.
 * This data structure is used in a list to replace a Map in which
 * constraints are unordered
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ConstraintConstantPair {
    public final Constraint constraint;
    public final Object constant;

    /**
     * Constructor
     *
     * @param constraint the  {@link Constraint} of the pair to instantiate
     * @param constant   the constant object of the pair to instantiate
     */
    public ConstraintConstantPair(Constraint constraint, JsonValue constant) {
        this.constraint = constraint;
        this.constant = Constraint.toConstantValue(constant);
    }

	
}
