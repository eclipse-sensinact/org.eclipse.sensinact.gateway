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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.message.MidCallbackException;

/**
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class AbstractMessageAgentCallback extends AbstractMessageCallback implements AgentMessageCallback {
	protected static final String[] UNLISTENED = new String[] { "/sensiNact/system", "/AppManager/admin" };

	/**
	 * The string formated location of service providers that have already been
	 * processed by this {@link AgentMessageCallback}
	 */
	private Map<String, String> locations;

	/**
	 * true if the {@link SnaAgent} attached to this
	 * {@link AgentMessageCallback} is propagated through the
	 * connected remote sensiNact instance(s)
	 */
	private final boolean propagate;
		
	/**
	 * Constructor
	 * 
	 */
	protected AbstractMessageAgentCallback() {
		this(true,true);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier the String identifier of the {@link AgentMessageCallback}
	 * to be instantiated 
	 */
	protected AbstractMessageAgentCallback(String identifier) {
		this(true,true,identifier);
	}
	
	/**
	 * Constructor
	 * 
	 * @param propagate true if the {@link SnaAgent} attached to the
	 * 		{@link AgentMessageCallback} to be instantiated must be propagated 
	 * 		through the connected remote sensiNact instance(s); false 
	 * 		otherwise
	 */
	protected AbstractMessageAgentCallback(boolean propagate) {
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
	 * 		{@link AgentMessageCallback} to be instantiated must be propagated 
	 * 		through the connected remote sensiNact instance(s). False 
	 * 		otherwise
	 */
	protected AbstractMessageAgentCallback(boolean stack, boolean propagate) {
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
	 * {@link AgentMessageCallback} to be instantiated must be propagated 
	 * through the connected remote sensiNact instance(s). False 
	 * otherwise
	 * @param identifier the String identifier of the {@link AgentMessageCallback}
	 * to be instantiated 
	 */
	protected AbstractMessageAgentCallback(boolean stack, boolean propagate, String identifier) {
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
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#propagate()
	 */
	public boolean propagate() {
		return this.propagate;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.api.message.AbstractMessageCallback#doCallback(org.eclipse.sensinact.gateway.api.message.SnaMessage)
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
			switch (((MessageSubType) message.getType()).getSnaMessageType()) {
			case ERROR:
				this.doHandle((ErrorMessageImpl) message);
				break;
			case LIFECYCLE:
				this.doHandle((LifecycleMessageImpl) message);
				break;
			case RESPONSE:
				this.doHandle((ResponseMessage<?, ?>) message);
				break;
			case UPDATE:
				this.doHandle((UpdateMessageImpl) message);
				break;
			case REMOTE:
				this.doHandle((RemoteMessageImpl) message);
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
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#doHandle(org.eclipse.sensinact.gateway.api.message.LifecycleMessageImpl)
	 */
	@Override
	public void doHandle(LifecycleMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#doHandle(org.eclipse.sensinact.gateway.api.message.UpdateMessageImpl)
	 */
	@Override
	public void doHandle(UpdateMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#doHandle(org.eclipse.sensinact.gateway.api.message.RemoteMessageImpl)
	 */
	@Override
	public void doHandle(RemoteMessageImpl message) throws MidCallbackException {	
		//to be overridden	
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#doHandle(org.eclipse.sensinact.gateway.api.message.ErrorMessageImpl)
	 */
	@Override
	public void doHandle(ErrorMessageImpl message) throws MidCallbackException {
		//to be overridden
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.AgentMessageCallback#doHandle(org.eclipse.sensinact.gateway.api.message.ResponseMessage)
	 */
	@Override
	public void doHandle(ResponseMessage<?, ?> message) throws MidCallbackException {
		//to be overridden
	}
}