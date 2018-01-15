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

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderPacket;
import org.eclipse.sensinact.gateway.simulated.slider.internal.SliderProtocolStackEndpoint;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractActivator<Mediator>
{
    private SliderProtocolStackEndpoint connector;
    

	public void doStart() throws Exception
    {    	
    	ExtModelConfiguration manager = 
    	new ExtModelInstanceBuilder(super.mediator, SliderPacket.class
    		).withStartAtInitializationTime(true
    		).<ExtModelConfiguration>buildConfiguration(
    		"slider-resource.xml", Collections.<String,String>emptyMap());
    	
        connector = new SliderProtocolStackEndpoint(super.mediator);        
    	connector.connect(manager);        
    }

    public void doStop() throws Exception 
    {
    	connector.stop();
    	connector = null;
    }

    public Mediator doInstantiate(BundleContext context)        
    {
        return new Mediator(context);
    }
}
