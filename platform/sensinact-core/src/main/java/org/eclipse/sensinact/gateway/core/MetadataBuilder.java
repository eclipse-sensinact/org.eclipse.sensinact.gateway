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
