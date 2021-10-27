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

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractAgent implements SnaAgent {
	
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//
	
	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//
	private static final Logger LOG=LoggerFactory.getLogger(AbstractAgent.class);


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
	 * the {@link MidAgentCallback} type
	 */
	protected final MidAgentCallback callback;

	/**
	 * the {@link SnaFilter} used to validate the received {@link SnaMessage} before
	 * transmitting them to the associated {@link MidAgentCallback}
	 */
	protected SnaFilter filter;

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
	protected AbstractAgent(Mediator mediator, MidAgentCallback callback, SnaFilter filter, String publicKey) {
		this.mediator = mediator;
		this.callback = callback;
		this.publicKey = publicKey;
		this.filter = filter;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.MessageRegisterer#register(org.eclipse.sensinact.gateway.core.message.SnaMessage)
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
	 * @see org.eclipse.sensinact.gateway.core.message.SnaAgent#getPublicKey()
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
				LOG.error("The agent cannot be registered while its callback is inactive");
				return;
			}
			Dictionary properties = new Hashtable();
			properties.put("org.eclipse.sensinact.gateway.agent.id", this.callback.getName());
			try {
				this.registration = this.mediator.getContext().registerService(
					getAgentInterfaces(),  this, properties);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("The agent is not registered ", e);
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
					LOG.error(e.getMessage(),e);
				}
			}
			if (this.registration != null) {
				try {
					this.registration.unregister();
					this.registration = null;
				} catch (IllegalStateException e) {
					LOG.error(e.getMessage(),e);
				}
			}
			doStop();			
		}
	}
}
