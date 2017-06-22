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

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;


/**
 * Extended {@link DataResource} whose type is SENSOR and whose 'value'
 * {@link Attribute} cannot be modified by default
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SensorDataResource extends DataResource
{
	/**
	 * The sensor data resource type.
	 */
	public static final Resource.Type TYPE_VALUE = Type.SENSOR;

	// Constant name is the concatenation the name of the targeted
	// AttributeBuilder's field and of the associated requirement
	// constant value
	public static final Modifiable VALUE_MODIFIABLE = Modifiable.UPDATABLE;
}
