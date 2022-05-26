/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.test;

import org.eclipse.sensinact.gateway.protocol.http.server.Content;
import org.eclipse.sensinact.gateway.protocol.http.server.HttpResponseContent;
import org.eclipse.sensinact.gateway.protocol.http.server.RequestContent;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonObjectBuilder;

import java.io.IOException;
import java.io.StringReader;

class HttpServerTestCallback {

    @doPost
    public Content callbackPost(RequestContent request) throws IOException {
    	JsonObjectBuilder requestDescription = JsonProviderFactory.getProvider().createObjectBuilder();
        requestDescription.add("method", "POST");
        
        if (request.getQueryString() != null) 
            requestDescription.add("url", request.getRequestURI() + "?" + request.getQueryString());
        else 
            requestDescription.add("url", request.getRequestURI());
        
        requestDescription.add("content-type", request.getHeaderAsString("Content-type"));
        requestDescription.add("content-length", Integer.parseInt(request.getHeaderAsString("Content-length")));

        byte[] content = request.getContent();
        String message = new String(content);
        requestDescription.add("message", JsonProviderFactory.getProvider().createReader(new StringReader(message)).readObject());

        HttpResponseContent response = new HttpResponseContent();
        response.addHeader("Content-Type", "application/json");
        response.setContent(requestDescription.build().toString().getBytes());
        return response;
    }

    @doGet
    public Content callbackGet(RequestContent request) throws IOException {
        JsonObjectBuilder requestDescription = JsonProviderFactory.getProvider().createObjectBuilder();
        requestDescription.add("method", "GET");
        
        if (request.getQueryString() != null) 
            requestDescription.add("url", request.getRequestURI() + "?" + request.getQueryString());
        else 
            requestDescription.add("url", request.getRequestURI());
        
        HttpResponseContent response = new HttpResponseContent();
        response.addHeader("Content-Type", "application/json");
        response.setContent(requestDescription.build().toString().getBytes());
        return response;
    }
}