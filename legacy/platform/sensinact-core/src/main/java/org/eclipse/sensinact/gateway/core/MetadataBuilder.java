/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
/**
 * 
 */
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * A service allowing to build a Metadata of an {@link Attribute}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface MetadataBuilder extends Nameable {
	/**
	 * Creates and returns an new {@link Metadata} based on this MetadataBuilder
	 * 
	 * @return the new created {@link Metadata}
	 * 
	 * @throws InvalidValueException
	 *             if an error occurred while creating the {@link Metadata}
	 */
	Metadata getMetadata() throws InvalidValueException;

}
