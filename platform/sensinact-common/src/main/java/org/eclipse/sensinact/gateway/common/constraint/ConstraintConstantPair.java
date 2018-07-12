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
    public ConstraintConstantPair(Constraint constraint, Object constant) {
        this.constraint = constraint;
        this.constant = constant;
    }
}
