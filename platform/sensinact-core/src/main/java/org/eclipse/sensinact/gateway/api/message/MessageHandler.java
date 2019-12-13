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

import org.eclipse.sensinact.gateway.core.message.MidCallbackException;

/**
 * A MessageHandler handles event messages triggered by the sensiNact platform
 *
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface MessageHandler {
	
	/**
	 * Processes the {@link LifecycleMessage} passed as parameter
	 * 
	 * @param message the {@link LifecycleMessage} to be processed
	 */
	void doHandle(LifecycleMessageImpl message) throws MidCallbackException;

	/**
	 * Processes the {@link UpdateMessage} passed as parameter
	 * 
	 * @param message the {@link UpdateMessage} to be processed
	 */
	void doHandle(UpdateMessageImpl message) throws MidCallbackException;

	/**
	 * Processes the {@link RemoteMessage} passed as parameter
	 * 
	 * @param message the {@link RemoteMessage} to be processed
	 */
	void doHandle(RemoteMessageImpl message) throws MidCallbackException;

	/**
	 * Processes the {@link ErrorMessage} passed as parameter
	 * 
	 * @param message the {@link ErrorMessage} to be processed
	 */
	void doHandle(ErrorMessageImpl message) throws MidCallbackException;

	/**
	 * Processes the {@link ResponseMessage} passed as parameter
	 * 
	 * @param message the {@link ResponseMessage} to be processed
	 */
	void doHandle(ResponseMessage<?, ?> message) throws MidCallbackException;
	
	/**
	 * Returns true if this {@link MessageHandler} is propagated through the 
	 * connected remote sensiNact instance(s). False if only  local events are 
	 * observed.
	 */
	boolean propagate();
	
}
