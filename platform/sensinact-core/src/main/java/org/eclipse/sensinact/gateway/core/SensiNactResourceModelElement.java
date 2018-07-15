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
package org.eclipse.sensinact.gateway.core;

import java.util.List;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;

/**
 * The sensiNact resource model element definition
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SensiNactResourceModelElement<M extends ModelElementProxy> {
	/**
	 * Creates and returns an {@link ModelElementProxy} of this
	 * {@link SensiNactResourceModelElement}
	 * 
	 * @param methodAccessibilities
	 *            the array of {@link MethodAccessibility}s applying on the
	 *            {@link ModelElementProxy} to be built
	 * 
	 * @return a new {@link SensiNactResourceModelElementProxy}
	 */
	M getProxy(List<MethodAccessibility> methodAccessibilities);

}
