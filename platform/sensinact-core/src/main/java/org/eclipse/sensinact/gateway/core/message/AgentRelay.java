/*********************************************************************
* Copyright (c) 2020 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.core.message.whiteboard.AgentFactory;

/**
 * AgentRelay service is used by the {@link AgentFactory} to 
 * instantiate {@link SnaAgent}
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface AgentRelay extends MessageHandler{
	
	/**
	 * Returns the unique String identifier of the {@link Agent} 
	 * to be instantiated by the intermediate of this AgentRelay - If null
	 * the sensiNact platform will generate an random  String identifier
	 * 
	 * @return the unique String identifier of the {@link Agent}
	 * based on this AgentRelay
	 */
	String getRelayIdentifier();
	
	/**
	 * Defines the long life time of this AgentRelay and so of the {@link Agent} 
	 * instantiated by the intermediate of this AgentRelay - -1 means for ever 
	 * 
	 * @return the long life time of this AgentRelay
	 */
	long lifetime();
}
