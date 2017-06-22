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

package org.eclipse.sensinact.gateway.simulated.slider.osgi;

import java.util.Collections;

import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderAdapter;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderSetter;
import org.osgi.framework.BundleContext;

import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderGUI;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderPacket;

public class Activator extends AbstractActivator<Mediator>
{
    private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";

    private LocalProtocolStackEndpoint<SliderPacket> connector;
    
    private SliderSetterItf sliderPanel;
    private ServiceRegistration sliderRegistration;

    @SuppressWarnings({ "unchecked"})
	public void doStart() throws Exception
    {    	
    	ExtModelConfiguration manager = 
    	new ExtModelInstanceBuilder(super.mediator,SliderPacket.class
    		).withStartAtInitializationTime(true
    		).<ExtModelConfiguration>buildConfiguration(
    		"slider-resource.xml", Collections.<String,String>emptyMap());
    	
        connector = new LocalProtocolStackEndpoint<SliderPacket>(super.mediator);
        
    	connector.connect(manager);
        if(mediator.getContext().getProperty(GUI_ENABLED).equals("true"))
        {
            sliderPanel = new SliderGUI(new SliderAdapter(connector));
        }
        else
        {
            sliderPanel = new SliderSetter(new SliderAdapter(connector));
            sliderRegistration = mediator.getContext().registerService(
            		SliderSetterItf.class, sliderPanel, null);
        }
    }

    public void doStop() throws Exception 
    {
    	connector.stop();
    	connector = null;
    	
        sliderPanel.stop();
        if(sliderRegistration != null)
        {
            sliderRegistration.unregister();
        }
    }

    public Mediator doInstantiate(BundleContext context)
             
    {
        return new Mediator(context);
    }
}
