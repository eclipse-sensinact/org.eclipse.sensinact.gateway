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
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.sensinact.gateway.api.message.MessageCallback;
import org.eclipse.sensinact.gateway.api.message.MessagePropagator;
import org.eclipse.sensinact.gateway.api.message.SnaAgent;
import org.eclipse.sensinact.gateway.api.message.SnaMessage;
import org.eclipse.sensinact.gateway.api.message.MessageSubType;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.core.message.MessageFilter;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;

/**
 * {@link SnaMessage} handler managing a set of callbacks mapped to
 * {@link MessageFilter}s defining whether to transmit or not triggered messages
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public class ModelInstanceMessagePropagator extends AbstractStackEngineHandler<SnaMessage<?>> implements MessagePropagator {
	/**
	 * The set of {@link MessageCallback} mapped to {@link MessageFilter}s defining whether
	 * to call them or not
	 */
	protected final Map<MessageFilter, List<MessageCallback>> callbacks;

	/**
	 * The set of {@link MessageCallback} mapped to {@link MessageFilter}s defining whether
	 * to call them or not
	 */
	protected final Map<String, List<MethodAccessibility>> agentsAccessibility;

	/**
	 * The sensiNact resource model configuration providing access rules applying on
	 * potentially registered {@link SnaAgent}
	 */
	private SensiNactResourceModelConfiguration configuration;

	/**
	 * the {@link Mediator} allowing to interact with the OSGi host environment
	 */
	private Mediator mediator;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param configuration
	 */
	public ModelInstanceMessagePropagator(Mediator mediator, SensiNactResourceModelConfiguration configuration) {
		super();
		this.mediator = mediator;
		this.configuration = configuration;
		this.callbacks = new HashMap<MessageFilter, List<MessageCallback>>();
		this.agentsAccessibility = new HashMap<String, List<MethodAccessibility>>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessagePropagator#addCallback(org.eclipse.sensinact.gateway.core.message.MessageFilter, org.eclipse.sensinact.gateway.core.message.MessageCallback)
	 */
	@Override
	public void addCallback(MessageFilter filter, MessageCallback callback) {
		if (filter == null || callback == null ||!callback.isActive()) {
			return;
		}
		synchronized (this.callbacks) {
			List<MessageCallback> list = this.callbacks.get(filter);
			if (list == null) {
				list = new LinkedList<MessageCallback>();
				this.callbacks.put(filter, list);
			}
			list.add(callback);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessagePropagator#deleteCallback(java.lang.String)
	 */
	@Override
	public void deleteCallback(String callback) {
		synchronized (this.callbacks) {
			Iterator<Entry<MessageFilter, List<MessageCallback>>> iterator = this.callbacks.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<MessageFilter, List<MessageCallback>> entry = iterator.next();

				List<MessageCallback> list = entry.getValue();
				if (list.remove(new Name<MessageCallback>(callback))) {
					if (list.isEmpty()) {
						iterator.remove();
					}
					break;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessagePropagator#count(java.lang.String)
	 */
	@Override
	public int count(String uri) {
		int count = 0;
		String formatedUri = UriUtils.formatUri(uri);
		synchronized (this.callbacks) {
			Iterator<MessageFilter> iterator = this.callbacks.keySet().iterator();
			while (iterator.hasNext()) {
				MessageFilter snaFilter = iterator.next();
				if (snaFilter.getSender().equals(formatedUri)) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Removes the {@link MessageFilter} passed as parameter and all mapped
	 * {@link MessageCallback}s
	 * 
	 * @param filter the {@link MessageFilter} to remove
	 */
	public void removeFilter(MessageFilter filter) {
		synchronized (this.callbacks) {
			this.callbacks.remove(filter);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessagePropagator#propagate(org.eclipse.sensinact.gateway.core.message.SnaMessage)
	 */
	@Override
	public void propagate(SnaMessage<?> message) {
		super.eventEngine.push(message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.util.stack.StackEngineHandler#doHandle(java.lang.Object)
	 */
	@Override
	public void doHandle(SnaMessage<?> message) {
		String messageMethod = null;

		switch (((MessageSubType) message.getType()).getSnaMessageType()) {
		case RESPONSE:
			switch ((AccessMethodResponse.Response) message.getType()) {
			case ACT_RESPONSE:
				messageMethod = AccessMethod.ACT;
				break;
			case DESCRIBE_RESPONSE:
				messageMethod = AccessMethod.DESCRIBE;
				break;
			case GET_RESPONSE:
				messageMethod = AccessMethod.GET;
				break;
			case SET_RESPONSE:
				messageMethod = AccessMethod.SET;
				break;
			case SUBSCRIBE_RESPONSE:
				messageMethod = AccessMethod.SUBSCRIBE;
				break;
			case UNSUBSCRIBE_RESPONSE:
				messageMethod = AccessMethod.UNSUBSCRIBE;
				break;
			default:
				break;
			}
			break;
		case ERROR:
		case LIFECYCLE:
			messageMethod = AccessMethod.DESCRIBE;
			break;
		case UPDATE:
			messageMethod = AccessMethod.GET;
			break;
		default:
			break;
		}
		if (messageMethod != null) {
			doHandleAgents(message, messageMethod);
		}
		doHandleSubscribers(message);
	}

	/**
	 * Transmits the {@link SnaMessage} passed as parameter to the appropriate
	 * subscribers according to their associated filter, target, and message type
	 * 
	 * @param message
	 *            the {@link SnaMessage} to transmit
	 */
	private void doHandleSubscribers(final SnaMessage<?> message) {
		synchronized (this.callbacks) {
			Iterator<Entry<MessageFilter, List<MessageCallback>>> iterator = this.callbacks.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<MessageFilter, List<MessageCallback>> entry = iterator.next();

				MessageFilter filter = entry.getKey();
				if (!filter.matches(message)) {
					continue;
				}
				Iterator<MessageCallback> callbackIterator = entry.getValue().iterator();

				while (callbackIterator.hasNext()) {
					MessageCallback callback = callbackIterator.next();

					if ((callback.getTimeout() != MessageCallback.ENDLESS
							&& System.currentTimeMillis() > callback.getTimeout())
							    || !callback.isActive()) {
						callbackIterator.remove();
						continue;
					}
					callback.getMessageRegisterer().register(message);
				}
			}
		}
	}

	/**
	 * Transmits the {@link SnaMessage} passed as parameter to the appropriate
	 * agents according to their access rights
	 * 
	 * @param message the {@link SnaMessage} to transmit
	 */
	private void doHandleAgents(final SnaMessage<?> message, final String method) {
		final String path = message.getPath();
		
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				mediator.callServices(SnaAgent.class, new Executable<SnaAgent, Void>() {
					@Override
					public Void execute(SnaAgent agent) throws Exception {
						String agentKey = agent.getPublicKey();
						List<MethodAccessibility> methodAccessibilities = ModelInstanceMessagePropagator.this.agentsAccessibility.get(agentKey);
						int index = -1;
						if (methodAccessibilities == null) {
							AccessLevelOption option = ModelInstanceMessagePropagator.this.configuration.getAuthenticatedAccessLevelOption(path, agentKey);
							if (option == null) {
								option = AccessLevelOption.ANONYMOUS;
							}
							methodAccessibilities = ModelInstanceMessagePropagator.this.configuration.getAccessibleMethods(path, option);
							ModelInstanceMessagePropagator.this.agentsAccessibility.put(agentKey, methodAccessibilities);
						}
						if ((index = methodAccessibilities.indexOf(new Name<MethodAccessibility>(method))) > -1
								&& methodAccessibilities.get(index).isAccessible()) {
							agent.register(message);
						}
						return null;
					}
				});
				return null;
				}
			});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.core.message.MessagePropagator#close(boolean)
	 */
	@Override
	public void close(boolean wait) {
		if (wait) {
			super.close();
		} else {
			super.stop();
		}
	}
}