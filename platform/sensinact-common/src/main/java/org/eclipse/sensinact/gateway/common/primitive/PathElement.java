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
package org.eclipse.sensinact.gateway.common.primitive;


/**
 * A PathElement service belongs to a hierarchy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface PathElement
{
	/**
	 * Returns the string uri path of this 
	 * PathElement service
	 * 
	 * @return
	 *     the uri path of this PathElement 
	 *     service
	 */
	 String getPath();
}
