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

import org.eclipse.sensinact.gateway.api.message.MessageCallback;
import org.eclipse.sensinact.gateway.api.message.ErrorMessageImpl;
import org.eclipse.sensinact.gateway.api.message.LifecycleMessageImpl;
import org.eclipse.sensinact.gateway.api.message.RemoteMessageImpl;
import org.eclipse.sensinact.gateway.api.message.ResponseMessage;
import org.eclipse.sensinact.gateway.api.message.UpdateMessageImpl;
import org.eclipse.sensinact.gateway.core.message.MidCallbackException;

/**
 * Abstract {@link AgentRelay} implementation allowing to only override
 * useful methods.
 * 
 * Extended AbstractAgentRelay can also implement the {@link MessageFilterDefinition},
 * or be annotated by a {@link Filter} annotation, if the received messages must 
 * be filtered 
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public class AbstractAgentRelay implements AgentRelay {

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessageObserver#doHandle(org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl)
	 */
	@Override
	public void doHandle(LifecycleMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessageObserver#doHandle(org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl)
	 */
	@Override
	public void doHandle(UpdateMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessageObserver#doHandle(org.eclipse.sensinact.gateway.core.message.SnaRemoteMessageImpl)
	 */
	@Override
	public void doHandle(RemoteMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessageObserver#doHandle(org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl)
	 */
	@Override
	public void doHandle(ErrorMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessageObserver#doHandle(org.eclipse.sensinact.gateway.core.message.SnaResponseMessage)
	 */
	@Override
	public void doHandle(ResponseMessage<?, ?> message) throws MidCallbackException {
		//to be overridden
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessageObserver#propagate()
	 */
	@Override
	public boolean propagate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.whiteboard.AgentRelay#getRelayIdentifier()
	 */
	@Override
	public String getRelayIdentifier() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.whiteboard.AgentRelay#getPublicKey()
	 */
	@Override
	public String getPublicKey() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.whiteboard.AgentRelay#lifetime()
	 */
	@Override
	public long lifetime() {
		return MessageCallback.ENDLESS;
	}
}
