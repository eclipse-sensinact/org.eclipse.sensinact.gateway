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

import org.eclipse.sensinact.gateway.core.remote.RemoteCore;

public class RemoteAgentCallback extends AbstractMidAgentCallback {
	
	private final RemoteCore remoteCore;

	public RemoteAgentCallback(String identifier, RemoteCore remoteCore) {
		super(identifier);
		this.remoteCore = remoteCore;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
	 * doHandle(org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl)
	 */
	@Override
	public void doHandle(SnaLifecycleMessageImpl message) {
		this.remoteCore.endpoint().dispatch(super.identifier, message);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
	 * doHandle(org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl)
	 */
	@Override
	public void doHandle(SnaUpdateMessageImpl message) {
		this.remoteCore.endpoint().dispatch(super.identifier, message);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
	 * doHandle(org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl)
	 */
	@Override
	public void doHandle(SnaErrorMessageImpl message) {
		this.remoteCore.endpoint().dispatch(super.identifier, message);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#
	 * doHandle(org.eclipse.sensinact.gateway.core.message.SnaResponseMessage)
	 */
	@Override
	public void doHandle(SnaResponseMessage<?, ?> message) {
		this.remoteCore.endpoint().dispatch(super.identifier, message);
	}
}