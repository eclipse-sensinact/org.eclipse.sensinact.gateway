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
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.DefaultHttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContext;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskProcessingContextFactory;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.osgi.framework.BundleContext;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class OpenHabMediator extends HttpMediator {

    class OpenHabBroker {

        String host;
        String port;
        Set<String> devices;
    }

    Map<String, OpenHabBroker> map;

    public OpenHabMediator(BundleContext context) {
        super(context);
        this.map = new HashMap<String, OpenHabBroker>();
    }

    void newBroker(String id, String host, String port) {
        OpenHabBroker openHabBroker = new OpenHabBroker();
        openHabBroker.host = host;
        openHabBroker.port = port;
        openHabBroker.devices = new HashSet<String>();
        this.map.put(id, openHabBroker);
    }

    public Set<String> deleteBroker(String endpointId) {
        return this.map.remove(endpointId).devices;
    }

    public Set<String> updateBroker(String endpointId, Set<String> devices) {
        OpenHabBroker broker = this.map.get(endpointId);
        if (broker == null) {
            return Collections.<String>emptySet();
        }
        HashSet<String> toBeRemoved = new HashSet<String>(broker.devices);
        toBeRemoved.removeAll(devices);
        return toBeRemoved;
    }

    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpMediator#getTaskProcessingContextFactory()
     */
    @Override
    public HttpTaskProcessingContextFactory getTaskProcessingContextFactory() {
        return new DefaultHttpTaskProcessingContextFactory(this) {
            @Override
            public HttpTaskProcessingContext newInstance(
                    HttpTaskConfigurator httpTaskConfigurator, String endpointId,
                    HttpTask<?, ?> task) {
                return new OpenHabTaskProcessingContext(
                        OpenHabMediator.this, httpTaskConfigurator, endpointId, task);
            }

        };
    }

    class OpenHabTaskProcessingContext extends DefaultHttpTaskProcessingContext {

        public OpenHabTaskProcessingContext(Mediator mediator,
                HttpTaskConfigurator httpTaskConfigurator,
                final String endpointId, final HttpTask<?, ?> task) {
            super(mediator, httpTaskConfigurator, endpointId, task);
            super.properties.put("openhab.host", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    return OpenHabMediator.this.map.get(endpointId).host;
                }
            });
            super.properties.put("openhab.port", new Executable<Void, String>() {
                @Override
                public String execute(Void parameter) throws Exception {
                    return OpenHabMediator.this.map.get(endpointId).port;
                }
            });
        }

    }
}
