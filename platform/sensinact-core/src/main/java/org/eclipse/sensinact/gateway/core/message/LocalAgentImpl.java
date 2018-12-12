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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.remote.RemoteCore;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class LocalAgentImpl extends AbstractAgent implements LocalAgent {

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	/**
	 * Creates an new {@link LocalAgent} with the callback and filter which 
	 * are passed as parameters
	 * 
	 * @param mediator the {@link Mediator} allowing the {@link LocalAgent} 
	 * to be created to interact with the OSGi host environment
	 * @param callback the {@link MidAgentCallback} being the local
	 * recipient of the messages registered by the {@link LocalAgent} to be 
	 * instantiated 
	 * @param filter the {@link SnaFilter} allowing to discriminate the messages
	 * registered by the {@link LocalAgent} to be instantiated
	 * @param publicKey the String public key of the {@link LocalAgent} to be 
	 * instantiated
	 * 
	 * @return the newly created {@link LocalAgent}
	 */
	public static LocalAgent createAgent(Mediator mediator, MidAgentCallback callback, 
		SnaFilter filter, String agentKey) {
		String suffix = (String) mediator.getProperty(SNAFILTER_AGENT_SUFFIX_PROPERTY);

		if (filter == null && suffix != null) {
			boolean isPattern = false;
			String sender = LocalAgentImpl.getSender(mediator, suffix);

			if (sender == null) {
				sender = ".*";
				isPattern = true;
			} else {
				isPattern = LocalAgentImpl.isPattern(mediator, suffix);
			}
			boolean isComplement = LocalAgentImpl.isComplement(mediator, suffix);
			JSONArray conditions = LocalAgentImpl.getConditions(mediator, suffix);
			SnaMessage.Type[] types = LocalAgentImpl.getTypes(mediator, suffix);

			filter = new SnaFilter(mediator, sender, isPattern, isComplement, conditions);
			int index = 0;
			int length = types.length;

			for (; index < length; index++) {
				filter.addHandledType(types[index]);
			}
		}
		return new LocalAgentImpl(mediator, callback, filter, agentKey);
	} 
	
	protected static JSONArray getConditions(Mediator mediator, String suffix) {
		JSONArray conditions = null;

		String conditionsStr = (String) mediator
				.getProperty(buildProperty(SNAFILTER_AGENT_CONDITIONS_PROPERTY, suffix));

		if (conditionsStr == null) {
			conditions = new JSONArray();

		} else {
			try {
				conditions = new JSONArray(conditionsStr);

			} catch (JSONException e) {
				conditions = new JSONArray();
			}
		}
		return conditions;
	}

	protected static SnaMessage.Type[] getTypes(Mediator mediator, String suffix) {
		SnaMessage.Type[] messageTypes = null;

		String typesStr = (String) mediator.getProperty(buildProperty(SNAFILTER_AGENT_TYPES_PROPERTY, suffix));

		if (typesStr == null) {
			messageTypes = SnaMessage.Type.values();

		} else {
			String[] typesArray = typesStr.split(COMMA);
			messageTypes = new SnaMessage.Type[typesArray.length];
			int index = 0;
			int length = typesArray.length;
			try {
				for (; index < length; index++) {
					messageTypes[index] = SnaMessage.Type.valueOf(typesArray[index]);
				}
			} catch (IllegalArgumentException e) {
				messageTypes = SnaMessage.Type.values();
			}
		}
		return messageTypes;
	}

	protected static String getSender(Mediator mediator, String suffix) {
		return (String) mediator.getProperty(buildProperty(SNAFILTER_AGENT_SENDER_PROPERTY, suffix));
	}

	protected static boolean isPattern(Mediator mediator, String suffix) {
		boolean isPattern = false;
		String patternStr = (String) mediator.getProperty(buildProperty(SNAFILTER_AGENT_PATTERN_PROPERTY, suffix));
		if (patternStr != null) {
			isPattern = Boolean.parseBoolean(patternStr);
		}
		return isPattern;
	}

	protected static boolean isComplement(Mediator mediator, String suffix) {
		boolean isComplement = false;
		String complementStr = (String) mediator
				.getProperty(buildProperty(SNAFILTER_AGENT_COMPLEMENT_PROPERTY, suffix));
		if (complementStr != null) {
			isComplement = Boolean.parseBoolean(complementStr);
		}
		return isComplement;
	}

	private static String buildProperty(String property, String suffix) {
		return new StringBuilder().append(property).append(DOT).append(suffix).toString();
	}

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
	protected LocalAgentImpl(Mediator mediator, MidAgentCallback callback, SnaFilter filter, String publicKey) {
		super(mediator,callback,filter,publicKey);
	}
	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.AbstractAgent#doStart()
	 */
	@Override
	public void doStart() {
		registerRemote();
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.AbstractAgent#doStop()
	 */
	@Override
	public void doStop() {
		if(this.callback.propagate()) {
			this.mediator.callServices(RemoteCore.class, new Executable<RemoteCore, Void>() {
				@Override
				public Void execute(RemoteCore remoteCore) throws Exception {
					remoteCore.endpoint().unregisterAgent(LocalAgentImpl.this.callback.getName());
					return null;
				}
			});
		}
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.LocalAgent#registerRemote(org.eclipse.sensinact.gateway.core.remote.RemoteCore)
	 */
	public void registerRemote(RemoteCore remoteCore) {
		if(!this.callback.propagate() || remoteCore == null){
			return;
		}
		synchronized(this) {
			final String identifier = this.callback.getName();
			remoteCore.endpoint().registerAgent(identifier, filter, publicKey);
		}
	}

	private void registerRemote() {
		if(!this.callback.propagate()){
			return;
		}
		synchronized(this) {
			LocalAgentImpl.this.mediator.callServices(RemoteCore.class, new Executable<RemoteCore, Void>() {
				@Override
				public Void execute(RemoteCore remoteCore) throws Exception {
					LocalAgentImpl.this.registerRemote(remoteCore);
					return null;
				}
			});
		}
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.message.AbstractAgent#getAgentInterfaces()
	 */
	@Override
	public String[] getAgentInterfaces() {
		return new String[] {SnaAgent.class.getName(), LocalAgent.class.getName()};
	}
}
