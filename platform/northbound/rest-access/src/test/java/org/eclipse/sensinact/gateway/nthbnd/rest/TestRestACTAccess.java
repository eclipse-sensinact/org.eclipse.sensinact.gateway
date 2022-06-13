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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;

import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

@ExtendWith(BundleContextExtension.class)
public class TestRestACTAccess {

	private final JsonProvider provider = JsonProviderFactory.getProvider();
	
	@BeforeEach
	public void before(@InjectBundleContext BundleContext context) {
		Mediator mediator = new Mediator(context);
		String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/light/services/switch/resources/turn_off/ACT", null, "POST");
		JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
		assertEquals(200, response.getInt("statusCode"));
		
		simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/light/services/switch/resources/dim/ACT", "{\"parameters\":[{\"name\": \"brightness\",\"value\": 10,\"type\": \"int\"}]}", "POST");
        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
	}
	
    @Test
    public void testHttpACTWithoutParameters(@InjectBundleContext BundleContext context) throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/light/services/switch/resources/status/GET", null, "GET");
        //System.out.println(simulated);
        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertTrue(response.getString("uri").equals("/light/switch/status"));
        assertTrue(response.getJsonObject("response").getString("value").equals("OFF"));
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/light/services/switch/resources/turn_on/ACT", null, "POST");

        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/light/services/switch/resources/status/GET", null, "GET");
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertTrue(response.getString("uri").equals("/light/switch/status"));
        assertTrue(response.getJsonObject("response").getString("value").equals("ON"));
    }

    @Test
    public void testSimplifiedHttpACTWithoutParameters(@InjectBundleContext BundleContext context) throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/light/switch/status/GET", null, "GET");

        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/light/switch/status", response.getString("uri"));
        assertEquals("OFF", response.getJsonObject("response").getString("value"));
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/light/switch/turn_on/ACT", null, "POST");
        System.out.println(simulated);
        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/light/switch/status/GET", null, "GET");
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/light/switch/status", response.getString("uri"));
        assertEquals("ON", response.getJsonObject("response").getString("value"));
    }

    @Test
    public void testHttpACTWithParameters(@InjectBundleContext BundleContext context) throws Exception {
        Mediator mediator = new Mediator(context);

        String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers", null, "GET");
        System.out.println(simulated);
        
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/light/services/switch/resources/brightness/GET", null, "GET");

        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertTrue(response.getString("uri").equals("/light/switch/brightness"));
        assertEquals(10, response.getJsonObject("response").getInt("value"));
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/light/services/switch/resources/dim/ACT", "{\"parameters\":[{\"name\": \"brightness\",\"value\": 5,\"type\": \"int\"}]}", "POST");
        System.out.println(simulated);
        
        response = provider.createReader(new StringReader(simulated)).readObject();
        System.out.println(response.toString());

        assertEquals(200, response.getInt("statusCode"));
        assertTrue(response.getString("uri").equals("/light/switch/dim"));
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/light/services/switch/resources/brightness/GET", null, "GET");
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertTrue(response.getString("uri").equals("/light/switch/brightness"));
        System.out.println(response.toString());
        assertEquals(5,response.getJsonObject("response").getInt("value"));
    }

    @Test
    public void testWsACTWithoutParameters(@InjectBundleContext BundleContext context) throws Exception {
        JsonObject response;

        WsServiceTestClient client = new WsServiceTestClient();
        
        new Thread(client).start();

        String simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/light/services/switch/resources/status/GET", null);

        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/light/switch/status", response.getString("uri"));
        assertEquals("OFF", response.getJsonObject("response").getString("value"));

        simulated = null;
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/light/services/switch/resources/turn_on/ACT", null);
        
        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));

        simulated = null;
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/light/services/switch/resources/status/GET", null);
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/light/switch/status", response.getString("uri"));
        assertEquals("ON", response.getJsonObject("response").getString("value"));
    }

    @Test
    public void testWsACTWithParameters() throws Exception {
        WsServiceTestClient client = new WsServiceTestClient();
        new Thread(client).start();

        String simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/light/services/switch/resources/brightness/GET", null);        
        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/light/switch/brightness", response.getString("uri")); 
        assertEquals(10, response.getJsonObject("response").getInt("value"));
        simulated = null;
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/light/services/switch/resources/dim/ACT", "[{\"name\": \"brightness\",\"value\": 5,\"type\": \"int\"}]");
        System.out.println(simulated);
        
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/light/switch/dim", response.getString("uri"));
        simulated = null;
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/light/services/switch/resources/brightness/GET", null);
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/light/switch/brightness", response.getString("uri"));
        System.out.println(response.toString());
        assertEquals(5,response.getJsonObject("response").getInt("value"));
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
