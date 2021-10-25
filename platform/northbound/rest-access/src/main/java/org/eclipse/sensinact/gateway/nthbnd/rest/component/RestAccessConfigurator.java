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
package org.eclipse.sensinact.gateway.nthbnd.rest.component;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.sensinact.gateway.common.interpolator.Interpolator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccessConstants;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.http.CorsFilter;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.http.HttpEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.http.HttpLoginEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.http.HttpRegisteringEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws.WebSocketConnectionFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
@Component(immediate=true)
@RequireHttpWhiteboard
public class RestAccessConfigurator {
   
	private static final Logger LOG = LoggerFactory.getLogger(RestAccessConfigurator.class);
	private CorsFilter corsFilter = null;
    private boolean corsHeader = false;
	private NorthboundMediator mediator;
    
    private static ClassLoader loader = null;
    
    private static void findJettyClassLoader(BundleContext context) {
    	Bundle[] bundles = context.getBundles();
    	for(Bundle bundle:bundles) {
    		if("org.apache.felix.http.jetty".equals(bundle.getSymbolicName())) {
    			try {
    				BundleWiring wire = bundle.adapt(BundleWiring.class);
    				loader = wire.getClassLoader();
    			}catch(Exception e) {
    				e.printStackTrace();
    				loader = WebSocketServlet.class.getClassLoader();
    			}
    			break;
    		}
    	}
    }

    protected void injectPropertyFields() throws Exception {
        LOG.debug("Starting introspection in bundle %s", mediator.getContext().getBundle().getSymbolicName());
        Interpolator interpolator = new Interpolator(this.mediator);
        interpolator.getInstance(this);
        for(Map.Entry<String,String> entry:interpolator.getPropertiesInjected().entrySet()){
            if(!this.mediator.getProperties().containsKey(entry.getKey()))
                mediator.setProperty(entry.getKey(),entry.getValue());
        }
    }
    
    @Activate
	public void activate(ComponentContext componentContext) throws Exception {
    	
    	this.mediator = new NorthboundMediator(componentContext.getBundleContext());

        injectPropertyFields();
    	findJettyClassLoader(this.mediator.getContext());
    	        
    	this.corsHeader = Boolean.valueOf((String) this.mediator.getProperty(RestAccessConstants.CORS_HEADER));
        if (this.corsHeader) {
            this.corsFilter = new CorsFilter(mediator);                    
            mediator.register(RestAccessConfigurator.this.corsFilter, Filter.class, new Hashtable() {{
            	this.put(Constants.SERVICE_RANKING, 1);
                this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, "/*");
                this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
               }
            });
        }
        
        this.mediator.register(
        	new Hashtable() {{
        		this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, RestAccessConstants.WS_ROOT);
        		this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");}}, 
        	new WebSocketServlet() { 			
				private static final long serialVersionUID = 1L;			
				private WebSocketConnectionFactory sessionPool = new WebSocketConnectionFactory(RestAccessConfigurator.this.mediator);
				
		        private final AtomicBoolean firstCall = new AtomicBoolean(true); 
		    
		        private final CountDownLatch initBarrier = new CountDownLatch(1); 
				@Override
		        public void init() throws ServletException {
		            LOG.info("The Echo servlet has been initialized, but we delay initialization until the first request so that a Jetty Context is available");	
		        }
			
		        @Override
		        public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
		            if(firstCall.compareAndSet(true, false)) {
		                try {         		        	
		                    delayedInit();
		                } finally {
		                    initBarrier.countDown();
		                }
		            } else {
		                try {
		                    initBarrier.await();
		                } catch (InterruptedException e) {
		                    throw new ServletException("Timed out waiting for initialisation", e);
		                }
		            }				
		            super.service(arg0, arg1);
		        }

		        private void delayedInit() throws ServletException {
		            Thread currentThread = Thread.currentThread();
		            ClassLoader tccl = currentThread.getContextClassLoader();
		            currentThread.setContextClassLoader(loader);
		            try {
		                super.init();
		            } catch(Exception e) {
		            	e.printStackTrace();		            
		            } finally {
		                currentThread.setContextClassLoader(tccl);
		            }
		        }
				
				@Override
                public void configure(WebSocketServletFactory factory) {
                    factory.getPolicy().setIdleTimeout(1000 * 3600);
                    factory.setCreator(sessionPool);
                };
            }, 
        	new Class[]{ Servlet.class, WebSocketServlet.class });
        LOG.info(String.format("%s servlet registered", RestAccessConstants.WS_ROOT));
        
        this.mediator.register(new HttpLoginEndpoint(mediator), Servlet.class, new Hashtable() {{
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, RestAccessConstants.LOGIN_ENDPOINT);
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
        	}
        });
        LOG.info(String.format("%s servlet registered", RestAccessConstants.LOGIN_ENDPOINT));
        
        mediator.register(new HttpRegisteringEndpoint(mediator), Servlet.class, new Hashtable() {{
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, RestAccessConstants.REGISTERING_ENDPOINT);
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
        	}
        });
        LOG.info(String.format("%s servlet registered", RestAccessConstants.REGISTERING_ENDPOINT));
        
        mediator.register(new HttpEndpoint(mediator), Servlet.class, new Hashtable() {{
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, RestAccessConstants.HTTP_ROOT);
        	this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");
        	}
        });      
        LOG.info(String.format("%s servlet registered", RestAccessConstants.HTTP_ROOT));
    }

    @Deactivate
    public void deactivate() throws Exception {
    }

}
