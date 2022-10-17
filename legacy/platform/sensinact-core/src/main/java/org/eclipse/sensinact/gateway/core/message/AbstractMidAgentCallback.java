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

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractMidAgentCallback extends AbstractMidCallback implements MidAgentCallback {
	protected static final String[] UNLISTENED = new String[] { "/sensiNact/system", "/AppManager/admin" };

	/**
	 * The string formated location of service providers that have already been
	 * processed by this {@link MidAgentCallback}
	 */
	private Map<String, String> locations;

	/**
	 * true if the {@link SnaAgent} attached to this
	 * {@link MidAgentCallback} is propagated through the
	 * connected remote sensiNact instance(s)
	 */
	private final boolean propagate;
		
	/**
	 * Constructor
	 * 
	 */
	protected AbstractMidAgentCallback() {
		this(true,true);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier the String identifier of the {@link MidAgentCallback}
	 * to be instantiated 
	 */
	protected AbstractMidAgentCallback(String identifier) {
		this(true,true,identifier);
	}
	
	/**
	 * Constructor
	 * 
	 * @param propagate true if the {@link SnaAgent} attached to the
	 * 		{@link MidAgentCallback} to be instantiated must be propagated 
	 * 		through the connected remote sensiNact instance(s); false 
	 * 		otherwise
	 */
	protected AbstractMidAgentCallback(boolean propagate) {
		this(true, propagate);
	}
	
	/**
	 * Constructor
	 * 
	 * @param stack true if the registered {@link SnaMessage}s are first
	 * 		stored into an intermediate stack for desynchronization purpose 
	 * 		before to be processed. False if the {@link SnaMessage}s are
	 * 		processed immediately when received
	 * @param propagate true if the {@link SnaAgent} attached to the
	 * 		{@link MidAgentCallback} to be instantiated must be propagated 
	 * 		through the connected remote sensiNact instance(s). False 
	 * 		otherwise
	 */
	protected AbstractMidAgentCallback(boolean stack, boolean propagate) {
		super(stack);
		this.locations = new HashMap<String, String>();
		this.propagate = propagate;
	}

	/**
	 * Constructor
	 * 
	 * @param stack true if the registered {@link SnaMessage}s are first
	 * stored into an intermediate stack for desynchronization purpose 
	 * before to be processed. False if the {@link SnaMessage}s are
	 * processed immediately when received
	 * @param propagate true if the {@link SnaAgent} attached to the
	 * {@link MidAgentCallback} to be instantiated must be propagated 
	 * through the connected remote sensiNact instance(s). False 
	 * otherwise
	 * @param identifier the String identifier of the {@link MidAgentCallback}
	 * to be instantiated 
	 */
	protected AbstractMidAgentCallback(boolean stack, boolean propagate, String identifier) {
		super(stack, identifier);
		this.locations = new HashMap<String, String>();
		this.propagate = propagate;
	}
	
	/**
	 * Returns the String location for the service provider whose path is passed as
	 * parameter
	 * 
	 * @param path
	 *            the path of the service provider for which to retrieve the string
	 *            location
	 * @return the String location for the specified path
	 */
	protected String getLocation(String serviceProvider) {
		synchronized (this.locations) {
			return this.locations.get(serviceProvider);
		}
	}

	/**
	 * Sets the String location for the service provider whose path is passed as
	 * parameter
	 * 
	 * @param path
	 *            the path of the service provider for which to set the string
	 *            location
	 * @param location
	 *            the string location to set
	 */
	protected void setLocation(String serviceProvider, String location) {
		synchronized (this.locations) {
			this.locations.put(serviceProvider, location);
		}
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#propagate()
	 */
	public boolean propagate() {
		return this.propagate;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.message.AbstractMidCallback#doCallback(org.eclipse.sensinact.gateway.core.message.SnaMessage)
	 */
	@Override
	public void doCallback(SnaMessage<?> message) throws MidCallbackException {
		if (message == null) {
			return;
		}
		String path = message.getPath();
		if (path == null) {
			return;
		}
		int index = 0;
		int length = UNLISTENED == null ? 0 : UNLISTENED.length;
		for (; index < length; index++) {
			String unlistened = UNLISTENED[index];
			if (unlistened == null) {
				continue;
			}
			if (path.startsWith(unlistened)) {
				return;
			}
		}
		try {
			switch (((SnaMessageSubType) message.getType()).getSnaMessageType()) {
			case ERROR:
				this.doHandle((SnaErrorMessageImpl) message);
				break;
			case LIFECYCLE:
				this.doHandle((SnaLifecycleMessageImpl) message);
				break;
			case RESPONSE:
				this.doHandle((SnaResponseMessage<?, ?>) message);
				break;
			case UPDATE:
				this.doHandle((SnaUpdateMessageImpl) message);
				break;
			case REMOTE:
				this.doHandle((SnaRemoteMessageImpl) message);
				break;
			default:
				;
			}
		} catch (MidCallbackException e) {
			throw e;
		} catch (Exception e) {
			throw new MidCallbackException(e);
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl)
	 */
	@Override
	public void doHandle(SnaLifecycleMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl)
	 */
	@Override
	public void doHandle(SnaUpdateMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaRemoteMessageImpl)
	 */
	@Override
	public void doHandle(SnaRemoteMessageImpl message) throws MidCallbackException {	
		//to be overridden	
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl)
	 */
	@Override
	public void doHandle(SnaErrorMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.MidAgentCallback#doHandle(org.eclipse.sensinact.gateway.core.message.SnaResponseMessage)
	 */
	@Override
	public void doHandle(SnaResponseMessage<?, ?> message) throws MidCallbackException {
		//to be overridden
	}
}