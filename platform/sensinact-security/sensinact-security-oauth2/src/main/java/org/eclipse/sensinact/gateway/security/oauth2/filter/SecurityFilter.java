/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.security.oauth2.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.eclipse.sensinact.gateway.security.oauth2.IdentityServer;
import org.eclipse.sensinact.gateway.security.oauth2.OAuthServer;
import org.eclipse.sensinact.gateway.security.oauth2.UserInfo;

import java.io.IOException;

@WebFilter(/*asyncSupported = true*/)
public class SecurityFilter implements Filter {
	private IdentityServer idServer;
	private OAuthServer authServer;

	public SecurityFilter(IdentityServer idServer, OAuthServer authServer) {
		this.idServer = idServer;
		this.authServer = authServer;
	}

    /**
     * @inheritDoc
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
//        if (res.isCommitted()) {
//            return;
//        }
//        final AsyncContext asyncContext;
//
//        if (req.isAsyncStarted()) {
//            asyncContext = req.getAsyncContext();
//        } else {
//            asyncContext = req.startAsync();
//        }
//        asyncContext.start(new Runnable() {
//            @Override
//            public void run() {
                final HttpServletRequest request = (HttpServletRequest) req;// asyncContext.getRequest();
                final HttpServletResponse response = (HttpServletResponse) res;//asyncContext.getResponse();
                
        		HttpSession session = request.getSession();
        		String token = (String) session.getAttribute("token");
        		String authorization = request.getHeader("Authorization");
        		
        		boolean authorizationExists = authorization!=null;
        		boolean tokenExists = token!=null && token.length()>0;
        		
	        	try {
	        		if (token == null && authorizationExists) {
	        			if (authorization.matches("^Bearer .*")) {
	        				token = authorization.substring(7);
	        			}
	        			if (authorization.matches("^Basic .*")) {
	        				token = authServer.basicToken(request, authorization);
	        			}
	        			tokenExists = token!=null && token.length()>0;
	        		}
	        		if (tokenExists) {
	        			UserInfo user = authServer.check(token);
	        			if (user != null && idServer.check(user, request)) {
	        				request.setAttribute("token", token);
	        				chain.doFilter(request, response);
	        				return;
	        			} else {
	        				session.setAttribute("token", null);
	        				response.sendError(401, "unauthorized");
	        				return;
	        			}
	        		} else {
	        			UserInfo user = authServer.anonymous();	    
	        			if (user != null && idServer.check(user, request)) {
	        				chain.doFilter(request, response);
	        				return;
	        			}
	        		}
	        		if(authorizationExists) {
	        			//the user should be authenticated here
	        			//if it is not the case it means that he/her
	        			//has no right access
	        			session.setAttribute("token", null);
        				response.sendError(401, "unauthorized");
	        		} else if (authServer.handleSecurity(request, response)) {
	        			chain.doFilter(request, response);
	        		}
        		} catch(Exception e){
        			e.printStackTrace();
        		} 
//	        	finally {
//	                if (req.isAsyncStarted()) {
//	                    asyncContext.complete();
//	                }
//        		}
//            }
//        });
    }
    
	@Override
	public void destroy() {}

	@Override
	public void init(FilterConfig config) throws ServletException {}
}
