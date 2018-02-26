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

package org.eclipse.sensinact.gateway.nthbnd.rest.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.http.api.ExtHttpService;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoints;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.http.CorsFilter;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.http.HttpEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws.WebSocketWrapperPool;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.http.HttpContext;

import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccessConstants;

/**
 * @see AbstractActivator
 */
public class Activator extends AbstractActivator<NorthboundMediator>
{
	private static final String HTTP_ROOT = "/";
	private static final String WS_ROOT = "/ws";
	
	/**
	 * @param context
	 * @return
	 */
	private static final ClassLoader getJettyBundleClassLoader(
			BundleContext context)
	{
        Bundle[] bundles = context.getBundles();
        int index=0;
        int length = bundles==null?0:bundles.length;
        
        ClassLoader loader = null;
        
        for(;index < length; index++)
        {
        	if("org.apache.felix.http.jetty".equals(
        			bundles[index].getSymbolicName()))
        	{
        		BundleWiring wiring = bundles[index].adapt(BundleWiring.class);
        		loader = wiring.getClassLoader();
        		break;
        	}
        }
        return loader;
	}
	
	private CorsFilter corsFilter = null;
	private boolean corsHeader = false;
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#
	 * doStart()
	 */
	public void doStart() throws Exception
	{
	    this.corsHeader = Boolean.valueOf((String)
	    		 super.mediator.getProperty(RestAccessConstants.CORS_HEADER));
	    
	    mediator.onServiceAppearing(ExtHttpService.class, null, 
	    new Executable<ExtHttpService,Void>()
	    {
            /**
             * @inheritDoc
             *
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
             */
            public Void execute( ExtHttpService service)
            {	        	
	    		if (Activator.this.corsHeader)
	    		{
	    			Activator.this.corsFilter = new CorsFilter(mediator);	    			
		        	try
					{
						service.registerFilter(corsFilter,".*", null, 0, null);
						Activator.this.mediator.info("CORS filter registered");
					}
					catch (Exception e)
					{
				        mediator.error(e);
					}
	    		}		        
				Dictionary<String, Object> params = new Hashtable<String, Object>();
		        params.put(Mediator.class.getCanonicalName(), Activator.this.mediator);
		        try
		        {					
					HttpContext context = service.createDefaultHttpContext();
			        service.registerServlet(HTTP_ROOT, new HttpEndpoint(mediator),params, context);
					Activator.this.mediator.info(String.format("%s servlet registered", HTTP_ROOT));
					
					params = new Hashtable<String, Object>();
			        params.put(Mediator.class.getCanonicalName(), Activator.this.mediator);
			        
			        final WebSocketWrapperPool sessionPool = new WebSocketWrapperPool(
			        		Activator.this.mediator);
			        			
			        //define the current thread classloader to avoid ServiceLoader error
			        ClassLoader current = Thread.currentThread().getContextClassLoader();			        
			        Thread.currentThread().setContextClassLoader(Activator.getJettyBundleClassLoader(
			        				mediator.getContext()));
			        try
			        {				        
				        service.registerServlet(WS_ROOT, new WebSocketServlet() 
				        {				            
				            /** 
				             * @inheritDoc
				             * 
				             * @see org.eclipse.jetty.websocket.servlet.WebSocketServlet#
				             * configure(org.eclipse.jetty.websocket.servlet.WebSocketServletFactory)
				             */
				            @Override
				            public void configure(WebSocketServletFactory factory)
				            {
				                factory.getPolicy().setIdleTimeout(1000*3600);
				                factory.setCreator(sessionPool);
				            };
				        }, params, context);
				        
			        } finally
			        {
			        	Thread.currentThread().setContextClassLoader(current);
			        }
					mediator.info(String.format("%s servlet registered", WS_ROOT));
		        }
		        catch (Exception e)
		        {
			       mediator.error(e);
		        }
                return null;
            }
	    });
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.bundle.AbstractActivator#
	 * doStop()
	 */
	public void doStop() throws Exception
	{
		mediator.callServices(ExtHttpService.class,
		new Executable<ExtHttpService,Void>()
		{
			@Override
			public Void execute(ExtHttpService service) throws Exception 
			{
            	try
            	{
            		service.unregister(HTTP_ROOT);
            	
            	} catch(Exception e)
            	{
            		mediator.error(e);
            	}
            	try
            	{
            		service.unregister(WS_ROOT);
            	
            	} catch(Exception e)
            	{
            		mediator.error(e);
            	}
            	try
            	{
            		service.unregisterFilter(corsFilter);
            	
            	} catch(Exception e)
            	{
            		mediator.error(e);
            	}
				return null;
			}
		});
	}

	/**
	 * @inheritDoc
	 *
	 * @see AbstractActivator#
	 * doInstantiate(org.osgi.framework.BundleContext, int, java.io.FileOutputStream)
	 */
	@Override
	public NorthboundMediator doInstantiate(BundleContext context)
	{
		NorthboundMediator mediator = new NorthboundMediator(context);
		mediator.setProperty(RestAccessConstants.NORTHBOUND_ENDPOINTS, 
				new NorthboundEndpoints(mediator));
		return mediator;
	}
}
