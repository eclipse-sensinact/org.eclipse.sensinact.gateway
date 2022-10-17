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
 * A set of {@link ProcessableData} processable by one {@link ResourceImpl} and
 * targeting one identified {@link ServiceImpl}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ServiceProcessableData<R extends ResourceProcessableData> extends ResourceProcessableContainer<R> {
	/**
	 * Returns the {@link Service}'s string identifier targeted by this
	 * PayloadFragment
	 * 
	 * @return the targeted {@link Service}'s identifier
	 */
	String getServiceId();
}
