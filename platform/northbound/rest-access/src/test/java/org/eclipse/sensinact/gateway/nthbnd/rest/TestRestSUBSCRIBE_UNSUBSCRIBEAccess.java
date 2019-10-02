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

import junit.framework.Assert;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.rest.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.server.JettyTestServer;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


import static org.junit.Assert.assertTrue;

public class TestRestSUBSCRIBE_UNSUBSCRIBEAccess extends TestRestAccess {

    private static JettyTestServer server = null;

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void initialization() throws Exception {
        if (server != null) {
            if (server.isStarted()) {
                server.stop();
                server.join();
            }
            server = null;
        }
        server = new JettyTestServer(54461);
        new Thread(server).start();
        server.join();
    }

    @AfterClass
    public static void finalization() throws Exception {
        server.stop();
        server.join();
    }

    public TestRestSUBSCRIBE_UNSUBSCRIBEAccess() throws Exception {
        super();
    }

    @Test
    public void testHttpAccessMethodSUBSCRIBE_UNSUBSCIBE() throws Exception {
        Mediator mediator = new Mediator(context);
        JSONObject response;
        String simulated;
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + 
        	"/providers/slider/services/cursor/resources/position/SUBSCRIBE",
        	"{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://localhost:54461\"}]}", "POST");
        //System.out.println(simulated);

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));

        String subscriptionId = response.getJSONObject("response").getString("subscriptionId");
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();

        server.setAvailable(false);
        Thread.sleep(5000);
        slider.move(2);
        String message = waitForAvailableMessage(10000);
        Assert.assertNotNull(message);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(2, response.getJSONObject("notification").getInt("value"));
        
        server.setAvailable(false);   
        Thread.sleep(5000);
        slider.move(0);
        message = waitForAvailableMessage(10000);
        Assert.assertNotNull(message);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(0, response.getJSONObject("notification").getInt("value"));
        
        server.setAvailable(false); 
        Thread.sleep(5000);
        slider.move(100);
        message = waitForAvailableMessage(10000);
        Assert.assertNotNull(message);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(100, response.getJSONObject("notification").getInt("value"));

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "{\"parameters\" : [{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]}", "POST");

        //System.out.println(simulated);
        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        server.setAvailable(false);
        Thread.sleep(5000);
        slider.move(150);
        message = waitForAvailableMessage(10000);
        Assert.assertNull(message);
    }

    @Test
    public void testHttpAccessMethodConditionalSUBSCRIBE_UNSUBSCIBE() throws Exception {
        Mediator mediator = new Mediator(context);
        JSONObject response;
        String simulated;

        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", "{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://127.0.0.1:54461\"}," + "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" + "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]}", "POST");

        //System.out.println(simulated);

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));

        String subscriptionId = response.getJSONObject("response").getString("subscriptionId");
        server.setAvailable(false);
        slider.move(2);
        String message = waitForAvailableMessage(10000);

        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(2, response.getJSONObject("notification").getInt("value"));
        server.setAvailable(false);
        slider.move(200);
        message = waitForAvailableMessage(10000);
        Assert.assertNull(message);

        server.setAvailable(false);
        slider.move(199);
        message = waitForAvailableMessage(10000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(199, response.getJSONObject("notification").getInt("value"));
        server.setAvailable(false);
        slider.move(201);
        message = waitForAvailableMessage(10000);
        Assert.assertNull(message);
        server.setAvailable(false);
        slider.move(185);
        message = waitForAvailableMessage(10000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(185, response.getJSONObject("notification").getInt("value"));

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "{\"parameters\" : [{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]}", "POST");

        //System.out.println(simulated);
        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        server.setAvailable(false);
        slider.move(150);
        message = waitForAvailableMessage(10000);
        Assert.assertNull(message);
    }

    @Ignore
    @Test
    public void testWsAccessMethodSUBSCRIBE_UNSUBSCIBE() throws Exception {
    	Thread.sleep(5000);
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();
        JSONObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", null);

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));

        String subscriptionId = response.getJSONObject("response").getString("subscriptionId");
        client.setAvailable(false);
        slider.move(2);
        String message = waitForAvailableMessage(client, 1000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);

        Assert.assertEquals(2, response.getJSONObject("notification").getInt("value"));
        client.setAvailable(false);
        slider.move(0);
        message = waitForAvailableMessage(client, 1000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(0, response.getJSONObject("notification").getInt("value"));
        client.setAvailable(false);
        slider.move(125);
        message = waitForAvailableMessage(client, 1000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(125, response.getJSONObject("notification").getInt("value"));

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "[{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]");

        //System.out.println(simulated);
        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        client.setAvailable(false);
        slider.move(150);
        message = waitForAvailableMessage(client, 1000);
        Assert.assertNull(message);
    }

    @Ignore
    @Test
    public void testWsAccessMethodConditionalSUBSCRIBE_UNSUBSCIBE() throws Exception {
    	Thread.sleep(5000);
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();
        JSONObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", "[{\"name\":\"conditions\",\"type\":\"array\",\"value\":" + "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]");

        System.out.println(simulated);

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));

        String subscriptionId = response.getJSONObject("response").getString("subscriptionId");
        client.setAvailable(false);
        slider.move(2);
        String message = waitForAvailableMessage(client, 1000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(2, response.getJSONObject("notification").getInt("value"));
        client.setAvailable(false);
        slider.move(200);
        message = waitForAvailableMessage(client, 1000);
        Assert.assertNull(message);

        client.setAvailable(false);
        slider.move(199);
        message = waitForAvailableMessage(client, 1000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(199, response.getJSONObject("notification").getInt("value"));
        client.setAvailable(false);
        slider.move(201);
        message = waitForAvailableMessage(client, 1000);
        Assert.assertNull(message);
        client.setAvailable(false);
        slider.move(185);
        message = waitForAvailableMessage(client, 1000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(185, response.getJSONObject("notification").getInt("value"));

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "[{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]");

        //System.out.println(simulated);
        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        client.setAvailable(false);
        slider.move(150);
        message = waitForAvailableMessage(client, 1000);
        Assert.assertNull(message);
    }

    private String waitForAvailableMessage(long delay) {
        String message = null;
        long wait = delay;

        while (!server.isAvailable() && wait > 0) {
            try {
                Thread.sleep(100);
                wait -= 100;
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        if (server.isAvailable()) {
            message = server.getResponseMessage();
        }
        return message;
    }

    private String synchronizedRequest(WsServiceTestClient client, String url, String content) {
        client.newRequest(url, content);
        String message = waitForAvailableMessage(client, 1000);
        return message;
    }

    private String waitForAvailableMessage(WsServiceTestClient client, long delay) {
        String message = null;
        long wait = delay;

        while (!client.isAvailable() && wait > 0) {
            try {
                Thread.sleep(100);
                wait -= 100;
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        if (client.isAvailable()) {
            message = client.getResponseMessage();
        }
        return message;
    }
}
