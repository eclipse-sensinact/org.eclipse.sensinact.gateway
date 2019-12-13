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

import org.eclipse.sensinact.gateway.api.message.AbstractAgent;
import org.eclipse.sensinact.gateway.api.message.RemoteAgent;
import org.eclipse.sensinact.gateway.api.message.SnaAgent;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public class RemoteAgentImpl extends AbstractAgent implements RemoteAgent {
	
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//
	
	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param callback
	 * @param filter
	 * @param publicKey
	 */
	public RemoteAgentImpl(Mediator mediator, RemoteAgentCallback callback, MessageFilter filter, String publicKey) {		
		super(mediator, callback, filter, publicKey);
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.AbstractAgent#doStart()
	 */
	@Override
	public void doStart() {
		super.mediator.debug("starting RemoteAgent [%s]", super.callback.getName());
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.AbstractAgent#doStop()
	 */
	@Override
	public void doStop() {
		super.mediator.debug("stopping RemoteAgent [%s]", super.callback.getName());
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.AbstractAgent#getAgentInterfaces()
	 */
	@Override
	public String[] getAgentInterfaces() {
		return new String[] {SnaAgent.class.getName(), RemoteAgent.class.getName()};
	}
}
