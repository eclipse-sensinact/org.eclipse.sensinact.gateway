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
package org.eclipse.sensinact.gateway.sthbnd.http.android;

import org.apache.felix.http.api.ExtHttpService;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import javax.servlet.ServletException;
import java.util.Collections;
import java.util.Hashtable;

public class Activator extends AbstractActivator {
    public LocalProtocolStackEndpoint<DevGenPacket> connector;
    public AndroidWebSocketPool pool;
    private BundleContext context;

    @Override
    public void doStart() {
        try {
            ExtModelConfiguration<DevGenPacket> configuration = ExtModelConfigurationBuilder.instance(mediator, DevGenPacket.class
            		).withServiceBuildPolicy((byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy())
            		).withResourceBuildPolicy((byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy())
            		).withStartAtInitializationTime(true
            		).build("devgen-resource.xml", Collections.emptyMap());
            connector = new LocalProtocolStackEndpoint<DevGenPacket>(mediator);
            connector.connect(configuration);
            pool = new AndroidWebSocketPool(mediator, connector);
            ServiceTracker st = new ServiceTracker(mediator.getContext(), ExtHttpService.class.getName(), new ServiceTrackerCustomizer() {
                @Override
                public Object addingService(ServiceReference serviceReference) {
                    BundleContext useContext = mediator.getContext();
                    ClassLoader current = Thread.currentThread().getContextClassLoader();
                    ClassLoader targetClassloader = useContext.getBundle().adapt(BundleWiring.class).getClassLoader();
                    Thread.currentThread().setContextClassLoader(targetClassloader);
                    //ExtHttpService service= (ExtHttpService) mediator.getContext().getService(serviceReference);
                    ExtHttpService service = (ExtHttpService) useContext.getService(serviceReference);
                    HttpContext httpContext = service.createDefaultHttpContext();
                    Hashtable params = new Hashtable<String, Object>();
                    try {
                        service.registerServlet("/androidws", new WebSocketServlet() {
                            @Override
                            public void configure(WebSocketServletFactory factory) {
                                factory.getPolicy().setIdleTimeout(1000 * 3600);
                                factory.setCreator(pool);
                            }

                            ;
                        }, params, httpContext);
                        service.registerResources("/android", "/android", httpContext);
                    } catch (NamespaceException e) {
                        mediator.error(e);
                    } catch (ServletException e) {
                        mediator.error(e);
                    } finally {
                        Thread.currentThread().setContextClassLoader(current);
                    }
                    return new Object();
                }

                @Override
                public void modifiedService(ServiceReference serviceReference, Object o) {
                }

                @Override
                public void removedService(ServiceReference serviceReference, Object o) {
                }
            });
            st.open(false);
        } catch (Exception e) {
            mediator.error(e);
        }
    }

    @Override
    public void doStop() {
        mediator.info("Stopping bundle");
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        this.context = context;
        return new Mediator(context);
    }
}
