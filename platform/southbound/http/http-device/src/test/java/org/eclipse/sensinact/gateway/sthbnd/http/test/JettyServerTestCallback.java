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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

class JettyServerTestCallback {
    private enum PATH {
        get, services, json1, json2, json3;
    }

    private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
    
    private Map<String, Object> remoteEntity = new HashMap<>();
	private Optional<CountDownLatch> latch = Optional.empty();

    @doPost
    public void callbackPost(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            JsonObject message = mapper.readValue(request.getInputStream(), JsonObject.class);
            this.remoteEntity.put("data", message.get("value"));
            response.setStatus(200);
        } catch (IOException e) {
            response.setStatus(520);
        }
    }

    @doGet
    public void callbackGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();
        String[] uriElements = UriUtils.getUriElements(uri);
        try {
            PATH path = PATH.valueOf(uriElements[uriElements.length - 1]);
            switch (path) {
                case get:
                	System.out.println("Call get");
                    response.setContentType("application/json");

                    if (response.getOutputStream().isReady()) {
                    	mapper.writeValue(response.getOutputStream(), this.remoteEntity);
                    }
                    response.setStatus(200);
                    System.err.println("Returning for get: " + this.remoteEntity.toString());
                    latch.ifPresent(CountDownLatch::countDown);
                    break;
                case services:
                	System.out.println("Call services");
                    Map<String, Object> object = new HashMap<>();
                    object.put("serviceProviderId", uriElements[1]);

                    List<Object> services = new ArrayList<>();
                    services.add("service1");
                    services.add("service2");
                    services.add("service3");
                    object.put("services", services);
                    try {
                        response.setContentType("application/json");
                        if (response.getOutputStream().isReady()) 
                        	mapper.writeValue(response.getOutputStream(), object);
                        response.setStatus(200);
                    } catch (IOException e) {
                        response.setStatus(520);
                    }
                    break;
                case json1:
                	System.out.println("Call json1");
                    response.setContentType("application/json");
                    object = new HashMap<>();
                    object.put("serviceProviderId", this.remoteEntity.get("serviceProviderId"));

                    if (response.getOutputStream().isReady()) {
                    	mapper.writeValue(response.getOutputStream(), object);
                    }
                    response.setStatus(200);
                    break;
                case json2:
                	System.out.println("Call json2");
                    response.setContentType("text/plain");
                    response.setContentLength(this.remoteEntity.get("serviceId").toString().length());

                    if (response.getOutputStream().isReady()) {
                        response.getOutputStream().write(this.remoteEntity.get("serviceId").toString().getBytes());
                    }
                    response.setStatus(200);
                    break;
                case json3:
                	System.out.println("Call json3");
                    response.setContentType("application/json");
                    List<Object> array = new ArrayList<>();
                    array.add(this.remoteEntity.get("resourceId"));
                    array.add(this.remoteEntity.get("data"));

                    if (response.getOutputStream().isReady()) {
                    	mapper.writeValue(response.getOutputStream(), array);
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

    public void setRemoteEntity(JsonObject remoteEntity) {
    	System.err.println("Setting entity for: " + remoteEntity.getString("serviceProviderId"));
    	this.remoteEntity.clear();
    	this.remoteEntity.putAll(remoteEntity);
    }
}