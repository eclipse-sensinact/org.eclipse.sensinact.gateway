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
package org.eclipse.sensinact.gateway.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Sessions.SessionObserver;
import org.eclipse.sensinact.gateway.core.message.LocalAgentImpl;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.RemoteAgentCallback;
import org.eclipse.sensinact.gateway.core.message.RemoteAgentImpl;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.UserKey;

/**
 * 
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
	public SessionKey(Mediator mediator, int localID, String token, 
		AccessTree<? extends AccessNode> tree, SessionObserver observer) {
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
	 * @param callback
	 * @param filter
	 * @return
	 */
	public boolean registerAgent(MidAgentCallback callback, SnaFilter filter) {
		if(this.agents.contains(callback.getName())){
			this.mediator.warn("Agent '%s' already registered",callback.getName());
			return false;
		}
		SnaAgent agent = null;
		if(RemoteAgentCallback.class.isAssignableFrom(callback.getClass())) {
			agent = new RemoteAgentImpl(mediator, (RemoteAgentCallback) callback, filter, getPublicKey());
		} else {
			agent = LocalAgentImpl.createAgent(mediator, callback, filter, getPublicKey());		}
		return registerAgent(callback.getName(), agent);
	}
	
	/**
	 * @param callback
	 * @param filter
	 * @return
	 */
	public boolean registerAgent(String agentId, SnaAgent agent) {
		if(this.agents.contains(agentId)) {
			this.mediator.warn("Agent %s already registered",agentId);
			return false;
		}
		synchronized(this) {
			this.agents.add(agentId);
			agent.start();
		}
		return true;
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
		return this.mediator.callService(SnaAgent.class, String.format("(org.eclipse.sensinact.gateway.agent.id=%s)",agentId), new Executable<SnaAgent, Boolean>() {
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

	private String getAgentsFilter() {
		if (this.agents.size() == 0) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
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
		return builder.toString();
	}

	void unregisterAgents() {
		if (this.agents.size() == 0) {
			return;
		}
		String filter = getAgentsFilter();	 
		this.mediator.callServices(SnaAgent.class, filter, new Executable<SnaAgent, Void>() {
			@Override
			public Void execute(final SnaAgent agent) throws Exception {
				agent.stop();
				return null;
			}
		});
		this.agents.clear();
	}

	@Override
	public void finalize() throws Throwable {
		this.unregisterAgents();
		if (this.observer != null) {
			this.observer.disappearing(this.getPublicKey());
		}
	}
}