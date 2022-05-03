/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.primitive.ProcessableData;

/**
 * A {@link ProcessableData} targeting one {@link ServiceProviderImpl}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ServiceProviderProcessableData<S extends ServiceProcessableData<?>>
		extends ServiceProcessableContainer<S> {
	/**
	 * Returns the string identifier of a {@link ServiceProvider} targeted by this
	 * SubPacket
	 * 
	 * @return the identifier of the targeted {@link ServiceProvider}
	 */
	public String getServiceProviderIdentifier();

}
