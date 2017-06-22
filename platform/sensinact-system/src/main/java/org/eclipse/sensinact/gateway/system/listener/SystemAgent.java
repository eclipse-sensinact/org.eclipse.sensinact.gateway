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

package org.eclipse.sensinact.gateway.system.listener;

import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.system.internal.SystemPacket;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

public class SystemAgent extends AbstractSnaAgentCallback {

    private static final String SELF_URI = "/sensiNact/system/event";

    private final LocalProtocolStackEndpoint<SystemPacket> connector;

    private Mediator mediator;
    
    /**
     * Constructor
     *
     * @param mediator the mediator
     * @param connector the connector to the system
     */
    public SystemAgent(Mediator mediator, LocalProtocolStackEndpoint<SystemPacket> connector) {
        super();
        this.mediator = mediator;
        this.connector = connector;
    }

    /**
     * @inheritDoc
     *
     * @see AbstractSnaAgentCallback#doHandle(SnaLifecycleMessageImpl)
     */
    @Override
    public void doHandle(SnaLifecycleMessageImpl message) {
        String uri = message.getPath();

        if(uri==null || uri.startsWith(SELF_URI)) {
            return;
        }

        String jsonMessage = message.getJSON();

        try 
        {
            this.connector.process(new SystemPacket(jsonMessage));
        } catch (Exception e) 
        {
           this.mediator.error(e);
        }
    }

    /**
     * @inheritDoc
     *
     * @see AbstractSnaAgentCallback#doHandle(SnaUpdateMessageImpl)
     */
    @Override
    public void doHandle(SnaUpdateMessageImpl message) {

        String uri = message.getPath();
        String[] uriElements = UriUtils.getUriElements(uri);

        if(uriElements.length < 2 || !ServiceProvider.ADMINISTRATION_SERVICE_NAME.equals(uriElements[1])) {
            return;
        }

        String jsonMessage = message.getJSON();

        try
        {
            this.connector.process(new SystemPacket(jsonMessage));
        } catch (Exception e) 
        {
            this.mediator.error(e);
        }
    }

    /**
     * @inheritDoc
     *
     * @see AbstractSnaAgentCallback#doHandle(SnaErrorMessageImpl)
     */
    @Override
    public void doHandle(SnaErrorMessageImpl event) {}

    /**
     * @inheritDoc
     *
     * @see AbstractSnaAgentCallback#doHandle(SnaResponseMessage)
     */
    @Override
    public void doHandle(SnaResponseMessage event) {}

    /**
     * @inheritDoc
     *
     * @see AbstractSnaAgentCallback#stop()
     */
    @Override
    public void stop() {}
}
