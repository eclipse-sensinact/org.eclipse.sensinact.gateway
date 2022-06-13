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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.nthbnd.rest.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestRestSETAccess{

	 private static final String NULL_LOCATION = "{\"type\":\"FeatureCollection\",\"features\":"
	 + "[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"coordinates\":[0.0,0.0],\"type\":\"Point\"}}]}";
	 
	 private String location; 
	 
	 private JsonProvider provider = JsonProviderFactory.getProvider();
	 
	@BeforeEach
	public void before(@InjectBundleContext BundleContext context) {
		Mediator mediator = new Mediator(context);
		location = ModelInstance.defaultLocation(mediator);
		String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL 
				+ "/providers/slider/services/admin/resources/location/SET", "{\"parameters\":"
				+ "[{\"name\": \"location\",\"value\": \""+location.replace("\"", "\\\"")+"\",\"type\": \"string\"}]}", "POST");
        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
	}
	
    @Test
    public void testHttpAccessMethodSET(@InjectBundleContext BundleContext context) throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, 
        		TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/GET",
        		null, "GET");

       
        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/admin/location", response.getString("uri"));
        assertEquals(location, response.getJsonObject("response").getString("value"));
        simulated = HttpServiceTestClient.newRequest(mediator, 
        		TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/SET", 
        		"{\"parameters\":[{\"name\": \"location\",\"value\": \""+NULL_LOCATION.replace("\"", "\\\"")+"\",\"type\": \"string\"}]}", 
        		"POST");

        response = provider.createReader(new StringReader(simulated)).readObject();

        simulated = HttpServiceTestClient.newRequest(mediator, 
        		TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/GET",
        		null, "GET");

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/admin/location", response.getString("uri"));
        assertEquals(NULL_LOCATION,response.getJsonObject("response").getString("value"));
        
        HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL 
				+ "/providers/slider/services/admin/resources/location/SET", "{\"parameters\":"
				+ "[{\"name\": \"location\",\"value\": \""+location.replace("\"", "\\\"")+"\",\"type\": \"string\"}]}", "POST");
    }

    @Test
    public void testWsAccessMethodSET(@InjectBundleContext BundleContext context) throws Exception {
        Mediator mediator = new Mediator(context);
        JsonObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, 
        		TestRestAccess.WS_ROOTURL + "/providers/slider/services/admin/resources/location/GET", 
        		null);

        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/admin/location", response.getString("uri"));
        assertEquals(location, response.getJsonObject("response").getString("value"));
        simulated = this.synchronizedRequest(client, 
        		TestRestAccess.WS_ROOTURL + "/providers/slider/services/admin/resources/location/SET",
        		"[{\"name\": \"location\",\"value\": \""+NULL_LOCATION.replace("\"", "\\\"")+"\",\"type\": \"string\"}]");

        response = provider.createReader(new StringReader(simulated)).readObject();

        simulated = this.synchronizedRequest(client, 
        		TestRestAccess.WS_ROOTURL + "/providers/slider/services/admin/resources/location/GET", 
        		null);

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/admin/location", response.getString("uri"));
        assertEquals(NULL_LOCATION,response.getJsonObject("response").getString("value"));
        
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
