/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
