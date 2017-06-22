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

package org.eclipse.sensinact.gateway.datastore.api;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Factory of {@link DataStoreService}
 */
public interface DataStoreServiceFactory 
{		
	/**
	 * @param mediator
	 * @return
	 * @throws UnableToFindDataStoreException 
	 */
	DataStoreService newInstance(Mediator mediator) 
			throws UnableToFindDataStoreException;	
}
