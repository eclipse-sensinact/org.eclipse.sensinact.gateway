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
package org.eclipse.sensinact.gateway.agent.mqtt.inst.osgi;

import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended {@link AbstractActivator}
 */
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
	
	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    @Override
    public void doStart() throws Exception {
    	LOG.info("Starting MQTT Agents factory");
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
    	LOG.info("Stopping MQTT Agents factory");
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doInstantiate(org.osgi.framework.BundleContext)
     */
    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
