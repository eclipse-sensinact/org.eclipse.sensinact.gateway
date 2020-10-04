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
package org.eclipse.sensinact.gateway.nthbnd.rest;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestRestSETAccess extends TestRestAccess {
    public TestRestSETAccess() throws Exception {
        super();
    }

    @Test
    public void testHttpAccessMethodSET() throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/GET", null, "GET");

        JSONObject response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/admin/location"));
        assertTrue(response.getJSONObject("response").get("value").equals("45.2:5.7"));
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/SET", "{\"parameters\":[{\"name\": \"location\",\"value\": \"0.0,0.0\",\"type\": \"string\"}]}", "POST");

        response = new JSONObject(simulated);

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/GET", null, "GET");

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/admin/location"));
        assertTrue(response.getJSONObject("response").get("value").equals("0.0,0.0"));
    }

    @Test
    public void testWsAccessMethodSET() throws Exception {
        JSONObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/admin/resources/location/GET", null);

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/admin/location"));
        assertTrue(response.getJSONObject("response").get("value").equals("45.2:5.7"));
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/admin/resources/location/SET", "[{\"name\": \"location\",\"value\": \"0.0,0.0\",\"type\": \"string\"}]");

        response = new JSONObject(simulated);

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/admin/resources/location/GET", null);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/admin/location"));
        assertTrue(response.getJSONObject("response").get("value").equals("0.0,0.0"));
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
