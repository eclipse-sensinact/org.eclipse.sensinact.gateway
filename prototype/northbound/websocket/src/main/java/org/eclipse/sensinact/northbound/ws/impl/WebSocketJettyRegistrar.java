/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.ws.impl;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.core.server.WebSocketServerComponents;
import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletAsyncSupported;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@Component(service = { Servlet.class, JettyWebSocketServlet.class })
@RequireHttpWhiteboard
@HttpWhiteboardServletPattern("/ws/sensinact")
@HttpWhiteboardServletAsyncSupported
public class WebSocketJettyRegistrar extends JettyWebSocketServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    SensiNactSessionManager sessionManager;

    @Reference
    IQueryHandler queryHandler;

    /**
     * WebSocket sessions tracker
     */
    private WebSocketCreator sessionPool;

    /**
     * Flag to indicate if the servlet was initialized
     */
    private final AtomicBoolean initCalled = new AtomicBoolean(false);

    /**
     * Lets queries wait for initialization
     */
    private final CountDownLatch initComplete = new CountDownLatch(1);

    @Activate
    void activate(final ComponentContext ctx) {
        sessionPool = new WebSocketCreator(sessionManager, queryHandler);
    }

    @Deactivate
    void stop() {
        // Close all web sockets
        sessionPool.close();
        sessionManager = null;
    }

    @Override
    public void init() throws ServletException {
        // Deliberately block initialization. This is needed as there is
        // no Jetty context on the thread until a request occurs
    }

    @Override
    public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
        // Check to see if init needs calling
        if (initCalled.getAndSet(true)) {
            // Someone else is responsible, wait until they are done
            try {
                initComplete.await();
            } catch (InterruptedException e) {
                throw new ServletException(e);
            }
        } else {
            // Initialise now
            try {
                ServletContext servletContext = getServletContext();
                ServletContextHandler contextHandler = ServletContextHandler.getServletContextHandler(servletContext,
                        "Jetty WebSocket init");
                WebSocketServerComponents.ensureWebSocketComponents(contextHandler.getServer(), servletContext);
                JettyWebSocketServerContainer.ensureContainer(servletContext);
                super.init();
            } finally {
                // Tell other callers that they can stop waiting
                initComplete.countDown();
            }
        }
        // Normal service resumes
        super.service(req, res);
    }

    @Override
    protected void configure(final JettyWebSocketServletFactory factory) {
        factory.setCreator(sessionPool);
    }
}
