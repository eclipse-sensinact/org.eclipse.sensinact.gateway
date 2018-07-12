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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.http;

import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccessConstants;

import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS Filter
 */
@WebFilter(asyncSupported = true)
public class CorsFilter implements Filter {
    private NorthboundMediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link NorthboundMediator} allowing to
     *                 interact with the OSGi host environment
     */
    public CorsFilter(NorthboundMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * @inheritDoc
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        if (mediator.isDebugLoggable()) {
            mediator.info("Init with config [" + config + "]");
        }
    }

    /**
     * @inheritDoc
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
        if (res.isCommitted()) {
            return;
        }
        final AsyncContext asyncContext;

        if (req.isAsyncStarted()) {
            asyncContext = req.getAsyncContext();
        } else {
            asyncContext = req.startAsync();
        }

        asyncContext.start(new Runnable() {
            @Override
            public void run() {
                final HttpServletRequest req = (HttpServletRequest) asyncContext.getRequest();
                final HttpServletResponse res = (HttpServletResponse) asyncContext.getResponse();

                if (RestAccessConstants.OPTIONS.equals(req.getMethod())) {
                    res.setHeader("Access-Control-Allow-Origin", "*");
                    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

                    StringBuilder builder = new StringBuilder();
                    String controlRequestHeaders = req.getHeader("Access-Control-Request-Headers");

                    if (controlRequestHeaders != null && controlRequestHeaders.length() > 0) {
                        builder.append(controlRequestHeaders);
                        builder.append(",");
                    }
                    builder.append("Authorization, X-Auth-Token, X-Requested-With");
                    res.setHeader("Access-Control-Allow-Headers", builder.toString());

                    try {
                        final ServletOutputStream output = res.getOutputStream();
                        output.setWriteListener(new WriteListener() {
                            @Override
                            public void onWritePossible() throws IOException {
                                try {
                                    output.println();

                                } finally {
                                    if (req.isAsyncStarted()) {
                                        asyncContext.complete();
                                    }
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                            }
                        });
                    } catch (IOException e) {
                        mediator.error(e);
                    }
                } else {
                    try {
                        chain.doFilter(req, res);

                    } catch (Exception e) {
                        mediator.error(e);
                    }
                    if (req.isAsyncStarted()) {
                        asyncContext.complete();
                    }
                }
            }
        });
    }

    /**
     * @inheritDoc
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        if (mediator.isDebugLoggable()) {
            mediator.info("Destroyed filter");
        }
    }
}
