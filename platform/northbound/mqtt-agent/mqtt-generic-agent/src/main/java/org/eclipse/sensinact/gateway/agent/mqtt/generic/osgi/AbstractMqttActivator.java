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
package org.eclipse.sensinact.gateway.agent.mqtt.generic.osgi;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.AbstractMqttHandler;
import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.GenericMqttAgent;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.osgi.framework.BundleContext;

import java.util.HashMap;

public abstract class AbstractMqttActivator extends AbstractActivator<Mediator> {
    private AbstractMqttHandler handler;
    private GenericMqttAgent agent;
    private String registration;

    /**
     * @inheritDoc
     * @see AbstractActivator#doStart()
     */
    protected void doStart(AbstractMqttHandler handler) throws Exception {
        if (super.mediator.isDebugLoggable()) {
            super.mediator.debug("Starting MQTT agent");
        }
        this.handler = handler;
        HashMap<String, String> config = new HashMap<String, String>();
        config.put("host", (String) super.mediator.getProperty("org.eclipse.sensinact.gateway.northbound.mqtt.host"));
        config.put("port", (String) super.mediator.getProperty("org.eclipse.sensinact.gateway.northbound.mqtt.port"));
        config.put("qos", (String) super.mediator.getProperty("org.eclipse.sensinact.gateway.northbound.mqtt.qos"));
        String broker = "tcp://" + config.get("host") + ":" + config.get("port");
        int qos = Integer.valueOf(config.get("qos"));
        this.agent = new GenericMqttAgent(broker, qos);
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
