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
package org.eclipse.sensinact.studio.web;

import javax.servlet.ServletException;

import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that published studio-lite on Jetty server
 * @author Jander Nascimento
 */
public class Activator implements BundleActivator {

    private ServiceTracker tracker;
    private ExtHttpService httpService;

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private static final String STUDIO_ALIAS = "/studio-web";
    private static final String SWAGGER_ALIAS = "/swagger-api";

    public void start(BundleContext context){

        this.tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(context, HttpService.class.getName(), null) {
            /**
             * @see ServiceTracker#addingService(org.osgi.framework.ServiceReference)
             */
            public ExtHttpService addingService(ServiceReference<ExtHttpService> serviceRef) {
                httpService = super.addingService(serviceRef);

                HttpContext context = httpService.createDefaultHttpContext();

                ResourceServlet servlet = new ResourceServlet(STUDIO_ALIAS);

                try {
                    httpService.registerServlet(STUDIO_ALIAS, servlet, null, context);
                    httpService.registerFilter(
                            new IndexFilter(STUDIO_ALIAS), "^\\" + STUDIO_ALIAS + "\\/?", null, 0, context);
                } catch (ServletException e) {
                    e.printStackTrace();
                } catch (NamespaceException e) {
                    e.printStackTrace();
                }

                LOG.info("Studio Web is running on " + STUDIO_ALIAS + " context");

                try {
					httpService.registerServlet(SWAGGER_ALIAS, new ResourceServlet(SWAGGER_ALIAS), null, context);
                    httpService.registerFilter(
                            new IndexFilter(SWAGGER_ALIAS), "^\\" + SWAGGER_ALIAS + "\\/?", null, 0, context);
				} catch (ServletException e) {
					e.printStackTrace();
				} catch (NamespaceException e) {
					e.printStackTrace();
				}

                LOG.info("Swagger API is running on " + SWAGGER_ALIAS + " context");

                return httpService;
            }

            /**
             * @see ServiceTracker#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
             */
            public void removedService(ServiceReference<ExtHttpService> ref, ExtHttpService service) {
                if (httpService == service) {
                    unregisterServlets();
                    httpService = null;
                }

                super.removedService(ref, service);
            }
        };

        this.tracker.open(true);
    }

    public void stop(BundleContext context){
        this.tracker.close();
        unregisterServlets();

        LOG.info("Studio Web was unregistered from {} context", STUDIO_ALIAS);
        LOG.info("Swagger API was unregistered from {} context", SWAGGER_ALIAS);
    }

    /**
     * Unregister the servlet
     */
    private void unregisterServlets() {
        if (this.httpService != null) {
            httpService.unregister(STUDIO_ALIAS);
            httpService.unregister(SWAGGER_ALIAS);
        }
    }
}
