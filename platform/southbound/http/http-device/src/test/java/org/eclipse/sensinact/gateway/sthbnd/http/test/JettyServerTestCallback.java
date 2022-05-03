/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.util.IOUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import java.util.concurrent.atomic.AtomicBoolean;

class JettyServerTestCallback {
    private enum PATH {
        get, services, json1, json2, json3;
    }

    private JSONObject remoteEntity;
	private Optional<CountDownLatch> latch = Optional.empty();

    @doPost
    public void callbackPost(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        try {
            byte[] content = IOUtils.read(request.getInputStream());
            JSONObject message = new JSONObject(new String(content));
            this.remoteEntity.put("data", message.get("value"));
            response.setStatus(200);
        } catch (IOException e) {
            response.setStatus(520);
        }
    }

    @doGet
    public void callbackGet(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        String uri = request.getRequestURI();
        String[] uriElements = UriUtils.getUriElements(uri);
        try {
            PATH path = PATH.valueOf(uriElements[uriElements.length - 1]);
            switch (path) {
                case get:
                	System.out.println("Call get");
                    response.setContentType("application/json");
                    response.setContentLength(this.remoteEntity.toString().length());

                    if (response.getOutputStream().isReady()) {
                        response.getOutputStream().write(this.remoteEntity.toString().getBytes());
                    }
                    response.setStatus(200);
                    System.err.println("Returning for get: " + this.remoteEntity.toString());
                    latch.ifPresent(CountDownLatch::countDown);
                    break;
                case services:
                	System.out.println("Call services");
                    JSONObject object = new JSONObject();
                    object.put("serviceProviderId", uriElements[1]);

                    JSONArray services = new JSONArray();
                    services.put("service1").put("service2").put("service3");
                    object.put("services", services);
                    try {
                        response.setContentType("application/json");
                        response.setContentLength(object.toString().length());
                        if (response.getOutputStream().isReady()) 
                            response.getOutputStream().write(object.toString().getBytes());
                        response.setStatus(200);
                    } catch (IOException e) {
                        response.setStatus(520);
                    }
                    break;
                case json1:
                	System.out.println("Call json1");
                    response.setContentType("application/json");
                    object = new JSONObject();
                    object.put("serviceProviderId", this.remoteEntity.getString("serviceProviderId"));
                    response.setContentLength(object.toString().length());

                    if (response.getOutputStream().isReady()) {
                        response.getOutputStream().write(object.toString().getBytes());
                    }
                    response.setStatus(200);
                    break;
                case json2:
                	System.out.println("Call json2");
                    response.setContentType("text/plain");
                    response.setContentLength(this.remoteEntity.getString("serviceId").length());

                    if (response.getOutputStream().isReady()) {
                        response.getOutputStream().write(this.remoteEntity.getString("serviceId").getBytes());
                    }
                    response.setStatus(200);
                    break;
                case json3:
                	System.out.println("Call json3");
                    response.setContentType("application/json");
                    JSONArray array = new JSONArray();
                    array.put(this.remoteEntity.getString("resourceId"));
                    array.put(this.remoteEntity.get("data"));
                    response.setContentLength(array.toString().length());

                    if (response.getOutputStream().isReady()) {
                        response.getOutputStream().write(array.toString().getBytes());
                    }
                    response.setStatus(200);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            response.setStatus(520);
        }
    }
    
    public void setCountDownLatch(CountDownLatch latch) {
		this.latch = Optional.ofNullable(latch);
    }

    public void setRemoteEntity(JSONObject remoteEntity) {
    	System.err.println("Setting entity for: " + remoteEntity.getString("serviceProviderId"));
        this.remoteEntity = remoteEntity;
    }
}