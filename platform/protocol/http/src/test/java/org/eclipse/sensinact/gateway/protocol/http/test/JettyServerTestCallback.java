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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonObjectBuilder;

class JettyServerTestCallback {

    @doPost
    public void callbackPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObjectBuilder requestDescription = JsonProviderFactory.getProvider().createObjectBuilder();
        requestDescription.add("method", "POST");
        
        if (request.getQueryString() != null) 
            requestDescription.add("url", request.getRequestURI() + "?" + request.getQueryString());
        else 
            requestDescription.add("url", request.getRequestURI());

        requestDescription.add("content-type", request.getContentType());
        requestDescription.add("content-length", request.getContentLength());

        try {
            requestDescription.add("message", JsonProviderFactory.getProvider().createReader(request.getInputStream()).readValue());

            response.setContentType("application/json");
            response.getWriter().println(requestDescription.build().toString());
            response.setStatus(200);

        } catch (IOException e) {
            response.getWriter().println(e.getMessage());
            response.setStatus(520);
        }
    }

    @doGet
    public void callbackGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObjectBuilder requestDescription = JsonProviderFactory.getProvider().createObjectBuilder();
        requestDescription.add("method", "GET");
        if (request.getQueryString() != null) 
            requestDescription.add("url", request.getRequestURI() + "?" + request.getQueryString());
        else 
            requestDescription.add("url", request.getRequestURI());
        response.setContentType("application/json");
        try {
            response.getWriter().println(requestDescription.build().toString());
            response.setStatus(200);

        } catch (IOException e) {
            response.getWriter().println(e.getMessage());
            response.setStatus(520);
        }
    }
}