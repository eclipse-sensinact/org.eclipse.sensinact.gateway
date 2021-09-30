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
package org.eclipse.sensinact.gateway.sthbnd.http.android;

import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;

@RequireHttpWhiteboard
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends AbstractActivator<Mediator> {
	
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

    public LocalProtocolStackEndpoint<DevGenPacket> connector;

    @SuppressWarnings({ "unchecked","serial"})
	@Override
    public void doStart() {
    	findJettyClassLoader(super.mediator.getContext());
        ExtModelConfiguration<DevGenPacket> configuration = ExtModelConfigurationBuilder.instance(mediator, DevGenPacket.class
        		).withServiceBuildPolicy((byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy() | SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy())
        		).withResourceBuildPolicy((byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy() | SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy())
        		).withStartAtInitializationTime(true
        		).build("resources.xml", Collections.emptyMap());
        connector = new LocalProtocolStackEndpoint<DevGenPacket>(mediator);
        try {
			connector.connect(configuration);
		} catch (InvalidProtocolStackException e) {
			mediator.error(e);
			return;
		}

        super.mediator.register(
            	new Hashtable() {{
            		this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "/androidws");
            		this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=org.eclipse.sensinact)");}}, 
            	new WebSocketServlet() { 			
    				private static final long serialVersionUID = 1L;	
    				private AndroidWebSocketPool pool = new AndroidWebSocketPool(mediator, connector);    				
    		        private final AtomicBoolean firstCall = new AtomicBoolean(true); 
    		    
					private final CountDownLatch initBarrier = new CountDownLatch(1); 
    				@Override
    		        public void init() throws ServletException {
    		            mediator.info("The Echo servlet has been initialized, but we delay initialization until the first request so that a Jetty Context is available");	
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
                        factory.setCreator(pool);
                    };
                }, 
            	new Class[]{ Servlet.class, WebSocketServlet.class }
            );
            super.mediator.info(String.format("%s servlet registered", "/androidws"));
            
            super.mediator.register(new IndexFilter("/android"), Filter.class, new Hashtable() {{
            	this.put(Constants.SERVICE_RANKING, 3);
                this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, "/android");
                this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=org.eclipse.sensinact)");
                }
            });
            super.mediator.info(String.format("%s filter registered", "/android"));
            
            super.mediator.register(new ResourceFilter(super.mediator), Filter.class, new Hashtable() {{
            	this.put(Constants.SERVICE_RANKING, 2);
                this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN, "/android/*");
                this.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=org.eclipse.sensinact)");
                }
            });
            super.mediator.info(String.format("%s filter registered", "/android/*"));
    }

    @Override
    public void doStop() {
        mediator.info("Stopping bundle");
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
