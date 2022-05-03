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
 * A {@link ProcessaData} processable by a {@link ResourceImpl}
 */
public interface ResourceProcessableData extends ProcessableData {
	/**
	 * Returns the String identifier of the targeted {@link Attribute}
	 * 
	 * @return the String identifier of the targeted {@link Attribute}
	 */
	String getAttributeId();

	/**
	 * Returns the String identifier of the targeted {@link Metadata}
	 * 
	 * @return the String identifier of the targeted {@link Metadata}
	 */
	String getMetadataId();

	/**
	 * Returns the data object value of this ResourceProcessableData
	 * 
	 * @return the data object value
	 */
	Object getData();

	/**
	 * Returns the timestamp of the update of the targeted {@link Attribute} or
	 * {@link Metadata}
	 * 
	 * @return the update timestamp
	 */
	long getTimestamp();
}
