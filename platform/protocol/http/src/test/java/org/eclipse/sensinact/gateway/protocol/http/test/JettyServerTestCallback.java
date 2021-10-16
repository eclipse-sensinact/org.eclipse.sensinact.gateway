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
package org.eclipse.sensinact.gateway.protocol.http.test;

import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class JettyServerTestCallback {

    @doPost
    public void callbackPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONObject requestDescription = new JSONObject();
        requestDescription.put("method", "POST");
        
        if (request.getQueryString() != null) 
            requestDescription.put("url", request.getRequestURI() + "?" + request.getQueryString());
        else 
            requestDescription.put("url", request.getRequestURI());

        requestDescription.put("content-type", request.getContentType());
        requestDescription.put("content-length", request.getContentLength());

        try {
            byte[] content = IOUtils.read(request.getInputStream());
            String message = new String(content);
            requestDescription.put("message", new JSONObject(message));

            response.setContentType("application/json");
            response.getWriter().println(requestDescription.toString());
            response.setStatus(200);

        } catch (IOException e) {
            response.getWriter().println(e.getMessage());
            response.setStatus(520);
        }
    }

    @doGet
    public void callbackGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONObject requestDescription = new JSONObject();
        requestDescription.put("method", "GET");
        if (request.getQueryString() != null) 
            requestDescription.put("url", request.getRequestURI() + "?" + request.getQueryString());
        else 
            requestDescription.put("url", request.getRequestURI());
        response.setContentType("application/json");
        try {
            response.getWriter().println(requestDescription.toString());
            response.setStatus(200);

        } catch (IOException e) {
            response.getWriter().println(e.getMessage());
            response.setStatus(520);
        }
    }
}