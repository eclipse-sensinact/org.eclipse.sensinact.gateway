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
package org.eclipse.sensinact.gateway.nthbnd.http.forward.internal;

import org.apache.felix.http.api.ExtHttpService;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.forward.ForwardingService;
import org.osgi.service.http.HttpContext;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A ForwardingFactory is in charge of creating the {@link ForwardingFilter}s attached
 * to one specific {@link ExtHttpService}, and configured by the {@link ForwardingService}s
 * registered in the OSGi host environment
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ForwardingFactory {
    private Mediator mediator;
    private String appearingKey;
    private String disappearingKey;

    private ExtHttpService extHttpService;
    private Map<String, ForwardingFilter> filters;

    private volatile int ranking;
    private final AtomicBoolean running;

    /**
     * Constructor
     *
     * @param mediator       the {@link Mediator} allowing the ForwardingFactory
     *                       to be instantiated to interact with the OSGi host environment
     * @param extHttpService the {@link ExtHttpService} to which the
     *                       ForwardingFactory to be instantiated will register {@link ForwardingFilter}s
     *                       according to the registered {@link ForwardingService}
     */
    public ForwardingFactory(Mediator mediator, ExtHttpService extHttpService) {
        this.mediator = mediator;
        this.extHttpService = extHttpService;
        this.filters = Collections.synchronizedMap(new HashMap<String, ForwardingFilter>());
        this.running = new AtomicBoolean(false);
    }

    /**
     * Starts this ForwardingFactory and starts to observe the registration and
     * the unregistration of the {@link ForwardingService}s
     */
    public void start() {
        if (this.running.get()) {
            return;
        }
        this.running.set(true);
        attachAll();
        this.appearingKey = mediator.attachOnServiceAppearing(ForwardingService.class, (String) null, new Executable<ForwardingService, Void>() {
            @Override
            public Void execute(ForwardingService forwardingService) throws Exception {
                attach(forwardingService);
                return null;
            }
        });
        this.disappearingKey = mediator.attachOnServiceDisappearing(ForwardingService.class, (String) null, new Executable<ForwardingService, Void>() {
            @Override
            public Void execute(ForwardingService forwardingService) throws Exception {
                detach(forwardingService);
                return null;
            }
        });
    }

    /**
     * Stops this ForwardingFactory and stops to observe the registration and
     * the unregistration of the {@link ForwardingService}s
     */
    public void stop() {
        if (!this.running.get()) {
            return;
        }
        this.running.set(false);
        mediator.detachOnServiceAppearing(ForwardingService.class, (String) null, appearingKey);
        mediator.detachOnServiceDisappearing(ForwardingService.class, (String) null, disappearingKey);
        detachAll();
    }

    /**
     * Detaches all the {@link ForwardingService}s registered into the
     * OSGi host environment
     */
    public void detachAll() {
        mediator.callServices(ForwardingService.class, new Executable<ForwardingService, Void>() {
            /**
             * @inheritDoc
             *
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#
             * execute(java.lang.Object)
             */
            @Override
            public Void execute(ForwardingService forwardingService) throws Exception {
                detach(forwardingService);
                return null;
            }
        });
    }

    /**
     * Attaches all the {@link ForwardingService}s registered into the
     * OSGi host environment
     */
    public void attachAll() {
        mediator.callServices(ForwardingService.class, new Executable<ForwardingService, Void>() {
            /**
             * @inheritDoc
             *
             * @see org.eclipse.sensinact.gateway.common.execution.Executable#
             * execute(java.lang.Object)
             */
            @Override
            public Void execute(ForwardingService forwardingService) throws Exception {
                attach(forwardingService);
                return null;
            }
        });
    }

    /**
     * Attaches the {@link ForwardingService} passed as parameter by
     * registering a newly created {@link ForwardingFilter} based on it
     *
     * @param forwardingService the {@link ForwardingService} to be attached
     */
    public final void attach(ForwardingService forwardingService) {
        if (forwardingService == null || !this.running.get()) {
            return;
        }
        String endpoint = forwardingService.getPattern();
        if (endpoint == null || endpoint.length() == 0 || "/".equals(endpoint)) {
            mediator.error("Invalid endpoint '%s' - expected '^|/([^/]+)(/([^/]+)*'", endpoint);
            return;
        }
        if (!endpoint.startsWith("/")) {
            endpoint = "/".concat(endpoint);
        }
        if (filters.containsKey(endpoint)) {
            mediator.error("A forwarding service is already registered at '%s'", endpoint);
            return;
        }
        ForwardingFilter forwardingFilter = new ForwardingFilter(mediator, forwardingService.getUriBuilder(), forwardingService.getQueryBuilder());

        Dictionary props = forwardingService.getProperties();
        HttpContext context = extHttpService.createDefaultHttpContext();
        try {
            extHttpService.registerFilter(forwardingFilter, endpoint, props, ranking++, context);
            mediator.info("Forwarding filter '%s' registered", endpoint);
            filters.put(endpoint, forwardingFilter);
        } catch (Exception e) {
            mediator.error(e);
        }
    }

    /**
     * Detaches the {@link ForwardingService} passed as parameter by
     * unregistering the {@link ForwardingFilter} that is based on it
     *
     * @param forwardingService the {@link ForwardingService} to be detached
     */
    public final void detach(ForwardingService forwardingService) {
        if (forwardingService == null) {
            return;
        }
        String endpoint = forwardingService.getPattern();
        ForwardingFilter forwardingFilter = filters.get(endpoint);
        if (forwardingFilter == null) {
            mediator.warn("The specified forwarding service '%s' was not registered", endpoint);
            return;
        }
        try {
            extHttpService.unregisterFilter(forwardingFilter);
            mediator.info("Forwarding filter '%s' unregistered", endpoint);
        } catch (Exception e) {
            mediator.error(e);
        }
        return;
    }
}
