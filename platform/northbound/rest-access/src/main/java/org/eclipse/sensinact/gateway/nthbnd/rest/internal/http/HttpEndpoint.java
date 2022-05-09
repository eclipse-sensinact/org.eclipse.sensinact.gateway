/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.SessionToken;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccessConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the REST interface between each others classes
 * that perform a task and jersey
 */
@SuppressWarnings("serial")
@WebServlet(displayName="sensiNact")
public class HttpEndpoint extends HttpServlet {

	private static final Logger LOG = LoggerFactory.getLogger(HttpEndpoint.class);
    private NorthboundMediator mediator;
    private Map<String, String> anonymous;

    /**
     * Constructor
     */
    public HttpEndpoint(NorthboundMediator mediator) {
        this.mediator = mediator;
        this.anonymous = new HashMap<String, String>();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getHeader("Accept") == null || (!request.getHeader("Accept").contains(RestAccessConstants.PARTIAL_JSON_CONTENT_TYPE) && !request.getHeader("Accept").contains(RestAccessConstants.ANY_CONTENT_TYPE))) {
            response.sendError(406, "Not Acceptable");
        }
        this.doExecute(request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getContentType() == null || !request.getContentType().contains(RestAccessConstants.PARTIAL_JSON_CONTENT_TYPE)) {
            response.sendError(415, "Unsupported Media Type");
        }
        if (request.getHeader("Accept") == null || (!request.getHeader("Accept").contains(RestAccessConstants.PARTIAL_JSON_CONTENT_TYPE) && !request.getHeader("Accept").contains(RestAccessConstants.ANY_CONTENT_TYPE))) {
            response.sendError(406, "Not Acceptable");
        }
        this.doExecute(request, response);
    }

    private final void doExecute(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        Authentication<?> authentication = null;
        String client = null;

        try {
            HttpRestAccessRequest rar = new HttpRestAccessRequest(mediator, request);
            authentication = rar.getAuthentication();
            if (authentication == null) {
                String clientAddress = rar.getRemoteAddr();
                //int clientPort = rar.getRemotePort();	
                client = /*new StringBuilder().append(*/clientAddress/*).append(":").append(clientPort).toString()*/;
                String sid = HttpEndpoint.this.anonymous.get(client);
                if (sid != null) {
                    authentication = new SessionToken(sid);
                    if (HttpEndpoint.this.mediator.getNorthboundEndpoints().getEndpoint(new SessionToken(sid)) != null) {
                        rar.setAuthentication(authentication);
                    }
                }
            }
            HttpRestAccess restAccess = new HttpRestAccess(rar, new HttpServletResponseWrapper(response));
            restAccess.proceed();

        } catch (InvalidCredentialException e) {
            LOG.error(e.getMessage(), e);
            response.sendError(401, e.getMessage());

        } catch (Exception e) {
        	e.printStackTrace();
            LOG.error(e.getMessage(), e);
            response.sendError(520, "Internal server error");

        } finally {
            String token = null;
            if (authentication == null && client != null && (token = response.getHeader("X-Auth-Token")) != null) {
                HttpEndpoint.this.anonymous.put(client, token);
            }
        }
    }
}
