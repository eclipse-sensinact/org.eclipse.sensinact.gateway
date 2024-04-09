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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.sensinact.northbound.security.api.Authenticator.Scheme.TOKEN;
import static org.eclipse.sensinact.northbound.security.api.Authenticator.Scheme.USER_PASSWORD;
import static org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.core.server.WebSocketServerComponents;
import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.security.api.Authenticator;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.security.api.Authenticator.Scheme;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.annotations.RequireHttpWhiteboard;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardFilterPattern;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletAsyncSupported;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@Component(service = { Servlet.class, Filter.class }, configurationPid = "sensinact.northbound.websocket")
@RequireHttpWhiteboard
@HttpWhiteboardServletPattern("/ws/sensinact")
@HttpWhiteboardFilterPattern("/ws/sensinact")
@HttpWhiteboardServletAsyncSupported
public class WebSocketJettyRegistrar extends JettyWebSocketServlet implements Filter {

    static final String SENSINACT_USER_INFO = "sensinact.user.info";

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketJettyRegistrar.class);

    private static final long serialVersionUID = 1L;

    @interface Config {
        boolean allow_anonymous() default false;
    }

    @Reference
    SensiNactSessionManager sessionManager;

    @Reference
    IQueryHandler queryHandler;

    @Reference(policy = DYNAMIC)
    private final Set<Authenticator> authenticators = new CopyOnWriteArraySet<>();

    /**
     * WebSocket sessions tracker
     */
    private WebSocketCreator sessionPool;

    private Config config;

    /**
     * Flag to indicate if the servlet was initialized
     */
    private final AtomicBoolean initCalled = new AtomicBoolean(false);

    /**
     * Lets queries wait for initialization
     */
    private final CountDownLatch initComplete = new CountDownLatch(1);

    @Activate
    void activate(final Config config) {
        this.config = config;
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
        // Normal service resumes - note that we use the Jetty Context to avoid problems
        // later, but we must not treat this like a normal servlet context as it is
        // potentially shared between different disjoint servlet contexts.
        super.service(new HttpServletRequestWrapper((HttpServletRequest) req) {
            @Override
            public ServletContext getServletContext() {
                return ContextHandler.getCurrentContext();
            }
        }, res);
    }

    @Override
    protected void configure(final JettyWebSocketServletFactory factory) {
        factory.setCreator(sessionPool);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null) {
            if (!config.allow_anonymous()) {
                unauthorizedResponse(resp);
            } else {
                req.setAttribute(SENSINACT_USER_INFO, UserInfo.ANONYMOUS);
                chain.doFilter(request, response);
            }
        } else {
            String[] headerChunks = authHeader.split(" ", 2);

            if (headerChunks.length != 2) {
                resp.sendError(400);
            } else {

                Scheme authScheme;
                String userid;
                String credential;
                if ("Bearer".equals(headerChunks[0])) {
                    authScheme = TOKEN;
                    userid = null;
                    credential = headerChunks[1];
                } else if ("Basic".equals(headerChunks[0])) {
                    authScheme = USER_PASSWORD;
                    String cred = new String(Base64.getMimeDecoder().decode(headerChunks[1]), UTF_8);
                    String[] credChunks = cred.split(":", 2);
                    userid = credChunks[0];
                    credential = credChunks[1];
                } else {
                    authScheme = null;
                    userid = null;
                    credential = null;
                }

                Optional<UserInfo> user = Set.copyOf(authenticators).stream().filter(a -> a.getScheme() == authScheme)
                        .map(a -> tryAuth(a, userid, credential)).filter(u -> u != null).findFirst();

                if (user.isEmpty()) {
                    unauthorizedResponse(resp);
                } else {
                    req.setAttribute(SENSINACT_USER_INFO, user.get());
                    chain.doFilter(request, response);
                }
            }
        }
    }

    private void unauthorizedResponse(HttpServletResponse response) throws IOException {
        Collection<Authenticator> authenticators = Set.copyOf(this.authenticators);
        if (authenticators.isEmpty()) {
            response.sendError(503);
        } else {
            response.setHeader("WWW-Authenticate", getAuthHeader(authenticators));
            response.sendError(401);
        }
    }

    private String getAuthHeader(Collection<Authenticator> authenticators) {
        return authenticators.stream()
                .map(a -> String.format("%s realm=%s", a.getScheme().getHttpScheme(), a.getRealm()))
                .collect(Collectors.joining(", "));
    }

    private UserInfo tryAuth(Authenticator a, String user, String credential) {
        UserInfo ui = null;
        try {
            ui = a.authenticate(user, credential);
        } catch (Exception e) {
            LOG.warn("Failed to authenticate user {}", user, e);
        }
        return ui;
    }
}
