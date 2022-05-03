/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RemoteAgentImpl extends AbstractAgent implements RemoteAgent {
	
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//
	
	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//
	private static final Logger LOG=LoggerFactory.getLogger(RemoteAgentImpl.class);

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
	public RemoteAgentImpl(Mediator mediator, RemoteAgentCallback callback, SnaFilter filter, String publicKey) {		
		super(mediator, callback, filter, publicKey);
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.AbstractAgent#doStart()
	 */
	@Override
	public void doStart() {
		LOG.debug("starting RemoteAgent [%s]", super.callback.getName());
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.AbstractAgent#doStop()
	 */
	@Override
	public void doStop() {
		LOG.debug("stopping RemoteAgent [%s]", super.callback.getName());
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.AbstractAgent#getAgentInterfaces()
	 */
	@Override
	public String[] getAgentInterfaces() {
		return new String[] {SnaAgent.class.getName(), RemoteAgent.class.getName()};
	}
}
