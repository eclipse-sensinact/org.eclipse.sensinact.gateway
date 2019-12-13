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

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.MessageFilter;
import org.osgi.framework.ServiceRegistration;

/**
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class AbstractAgent implements SnaAgent {
	
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//
	
	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	public abstract String[] getAgentInterfaces();
	
	public abstract void doStart();
	
	public abstract void doStop();
	
	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * the {@link Mediator} allowing to interact with the OSGi host environment
	 */
	protected Mediator mediator;
	
	/**
	 * the {@link AgentMessageCallback} type
	 */
	protected final AgentMessageCallback callback;

	/**
	 * the {@link MessageFilter} used to validate the received {@link SnaMessage} before
	 * transmitting them to the associated {@link AgentMessageCallback}
	 */
	protected MessageFilter filter;

	/**
	 * the String public key of this {@link SnaAgent}
	 */
	protected final String publicKey;


	/**
	 * this SnaAgent service OSGi registration
	 */
	protected ServiceRegistration<?> registration;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param callback
	 * @param filter
	 * @param publicKey
	 */
	protected AbstractAgent(Mediator mediator, AgentMessageCallback callback, MessageFilter filter, String publicKey) {
		this.mediator = mediator;
		this.callback = callback;
		this.publicKey = publicKey;
		this.filter = filter;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.MessageRegisterer#register(org.eclipse.sensinact.gateway.api.message.SnaMessage)
	 */
	@Override
	public void register(SnaMessage<?> message) {
		synchronized(this) {
			if(!this.callback.isActive()) {
				this.stop();
				return;
			}
			if (this.filter == null || filter.matches(message)) {
				this.callback.getMessageRegisterer().register(message);
			}
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.api.message.SnaAgent#getPublicKey()
	 */
	public String getPublicKey() {
		return this.publicKey;
	}

	/**
	 * Starts this LocalAgent, registers it into the registry of the OSGi host
	 * environment and, if relevant, registers it into remote cores
	 */
	public void start() {
		synchronized(this){
			if(!this.callback.isActive()) {
				this.mediator.error("The agent cannot be registered while its callback is inactive");
				return;
			}
			Dictionary properties = new Hashtable();
			properties.put("org.eclipse.sensinact.gateway.agent.id", this.callback.getName());
			try {
				this.registration = this.mediator.getContext().registerService(
					getAgentInterfaces(),  this, properties);
			} catch (IllegalStateException e) {
				this.mediator.error("The agent is not registered ", e);
			}
			doStart();
		}
	}

	/**
	 * Stops this RemoteAgent and unregisters it from the registry of the OSGi 
	 * host environment
	 */
	public void stop() {
		synchronized(this) {
			if(this.callback.isActive()) {
				try {
					this.callback.stop();		
				} catch (Exception e) {
					this.mediator.error(e);
				}
			}
			if (this.registration != null) {
				try {
					this.registration.unregister();
					this.registration = null;
				} catch (IllegalStateException e) {
					this.mediator.error(e);
				}
			}
			doStop();			
		}
	}
}
