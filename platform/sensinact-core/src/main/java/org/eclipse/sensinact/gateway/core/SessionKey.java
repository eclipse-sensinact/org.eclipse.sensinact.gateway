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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Sessions.SessionObserver;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaAgentImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.UserKey;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SessionKey {
	private Mediator mediator;

	private String token;
	private UserKey userKey;
	private int localID;

	private AccessTree<? extends AccessNode> tree;
	private List<String> agents;

	private SessionObserver observer;

	/**
	 * @param mediator
	 * @param localID
	 * @param token
	 * @param tree
	 */
	public SessionKey(Mediator mediator, int localID, String token, AccessTree<? extends AccessNode> tree,
			SessionObserver observer) {
		this.localID = localID;
		this.token = token;
		this.tree = tree;

		this.agents = new ArrayList<String>();
		this.mediator = mediator;
		this.observer = observer;
	}

	/**
	 * @return
	 */
	public AccessTree<? extends AccessNode> getAccessTree() {
		return this.tree;
	}

	/**
	 * @return
	 */
	public int localID() {
		return this.localID;
	}

	/**
	 * @param userKey
	 */
	public void setUserKey(UserKey userKey) {
		this.userKey = userKey;
	}

	/**
	 * @return
	 */
	public String getPublicKey() {
		return this.userKey.getPublicKey();
	}

	/**
	 * @return
	 */
	public String getToken() {
		return this.token;
	}

	/**
	 * 
	 * @param callback
	 * @param filter
	 * @return
	 */
	public String registerAgent(MidAgentCallback callback, SnaFilter filter) {
		final SnaAgentImpl agent = SnaAgentImpl.createAgent(mediator, callback, filter, getPublicKey());

		final String identifier = new StringBuilder().append("agent_").append(agent.hashCode()).toString();

		Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("org.eclipse.sensinact.gateway.agent.id", identifier);
		props.put("org.eclipse.sensinact.gateway.agent.local", (localID() == 0));

		agent.start(props);
		this.agents.add(identifier);
		return identifier;
	}

	/**
	 * 
	 * @param agentId
	 * @return
	 */
	public boolean unregisterAgent(String agentId) {
		if (!this.agents.remove(agentId)) {
			return false;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("(&(org.eclipse.sensinact.gateway.agent.local=");
		builder.append(localID() == 0);
		builder.append(")(");
		builder.append("org.eclipse.sensinact.gateway.agent.id=");
		builder.append(agentId);
		builder.append("))");

		return this.mediator.callService(SnaAgent.class, builder.toString(), new Executable<SnaAgent, Boolean>() {
			@Override
			public Boolean execute(SnaAgent agent) throws Exception {
				try {
					agent.stop();
					return true;
				} catch (Exception e) {
					mediator.error(e);
				}
				return false;
			}
		});
	}

	public boolean waitUntilClosed() {
		return waitUntilClosed(60 * 1000);
	}

	public boolean waitUntilClosed(long timeout) {
		if (this.agents.size() == 0) {
			return true;
		}
		final AtomicInteger c = new AtomicInteger();
		String filter = getAgentsFilter();
		long t = timeout;
		while (t > 0) {
			c.set(0);
			this.mediator.callServices(SnaAgent.class, filter, new Executable<SnaAgent, Void>() {
				@Override
				public Void execute(SnaAgent agent) throws Exception {
					if (agent != null) {
						c.incrementAndGet();
					}
					return null;
				}
			});
			if (c.get() == 0) {
				break;
			}
			try {
				Thread.sleep(2000);
				t -= 2000;

			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
		return c.get() == 0;
	}

	private String getAgentsFilter() {
		if (this.agents.size() == 0) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("(&(org.eclipse.sensinact.gateway.agent.local=");
		builder.append(localID() == 0);
		builder.append(")");
		if (this.agents.size() > 1) {
			builder.append("(|");
		}
		Iterator<String> it = this.agents.iterator();
		while (it.hasNext()) {
			builder.append("(");
			builder.append("org.eclipse.sensinact.gateway.agent.id=");
			builder.append(it.next());
			builder.append(")");
		}
		if (this.agents.size() > 1) {
			builder.append(")");
		}
		builder.append(")");
		return builder.toString();
	}

	void unregisterAgents() {
		if (this.agents.size() == 0) {
			return;
		}
		String filter = getAgentsFilter();
		this.mediator.callServices(SnaAgent.class, filter, new Executable<SnaAgent, Void>() {
			@Override
			public Void execute(SnaAgent agent) throws Exception {
				agent.stop();
				return null;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public void finalize() throws Throwable {
		if (this.observer != null) {
			this.observer.disappearing(this.getPublicKey());
		}
		this.unregisterAgents();
	}

}