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
import org.eclipse.sensinact.gateway.core.RemoteCore;
import org.json.JSONArray;
import org.json.JSONException;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SnaAgentImpl implements SnaAgent {
    //********************************************************************//
    //						STATIC DECLARATIONS  						  //
    //********************************************************************//

    /**
     * Creates an {@link SnaAgent} with the callback and filter which are
     * passed as parameter
     *
     * @param mediator the {@link Mediator} allowing the {@link SnaAgent}
     *                 to be created to interact with the OSGi host environment
     * @param callback the {@link AbstractMidAgentCallback} that will
     *                 be called by the{@link SnaAgent} to be created
     * @param filter   the {@link SnaFilter} that will be used by the
     *                 {@link SnaAgent} to be created to discriminate the handled {@link
     *                 SnaMessage}s
     * @return the newly created {@link SnaAgent}
     */
    public static SnaAgentImpl createAgent(Mediator mediator, MidAgentCallback callback, SnaFilter filter, String agentKey) {
        String suffix = (String) mediator.getProperty(SNAFILTER_AGENT_SUFFIX_PROPERTY);

        if (filter == null && suffix != null) {
            boolean isPattern = false;
            String sender = SnaAgentImpl.getSender(mediator, suffix);

            if (sender == null) {
                sender = ".*";
                isPattern = true;

            } else {
                isPattern = SnaAgentImpl.isPattern(mediator, suffix);
            }
            boolean isComplement = SnaAgentImpl.isComplement(mediator, suffix);
            JSONArray conditions = SnaAgentImpl.getConditions(mediator, suffix);
            SnaMessage.Type[] types = SnaAgentImpl.getTypes(mediator, suffix);

            filter = new SnaFilter(mediator, sender, isPattern, isComplement, conditions);
            int index = 0;
            int length = types.length;

            for (; index < length; index++) {
                filter.addHandledType(types[index]);
            }
        }
        SnaAgentImpl agent = new SnaAgentImpl(mediator, callback, filter, agentKey);

        return agent;
    }

    /**
     * @param mediator
     * @param suffix
     * @return
     */
    protected static JSONArray getConditions(Mediator mediator, String suffix) {
        JSONArray conditions = null;

        String conditionsStr = (String) mediator.getProperty(buildProperty(SNAFILTER_AGENT_CONDITIONS_PROPERTY, suffix));

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

    /**
     * @param mediator
     * @param suffix
     * @return
     */
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

    /**
     * @param mediator
     * @param suffix
     * @return
     */
    protected static String getSender(Mediator mediator, String suffix) {
        return (String) mediator.getProperty(buildProperty(SNAFILTER_AGENT_SENDER_PROPERTY, suffix));
    }

    /**
     * @param mediator
     * @param suffix
     * @return
     */
    protected static boolean isPattern(Mediator mediator, String suffix) {
        boolean isPattern = false;
        String patternStr = (String) mediator.getProperty(buildProperty(SNAFILTER_AGENT_PATTERN_PROPERTY, suffix));
        if (patternStr != null) {
            isPattern = Boolean.parseBoolean(patternStr);
        }
        return isPattern;
    }

    /**
     * @param mediator
     * @param suffix
     * @return
     */
    protected static boolean isComplement(Mediator mediator, String suffix) {
        boolean isComplement = false;
        String complementStr = (String) mediator.getProperty(buildProperty(SNAFILTER_AGENT_COMPLEMENT_PROPERTY, suffix));
        if (complementStr != null) {
            isComplement = Boolean.parseBoolean(complementStr);
        }
        return isComplement;
    }

    /**
     * @param property
     * @param suffix
     * @return
     */
    private static String buildProperty(String property, String suffix) {
        return new StringBuilder().append(property).append(DOT).append(suffix).toString();
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     * the {@link AbstractMidAgentCallback} type
     */
    protected final MidAgentCallback callback;

    /**
     * the {@link SnaFilter} used to validate the
     * received {@link SnaMessage} before transmitting
     * them to the dedicated {@link AbstractMidAgentCallback}
     */
    protected SnaFilter filter;

    /**
     * the String public key of this {@link SnaAgent}
     */
    protected final String publicKey;

    /**
     * the {@link Mediator} allowing to interact
     * with the OSGi host environment
     */
    protected Mediator mediator;
    /**
     * this SnaAgent service OSGi registration
     */
    protected ServiceRegistration<SnaAgent> registration;

    /**
     * Constructor
     *
     * @param mediator
     * @param callback
     * @param filter
     * @param publicKey
     */
    protected SnaAgentImpl(Mediator mediator, MidAgentCallback callback, SnaFilter filter, String publicKey) {
        this.mediator = mediator;
        this.publicKey = publicKey;
        this.callback = callback;
        this.filter = filter;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.message.MessageRegisterer#
     * register(org.eclipse.sensinact.gateway.core.message.SnaMessage)
     */
    public synchronized void register(SnaMessage<?> message) {
        if (this.filter == null || filter.matches(message)) {
            this.callback.getMessageRegisterer().register(message);
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.message.SnaAgent#getPublicKey()
     */
    public String getPublicKey() {
        return this.publicKey;
    }

    /**
     * @param properties
     */
    public void start(Dictionary<String, Object> properties) {
        boolean local = false;
        String identifier = null;

        Object localProp = properties.get("org.eclipse.sensinact.gateway.agent.local");
        Object identifierProp = properties.get("org.eclipse.sensinact.gateway.agent.id");

        try {
            identifier = (String) identifierProp;
        } catch (Exception e) {
            mediator.error("Invalid 'identifier' property : %s", e.getMessage());
        }
        if (identifier == null) {
            return;
        }
        this.callback.setIdentifier(identifier);
        try {
            local = ((Boolean) localProp).booleanValue();
        } catch (Exception e) {
            mediator.warn("Invalid 'local' property :%s", e.getMessage());
        }
        try {
            this.registration = this.mediator.getContext().registerService(SnaAgent.class, this, properties);
            if (local) {
                registerRemote();
            }
        } catch (IllegalStateException e) {
            this.mediator.error("The agent is not registered ", e);
        }
    }

    /**
     * Registers this SnaAgent into the referenced {@link
     * RemoteCore}s
     *
     * @param identifier this SnaAgent's identifier
     */
    protected void registerRemote() {
        SnaAgentImpl.this.mediator.callServices(RemoteCore.class, new Executable<RemoteCore, Void>() {
            @Override
            public Void execute(RemoteCore remoteCore) throws Exception {
                SnaAgentImpl.this.registerRemote(remoteCore);
                return null;
            }
        });
    }

    /**
     * Registers this SnaAgent into the {@link RemoteCore}
     * passed as parameter
     *
     * @param remoteCore the {@link RemoteCore} into which
     *                   register this SnaAgent
     * @param identifier this SnaAgent's identifier
     */
    public void registerRemote(RemoteCore remoteCore) {
        final String identifier = (String) this.registration.getReference().getProperty("org.eclipse.sensinact.gateway.agent.id");

        if (remoteCore == null || identifier == null || identifier.length() == 0) {
            return;
        }
        remoteCore.endpoint().registerAgent(identifier, filter, publicKey);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler#stop()
     */
    @Override
    public void stop() {
        try {
            this.callback.stop();

        } catch (Exception e) {
            this.mediator.error(e);
        }
        if (this.registration == null) {
            return;
        }
        try {
            this.registration.unregister();

        } catch (IllegalStateException e) {
            this.mediator.error(e);
        }
    }
}
