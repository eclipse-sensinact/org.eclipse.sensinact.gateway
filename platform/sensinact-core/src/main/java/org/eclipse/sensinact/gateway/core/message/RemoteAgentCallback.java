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
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.api.message.AbstractMessageAgentCallback;
import org.eclipse.sensinact.gateway.api.message.ErrorMessageImpl;
import org.eclipse.sensinact.gateway.api.message.LifecycleMessageImpl;
import org.eclipse.sensinact.gateway.api.message.ResponseMessage;
import org.eclipse.sensinact.gateway.api.message.UpdateMessageImpl;
import org.eclipse.sensinact.gateway.core.remote.RemoteCore;

public class RemoteAgentCallback extends AbstractMessageAgentCallback {
	
	private final RemoteCore remoteCore;

	public RemoteAgentCallback(String identifier, RemoteCore remoteCore) {
		super(identifier);
		this.remoteCore = remoteCore;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#
	 * doHandle(org.eclipse.sensinact.gateway.api.message.LifecycleMessageImpl)
	 */
	@Override
	public void doHandle(LifecycleMessageImpl message) {
		this.remoteCore.endpoint().dispatch(super.identifier, message);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#
	 * doHandle(org.eclipse.sensinact.gateway.api.message.UpdateMessageImpl)
	 */
	@Override
	public void doHandle(UpdateMessageImpl message) {
		this.remoteCore.endpoint().dispatch(super.identifier, message);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#
	 * doHandle(org.eclipse.sensinact.gateway.api.message.ErrorMessageImpl)
	 */
	@Override
	public void doHandle(ErrorMessageImpl message) {
		this.remoteCore.endpoint().dispatch(super.identifier, message);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#
	 * doHandle(org.eclipse.sensinact.gateway.api.message.ResponseMessage)
	 */
	@Override
	public void doHandle(ResponseMessage<?, ?> message) {
		this.remoteCore.endpoint().dispatch(super.identifier, message);
	}
}