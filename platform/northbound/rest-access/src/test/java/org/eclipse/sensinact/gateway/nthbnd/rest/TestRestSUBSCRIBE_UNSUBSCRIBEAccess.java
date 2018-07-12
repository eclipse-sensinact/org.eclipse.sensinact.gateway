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
import org.eclipse.sensinact.gateway.nthbnd.rest.server.JettyTestServer.doGet;
import org.eclipse.sensinact.gateway.nthbnd.rest.server.JettyTestServer.doPost;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class TestRestSUBSCRIBE_UNSUBSCRIBEAccess extends TestRestAccess {
    private static JettyTestServerCallback callback = null;
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
        server = new JettyTestServer(54460);
        new Thread(server).start();
        server.join();

        callback = new JettyTestServerCallback();
        server.registerCallback(callback);
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
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", "{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://127.0.0.1:54460\"}]}", "POST");
        //System.out.println(simulated);

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));

        String subscriptionId = response.getJSONObject("response").getString("subscriptionId");
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();

        callback.setAvailable(false);
        slider.move(2);
        String message = waitForAvailableMessage(10000);
        Assert.assertNotNull(message);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(2, response.getJSONObject("notification").getInt("value"));
        callback.setAvailable(false);
        slider.move(0);
        message = waitForAvailableMessage(10000);
        Assert.assertNotNull(message);

        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(0, response.getJSONObject("notification").getInt("value"));
        callback.setAvailable(false);
        slider.move(125);
        message = waitForAvailableMessage(10000);
        Assert.assertNotNull(message);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(125, response.getJSONObject("notification").getInt("value"));

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "{\"parameters\" : [{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]}", "POST");

        //System.out.println(simulated);
        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        callback.setAvailable(false);
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

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", "{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://127.0.0.1:54460\"}," + "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" + "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]}", "POST");

        //System.out.println(simulated);

        response = new JSONObject(simulated);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));

        String subscriptionId = response.getJSONObject("response").getString("subscriptionId");
        callback.setAvailable(false);
        slider.move(2);
        String message = waitForAvailableMessage(10000);

        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(2, response.getJSONObject("notification").getInt("value"));
        callback.setAvailable(false);
        slider.move(200);
        message = waitForAvailableMessage(10000);
        Assert.assertNull(message);

        callback.setAvailable(false);
        slider.move(199);
        message = waitForAvailableMessage(10000);
        response = new JSONObject(message);
        response = response.getJSONArray("messages").getJSONObject(0);
        Assert.assertEquals(199, response.getJSONObject("notification").getInt("value"));
        callback.setAvailable(false);
        slider.move(201);
        message = waitForAvailableMessage(10000);
        Assert.assertNull(message);
        callback.setAvailable(false);
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
        callback.setAvailable(false);
        slider.move(150);
        message = waitForAvailableMessage(10000);
        Assert.assertNull(message);
    }

    @Test
    public void testWsAccessMethodSUBSCRIBE_UNSUBSCIBE() throws Exception {
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

    @Test
    public void testWsAccessMethodConditionalSUBSCRIBE_UNSUBSCIBE() throws Exception {
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();
        JSONObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", "[{\"name\":\"conditions\",\"type\":\"array\",\"value\":" + "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]");

        //System.out.println(simulated);

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

    static class JettyTestServerCallback {
        private String message;
        private AtomicBoolean available;

        JettyTestServerCallback() {
            this.available = new AtomicBoolean(false);
        }

        @doPost
        public void callbackPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
            this.message = null;
            int length = request.getContentLength();
            if (length > -1) {
                byte[] content = IOUtils.read(request.getInputStream(), length, false);
                this.message = new String(content);
            } else {
                byte[] content = IOUtils.read(request.getInputStream(), false);
                this.message = new String(content);
            }
            this.setAvailable(true);
        }

        @doGet
        public void callbackGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            this.message = null;
            this.setAvailable(true);
        }

        private void setAvailable(boolean available) {
            this.available.set(available);
        }

        public boolean isAvailable() {
            return this.available.get();
        }

        public String getResponseMessage() {
            return this.message;
        }
    }

    private String waitForAvailableMessage(long delay) {
        String message = null;
        long wait = delay;

        while (!callback.isAvailable() && wait > 0) {
            try {
                Thread.sleep(100);
                wait -= 100;
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        if (callback.isAvailable()) {
            message = callback.getResponseMessage();
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
