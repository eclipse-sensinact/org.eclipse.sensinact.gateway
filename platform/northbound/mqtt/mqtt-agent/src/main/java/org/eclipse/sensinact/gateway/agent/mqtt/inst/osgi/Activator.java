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
package org.eclipse.sensinact.gateway.agent.mqtt.inst.osgi;

import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 * Extended {@link AbstractActivator}
 */
public class Activator extends AbstractActivator<Mediator> {

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception {
    	mediator.info("Starting MQTT Agents factory");
        super.mediator.register(new NorthboundBrokerManagedServiceFactory(super.mediator),
        	ManagedServiceFactory.class, new Hashtable() {{
        		this.put(Constants.SERVICE_PID, NorthboundBrokerManagedServiceFactory.MANAGER_NAME);
        	}});
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.agent.mqtt.generic.osgi.AbstractMqttActivator#doStop()
     */
    @Override
    public void doStop() throws Exception {
    	mediator.info("Stopping MQTT Agents factory");
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
     */
    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
