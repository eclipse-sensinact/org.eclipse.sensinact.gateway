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
    
    @Override
    public NorthboundMediator getMediator() {
        return this.mediator;
    }

    @Override
    public String getRequestIdProperty() {
       return "rid";
    }

	@Override
	public String getRequestId() {
		return (String) this.request.opt(getRequestIdProperty());
	}

    @Override
    public String getRequestURI() {
        String uri = request.optString("uri");
        String[] uriElements = uri.split("\\?");
        return uriElements[0];
    }

    @Override
    public Map<QueryKey, List<String>> getQueryMap() {
        String uri = request.optString("uri");
        String[] uriElements = uri.split("\\?");
        if (uriElements.length == 2) {
            try {
                return NorthboundRequest.processRequestQuery(uriElements[1]);
            } catch (UnsupportedEncodingException e) {
                this.mediator.error(e);
            }
        }
        return Collections.<QueryKey, List<String>>emptyMap();
    }

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

    @Override
    public Authentication<?> getAuthentication() {
        return null;
    }

    @Override
    public NorthboundRecipient createRecipient(List<Parameter> parameters) {
        return new WebSocketRecipient(mediator, wsConnection);
    }
}
