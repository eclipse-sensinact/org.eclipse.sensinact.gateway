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

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;

/**
 * Extended {@link DataResource} whose type is SENSOR and whose 'value'
 * {@link Attribute} cannot be modified by default
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SensorDataResource extends DataResource {
	/**
	 * The sensor data resource type.
	 */
	public static final Resource.Type TYPE_VALUE = Type.SENSOR;

	// Constant name is the concatenation the name of the targeted
	// AttributeBuilder's field and of the associated requirement
	// constant value
	public static final Modifiable VALUE_MODIFIABLE = Modifiable.UPDATABLE;
}
