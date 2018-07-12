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
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.util.stack.StackEngineHandler;
import org.json.JSONArray;

import java.lang.ref.WeakReference;

/**
 * An {@link SnaAgent} whose existence depend on the one of the {@link Session}
 * which created it
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SnaSessionAgentImpl extends SnaAgentImpl {

    //********************************************************************//
    //						STATIC DECLARATIONS  						  //
    //********************************************************************//

    /**
     * Creates an {@link SnaAgent} whose existence will be linked to the
     * one of the {@link Session}  passed as parameter with the callback
     * and filter which are passed as parameter
     *
     * @param mediator the {@link Mediator} allowing the {@link SnaAgent}
     *                 to be created to interact with the OSGi host environment
     * @param session  the {@link Session} to the existence of whom the
     *                 {@link SnaAgent} to be created will be linked
     * @param callback the {@link AbstractMidAgentCallback} that will
     *                 be called by the {@link SnaAgent} to be created
     * @param filter   the {@link SnaFilter} that will be used by the
     *                 {@link SnaAgent} to be created to discriminate the handled {@link
     *                 SnaMessage}s
     * @return the newly created {@link SnaAgent}
     */
    public static SnaSessionAgentImpl createAgent(Mediator mediator, Session session, MidAgentCallback callback, SnaFilter filter, String agentKey) {
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
        SnaSessionAgentImpl agent = new SnaSessionAgentImpl(mediator, session, callback, filter, agentKey);

        return agent;
    }


    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    private final WeakReference<Session> sessionRef;

    /**
     * Constructor
     *
     * @param mediator
     * @param callback
     * @param filter
     * @param publicKey
     */
    protected SnaSessionAgentImpl(Mediator mediator, Session session, MidAgentCallback callback, SnaFilter filter, String publicKey) {
        super(mediator, callback, filter, publicKey);
        this.sessionRef = new WeakReference<Session>(session);
    }

    /**
     * @inheritDoc
     * @see StackEngineHandler#doHandle(java.lang.Object)
     */
    @Override
    public void register(SnaMessage<?> message) {
        if (this.sessionRef.get() == null) {
            super.stop();
            return;
        }
        super.register(message);
    }
}
