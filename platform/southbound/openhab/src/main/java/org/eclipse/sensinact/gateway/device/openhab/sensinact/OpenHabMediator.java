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
package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.device.openhab.common.Broker;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class OpenHabMediator extends HttpMediator {

    private Map<String, Broker> brokerById;

    public OpenHabMediator(BundleContext context) {
        super(context);
        this.brokerById = new HashMap<String, Broker>();
    }

    void addBroker(String id, Broker broker) {
        brokerById.put(id, broker);
    }

    public Set<String> removeBroker(String endpointId) {
        return brokerById.remove(endpointId).getDevices();
    }

    public Set<String> updateBroker(String endpointId, Set<String> devices) {
        Broker broker = brokerById.get(endpointId);
        if (broker == null) {
            return Collections.<String>emptySet();
        }
        HashSet<String> toBeRemoved = new HashSet<String>(broker.getDevices());
        toBeRemoved.removeAll(devices);
        return toBeRemoved;
    }

    @Override
    public HttpTaskProcessingContextFactory getTaskProcessingContextFactory() {
        return new DefaultHttpTaskProcessingContextFactory(this) {
            @Override
            public HttpTaskProcessingContext newInstance(HttpTaskConfigurator httpTaskConfigurator, String endpointId, HttpTask<?, ?> task) {
                return new OpenHabTaskProcessingContext(OpenHabMediator.this, httpTaskConfigurator, endpointId, task);
            }
        };
    }

    class OpenHabTaskProcessingContext extends DefaultHttpTaskProcessingContext {
        public OpenHabTaskProcessingContext(Mediator mediator, HttpTaskConfigurator httpTaskConfigurator, final String endpointId, final HttpTask<?, ?> task) {
            super(mediator, httpTaskConfigurator, endpointId, task);
            properties.put("openhab.scheme", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    return brokerById.get(endpointId).getServer().getScheme();
                }
            });
            properties.put("openhab.host", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    return brokerById.get(endpointId).getServer().getHost();
                }
            });
            properties.put("openhab.port", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    return Integer.toString(brokerById.get(endpointId).getServer().getPort());
                }
            });
        }
    }
}
