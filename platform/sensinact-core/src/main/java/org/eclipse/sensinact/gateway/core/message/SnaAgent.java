/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
