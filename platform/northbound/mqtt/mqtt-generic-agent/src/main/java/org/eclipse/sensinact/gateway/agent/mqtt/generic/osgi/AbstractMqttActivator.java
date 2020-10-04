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
package org.eclipse.sensinact.gateway.agent.mqtt.generic.osgi;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.AbstractMqttHandler;
import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.GenericMqttAgent;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMqttActivator extends AbstractActivator<Mediator> {
    private final Logger LOG= LoggerFactory.getLogger(AbstractMqttActivator.class);

    private AbstractMqttHandler handler;
    private GenericMqttAgent agent;

    private String registration;

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    protected void doStart(AbstractMqttHandler handler) throws Exception {
        LOG.debug("Starting MQTT Agent");
        final String broker = mediator.getProperty("broker").toString();
        final String prefix = mediator.getProperty("prefix").toString();
        final Object username= mediator.getProperty("username");
        final Object password= mediator.getProperty("password");
        final Integer qos = new Integer(mediator.getProperty("qos").toString());
        LOG.debug("Starting MQTT Agent point to server {} with prefix {} and qos {}",broker,prefix,qos);
        this.handler = handler;

        if(username!=null&&password!=null&&!username.toString().trim().equals("")&&!password.toString().trim().equals("")){
            this.agent = new GenericMqttAgent(broker, qos,prefix,username.toString(),password.toString());
        }else {
            this.agent = new GenericMqttAgent(broker, qos,prefix);
        }

        this.handler.setAgent(agent);
        this.registration = mediator.callService(Core.class, new Executable<Core, String>() {
            @Override
            public String execute(Core service) throws Exception {
                return service.registerAgent(mediator, AbstractMqttActivator.this.handler, null);
            }
        });
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doStop()
     */
    public void doStop() throws Exception {
        if (super.mediator.isDebugLoggable()) {
            super.mediator.debug("Stopping MQTT agent");
        }
        this.registration = null;
        this.handler.stop();
        this.handler = null;
        this.agent.close();
    }

    /**
     * @inheritDoc
     * @see AbstractActivator#doInstantiate(BundleContext)
     */
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
