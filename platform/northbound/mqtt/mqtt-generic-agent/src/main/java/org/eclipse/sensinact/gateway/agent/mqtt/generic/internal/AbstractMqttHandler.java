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
package org.eclipse.sensinact.gateway.agent.mqtt.generic.internal;

import java.io.IOException;

import org.eclipse.sensinact.gateway.api.message.AbstractMessageAgentCallback;

public abstract class AbstractMqttHandler extends AbstractMessageAgentCallback {
    protected GenericMqttAgent agent;

    public AbstractMqttHandler() throws IOException {
        super();
    }

    public void setAgent(GenericMqttAgent agent) {
        this.agent = agent;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.core.message.AbstractMidCallback#stop()
     */
    @Override
    public void stop() {
    	super.stop();
    	this.agent.close();
    }
}