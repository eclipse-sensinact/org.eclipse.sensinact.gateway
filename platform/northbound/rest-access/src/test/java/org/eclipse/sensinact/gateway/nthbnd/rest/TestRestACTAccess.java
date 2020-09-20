/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.nthbnd.rest;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRestACTAccess extends TestRestAccess {
    public TestRestACTAccess() throws Exception {
        super();
    }

    @Test
    public void testHttpACTWithoutParameters() throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/light/services/switch/resources/status/GET", null, "GET");
        //System.out.println(simulated);
        JSONObject response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/status"));
        assertTrue(response.getJSONObject("response").get("value").equals("OFF"));
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/light/services/switch/resources/turn_on/ACT", null, "POST");

        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/light/services/switch/resources/status/GET", null, "GET");
        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/status"));
        assertTrue(response.getJSONObject("response").get("value").equals("ON"));
    }

    @Test
    public void testSimplifiedHttpACTWithoutParameters() throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/light/switch/status/GET", null, "GET");

        //System.out.println(simulated);
        JSONObject response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/status"));
        assertTrue(response.getJSONObject("response").get("value").equals("OFF"));
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/light/switch/turn_on/ACT", null, "POST");
        System.out.println(simulated);
        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/light/switch/status/GET", null, "GET");
        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/status"));
        assertTrue(response.getJSONObject("response").get("value").equals("ON"));
    }

    @Test
    public void testHttpACTWithParameters() throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/light/services/switch/resources/brightness/GET", null, "GET");

        JSONObject response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/brightness"));
        assertTrue(response.getJSONObject("response").get("value").equals(10));
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/light/services/switch/resources/dim/ACT", "{\"parameters\":[{\"name\": \"brightness\",\"value\": 5,\"type\": \"int\"}]}", "POST");
        System.out.println(simulated);
        
        response = new JSONObject(simulated);
        System.out.println(response.toString());

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/dim"));
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/light/services/switch/resources/brightness/GET", null, "GET");
        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/brightness"));
        System.out.println(response.toString());
        assertEquals(5,response.getJSONObject("response").getInt("value"));
    }

    @Test
    public void testWsACTWithoutParameters() throws Exception {
        JSONObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/light/services/switch/resources/status/GET", null);

        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/status"));
        assertTrue(response.getJSONObject("response").get("value").equals("OFF"));
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/light/services/switch/resources/turn_on/ACT", null);

        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/light/services/switch/resources/status/GET", null);
        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/status"));
        assertTrue(response.getJSONObject("response").get("value").equals("ON"));
    }

    @Test
    public void testWsACTWithParameters() throws Exception {
        JSONObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();


        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/light/services/switch/resources/brightness/GET", null);
        //System.out.println(simulated);
        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/brightness"));
        assertTrue(response.getJSONObject("response").get("value").equals(10));
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/light/services/switch/resources/dim/ACT", "[{\"name\": \"brightness\",\"value\": 5,\"type\": \"int\"}]");

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/dim"));
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/light/services/switch/resources/brightness/GET", null);
        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/light/switch/brightness"));
        System.out.println(response.toString());
        assertEquals(5,response.getJSONObject("response").getInt("value"));
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
