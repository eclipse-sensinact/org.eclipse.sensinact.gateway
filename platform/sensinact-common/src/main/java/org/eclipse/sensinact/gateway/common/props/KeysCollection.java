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

package org.eclipse.sensinact.gateway.common.props;

import java.util.Set;

/**
 * A set of {@link TypedKey} 
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface KeysCollection
{
	/**
	 * Returns the array of this KeyCollection's key strings
	 * 
	 * @return
	 * 		 this KeyCollection's key strings
	 */
	Set<TypedKey<?>> keys();
	
	/**
	 * Returns the {@link TypedKey} whose name is passed
	 * as parameter
	 * 
	 * @return
	 * 		the {@link TypedKey} with the specified
	 * 		name
	 */
	TypedKey<?> key(String key);
	
}
