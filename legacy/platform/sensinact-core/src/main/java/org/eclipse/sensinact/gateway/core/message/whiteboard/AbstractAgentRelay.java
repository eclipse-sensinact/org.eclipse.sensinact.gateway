/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message.whiteboard;

import org.eclipse.sensinact.gateway.core.message.AgentRelay;
import org.eclipse.sensinact.gateway.core.message.MessageFilterDefinition;
import org.eclipse.sensinact.gateway.core.message.MidCallback;
import org.eclipse.sensinact.gateway.core.message.MidCallbackException;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaRemoteMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;

/**
 * Abstract {@link AgentRelay} implementation allowing to only override useful methods.
 * 
 * Extended AbstractAgentRelay can also implement the {@link MessageFilterDefinition},
 * or be annotated by a {@link Filter} annotation, if the received messages must 
 * be filtered 
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public abstract class AbstractAgentRelay implements AgentRelay {

	@Override
	public boolean propagate() {
		return false;
	}

	@Override
	public String getRelayIdentifier() {
		return null;
	}

	@Override
	public long lifetime() {
		return MidCallback.ENDLESS;
	}

	@Override
	public void doHandle(SnaLifecycleMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	@Override
	public void doHandle(SnaUpdateMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	@Override
	public void doHandle(SnaRemoteMessageImpl message) throws MidCallbackException {	
		//to be overridden	
	}

	@Override
	public void doHandle(SnaErrorMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	@Override
	public void doHandle(SnaResponseMessage<?, ?> message) throws MidCallbackException {
		//to be overridden
	}
}
