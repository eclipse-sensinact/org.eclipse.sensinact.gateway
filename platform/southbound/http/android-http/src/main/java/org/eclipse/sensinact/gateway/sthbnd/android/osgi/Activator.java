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

package org.eclipse.sensinact.gateway.sthbnd.android.osgi;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;

import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleContext;

import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.sthbnd.android.internal.AndroidPacket;
import org.eclipse.sensinact.gateway.sthbnd.android.internal.http.AndroidFilter;
import org.eclipse.sensinact.gateway.sthbnd.android.internal.http.AndroidServlet;

public class Activator extends AbstractActivator<Mediator>
{
    private static final String ROOT = "/android";

	private ServiceTracker tracker;
    private ExtHttpService httpService = null; 
    
    private LocalProtocolStackEndpoint<AndroidPacket> connector;
    private ExtModelConfiguration manager;

    @SuppressWarnings("unchecked")
	public void doStart() throws Exception
    {
	    
        if(manager == null)
        {
        	manager = new ExtModelInstanceBuilder(
        		super.mediator, AndroidPacket.class 
                ).withStartAtInitializationTime(true
                ).buildConfiguration("android-resource.xml",
                Collections.<String,String>emptyMap());

    		manager.setStartAtInitializationTime(true);
        }
        if(connector == null)
        {
        	connector = new LocalProtocolStackEndpoint<AndroidPacket>(super.mediator);
        }
        this.tracker = new ServiceTracker(super.mediator.getContext(), 
	    		ExtHttpService.class.getName(), null)
	    {
	        @Override
	        public Object addingService(ServiceReference serviceRef) 
	        {
	            httpService = (ExtHttpService) super.addingService(serviceRef);
	            registerServlets();
	            return httpService;
	        }
	
	        @Override
	        public void removedService(ServiceReference ref, Object service) 
	        {
	            if (httpService == service) 
	            {
	                unregisterServlets();
	                httpService = null;
	            }	
	            super.removedService(ref, service);
	        }
	    };
    	connector.connect(manager);
	    this.tracker.open();
    }

    /**
     * @inheritDoc
     *
     * @see AbstractActivator#doStop()
     */
    public void doStop() throws Exception 
    {
        this.tracker.close();
        this.unregisterServlets();
        this.connector.stop();
    }

	/**
	 * 
	 */
	private void registerServlets() 
    {       
        Dictionary<String, String> initParams = new Hashtable<String, String>();
        initParams.put("servlet-name", "AndroidServlet");

        AndroidServlet subscription = new AndroidServlet(this.connector,mediator);
        AndroidFilter filter = new AndroidFilter(this.mediator);
        try 
        { 	
        	HttpContext context = httpService.createDefaultHttpContext();
        	httpService.registerFilter(filter, ROOT.concat(".*"), 
        			new Hashtable(),0, context);
            httpService.registerServlet(ROOT,subscription, initParams,context);
            
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (NamespaceException e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private void unregisterServlets() 
    {
        if (this.httpService != null)
        {
            httpService.unregister(ROOT);
        }
    }

    /**
     * @inheritDoc
     *
     * @see AbstractActivator#
     * doInstantiate(org.osgi.framework.BundleContext)
     */
    public Mediator doInstantiate(BundleContext context)
    {
        return new Mediator(context);
    }
}
