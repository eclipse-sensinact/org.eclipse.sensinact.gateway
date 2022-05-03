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
import org.json.JSONObject;

import java.io.IOException;

class HttpServerTestCallback {

    @doPost
    public Content callbackPost(RequestContent request) throws IOException {
        JSONObject requestDescription = new JSONObject();
        requestDescription.put("method", "POST");
        
        if (request.getQueryString() != null) 
            requestDescription.put("url", request.getRequestURI() + "?" + request.getQueryString());
        else 
            requestDescription.put("url", request.getRequestURI());
        
        requestDescription.put("content-type", request.getHeaderAsString("Content-type"));
        requestDescription.put("content-length", Integer.parseInt(request.getHeaderAsString("Content-length")));

        byte[] content = request.getContent();
        String message = new String(content);
        requestDescription.put("message", new JSONObject(message));

        HttpResponseContent response = new HttpResponseContent();
        response.addHeader("Content-Type", "application/json");
        response.setContent(requestDescription.toString().getBytes());
        return response;
    }

    @doGet
    public Content callbackGet(RequestContent request) throws IOException {
        JSONObject requestDescription = new JSONObject();
        requestDescription.put("method", "GET");
        
        if (request.getQueryString() != null) 
            requestDescription.put("url", request.getRequestURI() + "?" + request.getQueryString());
        else 
            requestDescription.put("url", request.getRequestURI());
        
        HttpResponseContent response = new HttpResponseContent();
        response.addHeader("Content-Type", "application/json");
        response.setContent(requestDescription.toString().getBytes());
        return response;
    }
}