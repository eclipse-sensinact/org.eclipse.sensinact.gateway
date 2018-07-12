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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws;

import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WsRestAccessRequest implements NorthboundRequestWrapper {
    private NorthboundMediator mediator;
    private JSONObject request;
    private String content;
    private WebSocketConnection wsConnection;

    public WsRestAccessRequest(NorthboundMediator mediator, WebSocketConnection wsConnection, JSONObject request) {
        this.request = request;
        this.mediator = mediator;
        this.wsConnection = wsConnection;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#getMediator()
     */
    @Override
    public NorthboundMediator getMediator() {
        return this.mediator;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#getRequestID(org.eclipse.sensinact.gateway.core.method.Parameter[])
     */
    @Override
    public String getRequestID(Parameter[] parameters) {
        String rid = (String) request.opt("rid");
        if (rid == null) {
            int index = 0;
            int length = parameters == null ? 0 : parameters.length;
            for (; index < length; index++) {
                if ("rid".equals(parameters[index].getName())) {
                    rid = (String) parameters[index].getValue();
                }
            }
        }
        return rid;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#getRequestURI()
     */
    @Override
    public String getRequestURI() {
        String uri = request.optString("uri");
        String[] uriElements = uri.split("\\?");
        return uriElements[0];
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#getQueryMap()
     */
    @Override
    public Map<String, List<String>> getQueryMap() {
        String uri = request.optString("uri");
        String[] uriElements = uri.split("\\?");
        if (uriElements.length == 2) {
            try {
                return NorthboundRequest.processRequestQuery(uriElements[1]);
            } catch (UnsupportedEncodingException e) {
                this.mediator.error(e);
            }
        }
        return Collections.<String, List<String>>emptyMap();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#getContent()
     */
    @Override
    public String getContent() {
        if (this.content == null) {
            JSONArray parameters = request.optJSONArray("parameters");
            if (parameters == null) {
                parameters = new JSONArray();
            }
            this.content = parameters.toString();
        }
        return this.content;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#getAuthentication()
     */
    @Override
    public Authentication<?> getAuthentication() {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#createRecipient(org.eclipse.sensinact.gateway.core.method.Parameter[])
     */
    @Override
    public NorthboundRecipient createRecipient(Parameter[] parameters) {
        return new WebSocketRecipient(mediator, wsConnection);
    }
}
