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

package org.eclipse.sensinact.gateway.system.osgi;

import java.io.IOException;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sensinact.gateway.system.listener.SystemAgent;
import org.osgi.framework.BundleContext;

import org.xml.sax.SAXException;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.system.internal.SystemPacket;

public class Activator extends AbstractActivator<Mediator> {

    private ExtModelConfiguration manager = null;
    private LocalProtocolStackEndpoint<SystemPacket> connector = null;

    private String agent = null;
    private String registration = null;
    private Session session;

    /**
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @inheritDoc
     *
     * @see AbstractActivator#doStart()
     */
    public void doStart() throws Exception 
    {        
        if(manager == null)
        {
        	manager = new ExtModelInstanceBuilder(
        	super.mediator, SystemPacket.class
            	).withStartAtInitializationTime(true
                ).buildConfiguration("system-resource.xml", 
                Collections.<String,String>emptyMap());
        }
        if(this.connector == null)
        {
        	this.connector = new LocalProtocolStackEndpoint<SystemPacket>(mediator);
        }
        this.connector.connect(manager);
        
        this.agent = mediator.callService(SecuredAccess.class, 
        		new Executable<SecuredAccess,String>()
		{
			@Override
			public String execute(SecuredAccess service) 
					throws Exception 
			{
				return service.registerAgent(mediator,
					new SystemAgent(mediator, connector), null);
			}	
		});
    }

    /**
     * @inheritDoc
     *
     * @see AbstractActivator#doStop()
     */
    public void doStop() 
    {
    	if(this.connector != null)
    	{
    		this.connector.stop();
    		this.connector = null;
    	}
        this.manager = null;

        mediator.callService(SecuredAccess.class, 
    		new Executable<SecuredAccess,Void>()
		{
			@Override
			public Void execute(SecuredAccess service) 
					throws Exception 
			{
				service.unregisterAgent(
						Activator.this.agent);
				return null;
			}
	
		});
        this.agent = null;
    }

    /**
     * @inheritDoc
     *
     * @see AbstractActivator#
     * doInstantiate(org.osgi.framework.BundleContext, int, java.io.FileOutputStream)
     */
    public Mediator doInstantiate(BundleContext context)
    {
        return new Mediator(context);
    }
}
