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
package org.eclipse.sensinact.gateway.generic;

import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class ManagedBasisActivator<M extends Mediator> extends AbstractActivator<M> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ManagedBasisActivator.class);
	/**
	 * Returns the String value defining the name of the {@link ManagedServiceFactory}
	 * implementation to be registered
	 * 
	 * @return the name of the {@link ManagedServiceFactory}
	 */
	protected abstract String name();

	/**
	 * Returns the {@link ProtocolStackEndpointConfigurator}
	 * allowing to configure the {@link ProtocolStackEndpoint}s maintained by a
	 * {@link ManagedProtocolStackEndpointFactory} 
	 * 
	 * @return {@link ProtocolStackEndpointConfigurator} to be
	 * used to configure the {@link ProtocolStackEndpoint}s 
	 */
	protected abstract ProtocolStackEndpointConfigurator configurator();
	
	private ManagedProtocolStackEndpointFactory factory;

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStart()
     */
    @Override
    public void doStart() throws Exception {
    	LOG.info("Registering ManagedProtocolStackEndpointFactory");
    	this.factory = new ManagedProtocolStackEndpointFactory(super.mediator, name()) {
			@Override
			protected ProtocolStackEndpointConfigurator configurator() {
				return ManagedBasisActivator.this.configurator();
			}
    	};
        super.mediator.register(
        	this.factory,
        	ManagedServiceFactory.class, new Hashtable() {{this.put(Constants.SERVICE_PID, name());}}
        );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#doStop()
     */
    @Override
    public void doStop() throws Exception {
    	LOG.info("Unregistering ManagedProtocolStackEndpointFactory");
    	if(this.factory != null)
    		this.factory.stop();
    }
}
