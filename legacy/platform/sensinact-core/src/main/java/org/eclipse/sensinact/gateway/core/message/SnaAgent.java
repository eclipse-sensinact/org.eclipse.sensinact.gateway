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
 * Recipient of the messages of a sensiNact gateway instance
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaAgent extends MessageRegisterer {
	
	/**
	 * Returns the String public key of this SnaAgent used to defined 
	 * the access level applying on the propagated messages
	 * 
	 * @return this SnaAgent's String public key
	 */
	String getPublicKey();
	
	/**
	 * Starts this SnaAgent and registers it into the OSGi host 
	 * environment's registry 
	 */
	void start();

	/**
	 * Stops this SnaAgent and unregisters it from the OSGi host 
	 * environment's registry 
	 */
	 void stop();
}
