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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is a wrapper for simple callback subscription
 */
public class HttpRecipient extends NorthboundRecipient {
    private String urlCallback;
    private ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> connectionBuilder;

    public HttpRecipient(Mediator mediator, String callback) {
        super(mediator);
        this.urlCallback = callback;
        this.connectionBuilder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
        this.connectionBuilder.setContentType("application/json");
        this.connectionBuilder.setHttpMethod(ConnectionConfiguration.POST);
    }

    public HttpRecipient(Mediator mediator, JSONObject object) {
        super(mediator);
        try {
        	this.urlCallback = object.getString("callback");
        } catch(JSONException e) {
        	throw new NullPointerException("callback is required");
        }
        this.connectionBuilder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
        this.connectionBuilder.setContentType("application/json");
        this.connectionBuilder.setHttpMethod(ConnectionConfiguration.POST);
    }

    @Override
    public void callback(String callbackId, SnaMessage<?>[] messages) throws IOException {
        int index = 0;
        int length = messages == null ? 0 : messages.length;

        StringBuilder builder = new StringBuilder();
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append("\"type\":\"CALLBACK\",");
        builder.append("\"callbackId\":");
        if (callbackId == null) {
            builder.append("null");
        } else {
            builder.append("\"");
            builder.append(callbackId);
            builder.append("\"");
        }
        builder.append(",\"messages\":");
        builder.append(JSONUtils.OPEN_BRACKET);
        for (; index < length; index++) {
            builder.append(index == 0 ? "" : ",");
            builder.append(messages[index].getJSON());
        }
        builder.append(JSONUtils.CLOSE_BRACKET);
        builder.append(JSONUtils.CLOSE_BRACE);

        String uri = null;
        if (callbackId != null) {
            String separator = urlCallback.endsWith("/") ? "" : "/";
            uri = new StringBuilder().append(urlCallback).append(separator).append(callbackId).toString();
        } else {
            uri = urlCallback;
        }
        this.connectionBuilder.setUri(uri);
        this.connectionBuilder.setContent(builder.toString());
        this.connectionBuilder.setHttpMethod("POST");
        this.connectionBuilder.setContentType("application/json");

        SimpleRequest request = new SimpleRequest(HttpRecipient.this.connectionBuilder);
        request.send();
    }

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
	 */
	@Override
	public String getJSON() {
		return new StringBuilder(
			).append("{\"callback\":\""
			).append(this.urlCallback
			).append("\"}").toString();
	}
}
