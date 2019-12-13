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
package org.eclipse.sensinact.gateway.api.message;

/**
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface MessageSubType {
	/**
	 * Returns the {@link SnaMessage.Type} to which this SnaMessageSubType belongs
	 * to
	 * 
	 * @return the the {@link SnaMessage.Type} of this SnaMessageSubType
	 */
	SnaMessage.Type getSnaMessageType();
}
