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
package org.eclipse.sensinact.gateway.generic.parser;

/**
 * A {@link  ConstraintDefinition} holder
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ConstrainableDefinition {
    /**
     * Adds the {@link ConstraintDefinition} passed as parameter
     * to this ConstrainableDefinition
     *
     * @param constraint the {@link ConstraintDefinition} to be added
     */
    void addConstraint(ConstraintDefinition constraint);
}
