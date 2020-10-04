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

import org.eclipse.sensinact.gateway.core.message.whiteboard.AgentFactory;

/**
 * AgentRelay service is used by the {@link AgentFactory} to 
 * instantiate {@link SnaAgent}
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface MessageHandler {

	/**
	 * Processes the {@link SnaLifecycleMessageImpl} passed as parameter
	 * 
	 * @param message
	 *            the {@link SnaLifecycleMessageImpl} to be processed
	 */
	void doHandle(SnaLifecycleMessageImpl message) throws MidCallbackException;

	/**
	 * Processes the {@link SnaUpdateMessageImpl} passed as parameter
	 * 
	 * @param message
	 *            the {@link SnaUpdateMessageImpl} to be processed
	 */
	void doHandle(SnaUpdateMessageImpl message) throws MidCallbackException;

	/**
	 * Processes the {@link SnaRemoteMessageImpl} passed as parameter
	 * 
	 * @param message
	 *            the {@link SnaRemoteMessageImpl} to be processed
	 */
	void doHandle(SnaRemoteMessageImpl message) throws MidCallbackException;

	/**
	 * Processes the {@link SnaErrorMessageImpl} passed as parameter
	 * 
	 * @param message
	 *            the {@link SnaErrorMessageImpl} to be processed
	 */
	void doHandle(SnaErrorMessageImpl message) throws MidCallbackException;

	/**
	 * Processes the {@link SnaResponseMessage} passed as parameter
	 * 
	 * @param message
	 *            the {@link SnaResponseMessage} to be processed
	 */
	void doHandle(SnaResponseMessage<?, ?> message) throws MidCallbackException;

	/**
	 * Returns true if the {@link SnaAgent} attached to this
	 * {@link MessageHandler} must be propagated through the 
	 * connected remote sensiNact instance(s). False if the 
	 * {@link SnaAgent} observes local events only.
	 */
	boolean propagate();
}
