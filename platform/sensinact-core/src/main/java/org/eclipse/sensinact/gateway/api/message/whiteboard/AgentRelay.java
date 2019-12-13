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
package org.eclipse.sensinact.gateway.api.message.whiteboard;

import org.eclipse.sensinact.gateway.api.message.MessageHandler;

/**
 * AgentRelay service is used by the {@link AgentFactory} to 
 * instantiate {@link SnaAgent}
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface AgentRelay extends MessageHandler {

	/**
	 * Returns the unique String identifier of the {@link SnaAgent} 
	 * to be instantiated by the intermediate of this AgentRelay - If null
	 * the sensiNact platform will generate an random  String identifier
	 * 
	 * @return the unique String identifier of the {@link SnaAgent}
	 * based on this AgentRelay
	 */
	String getRelayIdentifier();
	
	/**
	 * Returns the String public key allowing to define access rights to 
	 * the data structures of the platform (and so to the messages they emit) - 
	 * If null the anonymous user access rights are used
	 * 
	 * @return the String public key allowing to define access rights
	 * to the emit messages
	 */
	String getPublicKey();
	
	/**
	 * Defines the long life time of this AgentRelay and so of the {@link SnaAgent} 
	 * instantiated by the intermediate of this AgentRelay - -1 means for ever 
	 * 
	 * @return the long life time of this AgentRelay
	 */
	long lifetime();
}
