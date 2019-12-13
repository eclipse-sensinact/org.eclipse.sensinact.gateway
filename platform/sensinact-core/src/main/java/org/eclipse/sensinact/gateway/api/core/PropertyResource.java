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
package org.eclipse.sensinact.gateway.api.core;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;

/**
 * Extended {@link DataResource} whose type is PROPERTY and whose 'value'
 * {@link Attribute} can be modified by default
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface PropertyResource extends DataResource {
	/**
	 * The property resource type.
	 */
	public static final Type TYPE_VALUE = Type.PROPERTY;

	// Constant name is the concatenation the name of the targeted
	// AttributeBuilder's field and of the associated requirement
	// constant value
	public static final Modifiable DEFAULT_VALUE_MODIFIABLE = Modifiable.MODIFIABLE;
}
