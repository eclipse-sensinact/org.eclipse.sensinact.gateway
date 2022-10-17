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

import org.eclipse.sensinact.gateway.common.primitive.ProcessableContainer;
import org.eclipse.sensinact.gateway.common.primitive.ProcessableData;

/**
 * A set of {@link ProcessableData} targeting one identified
 * {@link ResourceImpl}
 */
public interface ResourceProcessableContainer<R extends ResourceProcessableData>
		extends ProcessableData, ProcessableContainer<R> {
	/**
	 * Returns the String identifier of the targeted {@link ResourceImpl}
	 * 
	 * @return the String identifier of the targeted {@link ResourceImpl}
	 */
	String getResourceId();
}
