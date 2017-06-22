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

import java.util.Set;

import org.eclipse.sensinact.gateway.core.method.AccessMethod;

/**
 * An AccessProfile maps {@link AccessLevel}s to the set of existing 
 * {@link AccessMethod.Type}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessProfile
{
	/**
	 * Returns the set of {@link MethodAccess} this AccessProfile
	 * gathers
	 * 
	 * @return this AccessProfile's {@link MethodAccess}es
	 */
	Set<MethodAccess> getMethodAccesses();
}
