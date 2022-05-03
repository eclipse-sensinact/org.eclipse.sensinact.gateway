/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaMessageSubType {
	/**
	 * Returns the {@link SnaMessage.Type} to which this SnaMessageSubType belongs
	 * to
	 * 
	 * @return the the {@link SnaMessage.Type} of this SnaMessageSubType
	 */
	SnaMessage.Type getSnaMessageType();
}
