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
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.SessionToken;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRestAccessRequest extends HttpServletRequestWrapper implements NorthboundRequestWrapper {
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpRestAccessRequest.class);
    private NorthboundMediator mediator;
    private Map<QueryKey, List<String>> queryMap;
    private Authentication<?> authentication;
    private String content;

    /**
     * Constructor
     *
     * @param mediator the {@link NorthboundMediator} allowing to interact
     *                 with the OSGi host environment
     * @param request  the {@link HttpServletRequest} to be wrapped by
     *                 the HttpRestAccessRequest to be instantiated
     * @throws InvalidCredentialException
     */
    public HttpRestAccessRequest(NorthboundMediator mediator, HttpServletRequest request) throws InvalidCredentialException {
        super(request);
        this.mediator = mediator;
    }

    @Override
    public NorthboundMediator getMediator() {
        return this.mediator;
    }
    
    @Override
    public Map<QueryKey, List<String>> getQueryMap() {
        if (this.queryMap == null) {
            try {
                this.queryMap = NorthboundRequest.processRequestQuery(super.getQueryString());
            } catch (UnsupportedEncodingException e) {
                LOG.error(e.getMessage(), e);
                this.queryMap = Collections.<QueryKey, List<String>>emptyMap();
            }
        }
        return queryMap;
    }

    @Override
    public String getContent() {
        if (this.content == null) {
            try {
                ServletInputStream input = super.getInputStream();
                byte[] stream = IOUtils.read(input, super.getContentLength(), true);
                this.content = new String(stream);

            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return this.content;
    }

    /**
     * Define the {@link Authentication} attached to this
     * HttpRestAccessRequest
     *
     * @param authentication the {@link Authentication} of this
     *                       HttpRestAccessRequest
     */
    void setAuthentication(Authentication<?> authentication) {
        this.authentication = authentication;
    }

    @Override
    public Authentication<?> getAuthentication() {
        if (this.authentication == null) {
            String tokenHeader = super.getHeader("X-Auth-Token");
            String authorizationHeader = super.getHeader("Authorization");

            if (tokenHeader != null) {
                this.authentication = new SessionToken(tokenHeader);

            } else if (authorizationHeader != null && super.getAttribute("token")==null) {
                this.authentication = new Credentials(authorizationHeader);
            }
        }
        return this.authentication;
    }

    @Override
    public String getRequestIdProperty() {
       return "rid";
    }

	@Override
	public String getRequestId() {
		return super.getHeader(getRequestIdProperty());
	}

    @Override
    public NorthboundRecipient createRecipient(List<Parameter> parameters) {
        NorthboundRecipient recipient = null;

        int index = 0;
        int length = parameters == null ? 0 : parameters.size();
        String callback = null;

        for (; index < length; index++) {
            String name = parameters.get(index).getName();
            if ("callback".equals(name)) {
                callback = (String) parameters.get(index).getValue();
                break;
            }
        }
        if (callback != null) 
            recipient = new HttpRecipient(mediator, callback);
        return recipient;
    }

    public void destroy() {
        this.queryMap = null;
        this.authentication = null;
        this.content = null;
    }
}
