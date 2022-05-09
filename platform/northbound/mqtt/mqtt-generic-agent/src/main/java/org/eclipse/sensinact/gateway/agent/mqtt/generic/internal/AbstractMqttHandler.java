/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.mqtt.generic.internal;

import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;

import java.io.IOException;

public abstract class AbstractMqttHandler extends AbstractMidAgentCallback {
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