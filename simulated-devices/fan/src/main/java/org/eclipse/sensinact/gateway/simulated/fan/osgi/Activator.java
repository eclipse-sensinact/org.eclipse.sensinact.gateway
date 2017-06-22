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

package org.eclipse.sensinact.gateway.simulated.fan.osgi;

import java.util.Collections;

import org.eclipse.sensinact.gateway.simulated.fan.internal.FanConfig;
import org.osgi.framework.BundleContext;


import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.simulated.fan.swing.FanPanel;

@SuppressWarnings({ "unchecked" })
public class Activator extends AbstractActivator<Mediator>
{
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";

    private LocalProtocolStackEndpoint<Packet> connector;
    private ExtModelConfiguration manager;
    private FanConfig config;
    private FanPanel fanPanel;

    public void doStart() throws Exception
    {
        config = new FanConfig();       
        if(manager == null)
        {
        	manager = new ExtModelInstanceBuilder(
        	super.mediator, Packet.class
                ).withStartAtInitializationTime(true
                ).buildConfiguration("fan-resource.xml", 
        		Collections.<String,String>emptyMap());
        }
        if(connector == null)
        {
        	connector = new LocalProtocolStackEndpoint<Packet>(super.mediator);
        	connector.addInjectableInstance(FanConfig.class, config);
        }
    	connector.connect(manager);

        if(mediator.getContext().getProperty(GUI_ENABLED).equals("true")) 
        {
            fanPanel = new FanPanel(super.mediator);
            config.addListener(fanPanel);
        }
    }

    public void doStop() throws Exception 
    {
    	connector.stop();
        if(fanPanel != null) {
            config.removeListener(fanPanel);
            fanPanel.stop();
        }
    }

    public Mediator doInstantiate(BundleContext context)
             
    {
        return new Mediator(context);
    }
}
