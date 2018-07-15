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
 * An MethodAccess defines an {@link AccessLevel} for an
 * {@link AccessMethod.Type}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface MethodAccess extends Nameable {
	/**
	 * The {@link AccessMethod.Type} of this MethodAccess
	 * 
	 * @return this MethodAccess's {@link AccessMethod.Type}
	 */
	AccessMethod.Type getMethod();

	/**
	 * The {@link AccessLevel} for this MethodAccess's {@link AccessMethod.Type}
	 * 
	 * @return this MethodAccess's {@link AccessLevel}
	 */
	AccessLevel getAccessLevel();
}
