/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.nthbnd.rest.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestRestSETAccess{

	 private static final String NULL_LOCATION = "{\"type\":\"FeatureCollection\",\"features\":"
	 + "[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"coordinates\":[0.0,0.0],\"type\":\"Point\"}}]}";
	 
	 private String location; 
	 
	@BeforeEach
	public void before(@InjectBundleContext BundleContext context) {
		Mediator mediator = new Mediator(context);
		location = ModelInstance.defaultLocation(mediator);
		String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL 
				+ "/providers/slider/services/admin/resources/location/SET", "{\"parameters\":"
				+ "[{\"name\": \"location\",\"value\": \""+location.replace("\"", "\\\"")+"\",\"type\": \"string\"}]}", "POST");
        JSONObject response = new JSONObject(simulated);
        assertEquals(200, response.get("statusCode"));
	}
	
    @Test
    public void testHttpAccessMethodSET(@InjectBundleContext BundleContext context) throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, 
        		TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/GET",
        		null, "GET");

       
        JSONObject response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/admin/location"));
        assertEquals(location, response.getJSONObject("response").get("value"));
        simulated = HttpServiceTestClient.newRequest(mediator, 
        		TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/SET", 
        		"{\"parameters\":[{\"name\": \"location\",\"value\": \""+NULL_LOCATION.replace("\"", "\\\"")+"\",\"type\": \"string\"}]}", 
        		"POST");

        response = new JSONObject(simulated);

        simulated = HttpServiceTestClient.newRequest(mediator, 
        		TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/GET",
        		null, "GET");

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/admin/location"));
        assertEquals(NULL_LOCATION,response.getJSONObject("response").get("value"));
        
        HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL 
				+ "/providers/slider/services/admin/resources/location/SET", "{\"parameters\":"
				+ "[{\"name\": \"location\",\"value\": \""+location.replace("\"", "\\\"")+"\",\"type\": \"string\"}]}", "POST");
    }

    @Test
    public void testWsAccessMethodSET(@InjectBundleContext BundleContext context) throws Exception {
        Mediator mediator = new Mediator(context);
        JSONObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, 
        		TestRestAccess.WS_ROOTURL + "/providers/slider/services/admin/resources/location/GET", 
        		null);

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/admin/location"));
        assertEquals(location, response.getJSONObject("response").get("value"));
        simulated = this.synchronizedRequest(client, 
        		TestRestAccess.WS_ROOTURL + "/providers/slider/services/admin/resources/location/SET",
        		"[{\"name\": \"location\",\"value\": \""+NULL_LOCATION.replace("\"", "\\\"")+"\",\"type\": \"string\"}]");

        response = new JSONObject(simulated);

        simulated = this.synchronizedRequest(client, 
        		TestRestAccess.WS_ROOTURL + "/providers/slider/services/admin/resources/location/GET", 
        		null);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/admin/location"));
        assertEquals(NULL_LOCATION,response.getJSONObject("response").get("value"));
        
        HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL 
				+ "/providers/slider/services/admin/resources/location/SET", "{\"parameters\":"
				+ "[{\"name\": \"location\",\"value\": \""+location.replace("\"", "\\\"")+"\",\"type\": \"string\"}]}", "POST");
    }

    private String synchronizedRequest(WsServiceTestClient client, String url, String content) {
    	String simulated = null;
        long wait = 10000;
        client.newRequest(url, content);

        while (!client.isAvailable() && wait > 0) {
            wait-=100;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        if (client.isAvailable()) {
            simulated = client.getResponseMessage();
        }
        return simulated;
    }
}
