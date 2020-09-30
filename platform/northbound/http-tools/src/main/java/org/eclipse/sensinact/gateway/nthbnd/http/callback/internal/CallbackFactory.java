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
package org.eclipse.sensinact.gateway.nthbnd.http.callback.internal;

import java.io.IOException;
//import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/**
 * A CallbackFactory is in charge of creating the {@link CallbackServlet}s attached
 * to one specific {@link ExtHttpService}, and configured by the {@link CallbackService}s
 * registered in the OSGi host environment
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class CallbackFactory {

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
    
    private Mediator mediator;
    private String appearingKey;
    private String disappearingKey;

    private Map<String, ServiceRegistration> registrations;

    private final AtomicBoolean running;

    /**
     * Constructor
     *
     * @param mediator       the {@link Mediator} allowing the CallbackFactory
     *                       to be instantiated to interact with the OSGi host environment
     */
    public CallbackFactory(Mediator mediator) {
    	if(CallbackFactory.loader == null) {
    		findJettyClassLoader(mediator.getContext());
    	}
        this.mediator = mediator;
        this.registrations = Collections.synchronizedMap(new HashMap<String, ServiceRegistration>());
        this.running = new AtomicBoolean(false);
    }

    /**
     * Starts this ForwardingInstaller and starts to observe the registration and
     * the unregistration of the {@link CallbackService}s
     */
    public void start() {
        if (this.running.get()) {
            return;
        }
        this.running.set(true);
        attachAll();
        this.appearingKey = mediator.attachOnServiceAppearing(CallbackService.class, (String) null, new Executable<CallbackService, Void>() {
            @Override
            public Void execute(CallbackService callbackService) throws Exception {
                attach(callbackService);
                return null;
            }
        });
        this.disappearingKey = mediator.attachOnServiceDisappearing(CallbackService.class, (String) null, new Executable<CallbackService, Void>() {
            @Override
            public Void execute(CallbackService callbackService) throws Exception {
                detach(callbackService);
                return null;
            }
        });
    }

    /**
     * Stops this ForwardingInstaller and stops to observe the registration and
     * the unregistration of the {@link CallbackService}s
     */
    public void stop() {
        if (!this.running.get()) {
            return;
        }
        this.running.set(false);
        mediator.detachOnServiceAppearing(CallbackService.class, (String) null, appearingKey);
        mediator.detachOnServiceDisappearing(CallbackService.class, (String) null, disappearingKey);
        detachAll();
    }

    /**
     * Detaches all the {@link CallbackService}s registered into the
     * OSGi host environment
     */
    public void detachAll() {
        mediator.callServices(CallbackService.class, new Executable<CallbackService, Void>() {
            /* (non-Javadoc)
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
             */
            @Override
            public Void execute(CallbackService callbackService) throws Exception {
                detach(callbackService);
                return null;
            }
        });
    }

    /**
     * Attaches all the {@link CallbackService}s registered into the
     * OSGi host environment
     */
    public void attachAll() {
        mediator.callServices(CallbackService.class, new Executable<CallbackService, Void>() {
            /* (non-Javadoc)
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
             */
            @Override
            public Void execute(CallbackService callbackService) throws Exception {
                attach(callbackService);
                return null;
            }
        });
    }

    /**
     * Attaches the {@link CallbackService} passed as parameter by
     * registering a newly created {@link CallbackServlet} based on it
     *
     * @param callbackService the {@link CallbackService} to be attached
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public final void attach(CallbackService callbackService) {
        if (callbackService == null || !this.running.get()) {
            return;
        }
        String endpoint = callbackService.getPattern();
        if (endpoint == null || endpoint.length() == 0 || "/".equals(endpoint)) {
            mediator.error("Invalid endpoint '%s' - expected '^|/([^/]+)(/([^/]+)*'", endpoint);
            return;
        }
        if (!endpoint.startsWith("/")) {
            endpoint = "/".concat(endpoint);
        }
        if (registrations.containsKey(endpoint)) {
            mediator.error("A callback service is already registered at '%s'", endpoint);
            return;
        }
        int callbackType = callbackService.getCallbackType();
        if((callbackType & CallbackService.CALLBACK_SERVLET) == CallbackService.CALLBACK_SERVLET) {
	        
        	CallbackServlet callbackServlet = new CallbackServlet(mediator, callbackService);

	        Dictionary props = callbackService.getProperties();
	        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, endpoint);
	        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)");

	        ServiceRegistration registration = mediator.getContext().registerService(Servlet.class, callbackServlet, props);
		    this.registrations.put(endpoint, registration);
        }
        if((callbackType & CallbackService.CALLBACK_WEBSOCKET) == CallbackService.CALLBACK_WEBSOCKET) {
        	String wsEndpoint = endpoint;
        	if(!endpoint.startsWith("/ws/")) {
        		wsEndpoint = "/ws".concat(endpoint);
        	}
        	Dictionary props = callbackService.getProperties();
        	props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, wsEndpoint);
        	props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=default)"); 
        	
        	WebSocketServlet webSocketServlet = new WebSocketServlet() { 			
				private static final long serialVersionUID = 1L;	
				private CallbackWebSocketPool pool = new CallbackWebSocketPool(mediator, callbackService);    				
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
            };
	        ServiceRegistration registration = mediator.getContext().registerService( 
	        	new String[]{ Servlet.class.getName(), WebSocketServlet.class.getName() }, 
	        	webSocketServlet, props);
	        
		    this.registrations.put(wsEndpoint, registration);		    
		    this.mediator.info(String.format("%s servlet registered", wsEndpoint));
        }
    }

    /**
     * Detaches the {@link CallbackService} passed as parameter by
     * unregistering the {@link CallbackServlet} that is based on it
     *
     * @param callbackService the {@link CallbackService} to be detached
     */
    public final void detach(CallbackService callbackService) {
        if (callbackService == null) {
            return;
        }
        String endpoint = callbackService.getPattern(); 
        if (!endpoint.startsWith("/")) {
            endpoint = "/".concat(endpoint);
        }
        ServiceRegistration registration = this.registrations.get(endpoint);
    	if(registration != null) {
    		try {
    			registration.unregister();
                mediator.info("Callback servlet '%s' unregistered", endpoint);
    		}catch(IllegalStateException e) {
    			//do nothing
    		}
    		registration = null;
    	}
    	if(!endpoint.startsWith("/ws/")) {
	    	registration = this.registrations.get("/ws".concat(endpoint));
	    	if(registration != null) {
	    		try {
	    			registration.unregister();
	                mediator.info("Callback servlet '%s' unregistered", endpoint);
	    		}catch(IllegalStateException e) {
	    			//do nothing
	    		}
	    		registration = null;
	    	}
    	}
    }
}
