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


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;

import java.io.IOException;

/**
 * CORS Filter
 */
public class CorsFilter implements Filter 
{

    private static final String OPTIONS = "OPTIONS".intern();

    private NorthboundMediator mediator;

    /**
     * Constructor
     */
    public CorsFilter(NorthboundMediator mediator) {
        this.mediator = mediator;
    }
    
    /** 
     * @inheritDoc
     *
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        if(mediator.isDebugLoggable()) {
            mediator.info("Init with config [" + config + "]");
        }
    }

    /**
     * @inheritDoc
     *
     * @see Filter#
     * doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException 
    {
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", "*");
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Methods", "GET,POST");
        ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers",
                ((HttpServletRequest) request).getHeader("Access-Control-Request-Headers"));

        if(OPTIONS.equals(((HttpServletRequest) request).getMethod().intern()))
        {
            response.getWriter().flush();
            
        } else
        {
            chain.doFilter(request, response);
        }
    }

    /**
     * @inheritDoc
     *
     * @see Filter#destroy()
     */
    public void destroy() {
        if(mediator.isDebugLoggable()) {
            mediator.info("Destroyed filter");
        }
    }
}
