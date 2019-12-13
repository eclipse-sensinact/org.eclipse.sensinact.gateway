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
package org.eclipse.sensinact.gateway.core.security;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;

/**
 * An MethodAccessibility defines whether an {@link AccessMethod.Type} is
 * accessible for a specific {@link AccessLevelOption}
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface MethodAccessibility extends Nameable {
	/**
	 * The {@link AccessMethod.Type} of this MethodAccessibility
	 * 
	 * @return this MethodAccessibility's {@link AccessMethod.Type}
	 */
	AccessMethod.Type getMethod();

	/**
	 * The {@link AccessLevelOption} of this MethodAccessibility
	 * 
	 * @return this MethodAccessibility's {@link AccessLevelOption}
	 */
	AccessLevelOption getAccessLevelOption();

	/**
	 * Returns true if the method held by this MethodAccessibility is accessible;
	 * false otherwise
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if the method is accessible</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean isAccessible();
}
