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

//import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.Servlet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
//import org.osgi.framework.InvalidSyntaxException;
//import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

/**
 * A CallbackFactory is in charge of creating the {@link CallbackServlet}s attached
 * to one specific {@link ExtHttpService}, and configured by the {@link CallbackService}s
 * registered in the OSGi host environment
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class CallbackFactory {
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
            /**
             * @inheritDoc
             *
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#
             * execute(java.lang.Object)
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
            /**
             * @inheritDoc
             *
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#
             * execute(java.lang.Object)
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
        CallbackServlet callbackServlet = new CallbackServlet(mediator, callbackService);

        Dictionary props = callbackService.getProperties();
        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, endpoint);
        //props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ASYNC_SUPPORTED,true);
        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT,"("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"=org.osgi.service.http)");
        
        ServiceRegistration registration = mediator.getContext().registerService(Servlet.class, callbackServlet, props);
	    this.registrations.put(endpoint, registration);
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
    }
}
