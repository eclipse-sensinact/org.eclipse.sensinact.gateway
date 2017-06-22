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
package org.eclipse.sensinact.gateway.generic.parser;

/**
 *	A XmlDefinition on which constraints can apply
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ConstrainableDefinition 
{
	/**
	 * Adds the {@link ConstraintDefinition} passed as parameter
	 * to this ConstrainableDefinition
	 * 
	 * @param constraint
	 * 		the {@link ConstraintDefinition} to add
	 */
	void addConstraint(ConstraintDefinition constraint);
}
