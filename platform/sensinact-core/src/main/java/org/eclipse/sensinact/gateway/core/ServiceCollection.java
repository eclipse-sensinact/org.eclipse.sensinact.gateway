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

/**
 * A collection of {@link Resource} service
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ServiceCollection {
	/**
	 * Returns the set of all {@link Service}s of this collection
	 * 
	 * @return the set of held {@link Service}s
	 */
	List<Service> getServices();

	/**
	 * Returns the {@link Service} held by this collection and whose name is passed
	 * as parameter
	 * 
	 * @param service
	 *            the name of the {@link Service}
	 * @return the {@link Service} of this collection with the specified name
	 */
	Service getService(String service);
}
