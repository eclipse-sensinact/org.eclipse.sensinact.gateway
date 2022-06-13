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
import org.eclipse.sensinact.gateway.nthbnd.rest.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.server.JettyTestServer;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestRestSUBSCRIBE_UNSUBSCRIBEAccess{

    private static JettyTestServer server = null;

    private JsonProvider provider = JsonProviderFactory.getProvider();
    
    /**
     * @throws Exception
     */
    @BeforeAll
    public static void initialization() throws Exception {
        if (server != null) {
            if (server.isStarted()) {
                server.stop();
                server.join();
            }
            server = null;
        }
        server = new JettyTestServer(8898);
        new Thread(server).start();
        server.join();
    }

    @AfterAll
    public static void finalization() throws Exception {
        server.stop();
        server.join();
    }

    public TestRestSUBSCRIBE_UNSUBSCRIBEAccess() throws Exception {
        super();
    }

    @Test
    public void testHttpAccessMethodSUBSCRIBE_UNSUBSCIBE(@InjectBundleContext BundleContext context, @InjectService(timeout = 500) SliderSetterItf slider) throws Exception {
        Mediator mediator = new Mediator(context);
        JsonObject response;
        String simulated;
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + 
        	"/providers/slider/services/cursor/resources/position/SUBSCRIBE",
        	"{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://localhost:8898\"}]}", "POST");
        System.out.println(simulated);

        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));

        String subscriptionId = response.getJsonObject("response").getString("subscriptionId");

        server.setAvailable(false);
        Thread.sleep(5000);
        slider.move(2);
        String message = waitForAvailableMessage(10000);
        Assertions.assertNotNull(message);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(2, response.getJsonObject("notification").getInt("value"));
        
        server.setAvailable(false);   
        Thread.sleep(5000);
        slider.move(0);
        message = waitForAvailableMessage(10000);
        System.out.println(message);
        Assertions.assertNotNull(message);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(0, response.getJsonObject("notification").getInt("value"));
        
        server.setAvailable(false); 
        Thread.sleep(5000);
        slider.move(100);
        message = waitForAvailableMessage(10000);
        Assertions.assertNotNull(message);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(100, response.getJsonObject("notification").getInt("value"));

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "{\"parameters\" : [{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]}", "POST");

        System.out.println(simulated);
        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        server.setAvailable(false);
        Thread.sleep(5000);
        slider.move(150);
        message = waitForAvailableMessage(10000);
        Assertions.assertNull(message);
    }

    @Test
    public void testHttpAccessMethodConditionalSUBSCRIBE_UNSUBSCIBE(@InjectBundleContext BundleContext context, @InjectService(timeout = 500) SliderSetterItf slider) throws Exception {
    	Mediator mediator = new Mediator(context);
        JsonObject response;
        String simulated;

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", "{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://127.0.0.1:8898\"}," + "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" + "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]}", "POST");

        //System.out.println(simulated);

        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));

        String subscriptionId = response.getJsonObject("response").getString("subscriptionId");
        server.setAvailable(false);
        slider.move(2);
        String message = waitForAvailableMessage(10000);

        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(2, response.getJsonObject("notification").getInt("value"));
        server.setAvailable(false);
        slider.move(200);
        message = waitForAvailableMessage(10000);
        Assertions.assertNull(message);

        server.setAvailable(false);
        slider.move(199);
        message = waitForAvailableMessage(10000);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(199, response.getJsonObject("notification").getInt("value"));
        server.setAvailable(false);
        slider.move(201);
        message = waitForAvailableMessage(10000);
        Assertions.assertNull(message);
        server.setAvailable(false);
        slider.move(185);
        message = waitForAvailableMessage(10000);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(185, response.getJsonObject("notification").getInt("value"));

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "{\"parameters\" : [{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]}", "POST");

        //System.out.println(simulated);
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        server.setAvailable(false);
        slider.move(150);
        message = waitForAvailableMessage(10000);
        Assertions.assertNull(message);
    }

    @Test
    public void testWsAccessMethodSUBSCRIBE_UNSUBSCIBE(@InjectService(timeout = 500) SliderSetterItf slider) throws Exception {
        JsonObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", null);

        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));

        String subscriptionId = response.getJsonObject("response").getString("subscriptionId");
        client.setAvailable(false);
        slider.move(2);
        String message = waitForAvailableMessage(client, 1000);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);

        Assertions.assertEquals(2, response.getJsonObject("notification").getInt("value"));
        client.setAvailable(false);
        slider.move(0);
        message = waitForAvailableMessage(client, 1000);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(0, response.getJsonObject("notification").getInt("value"));
        client.setAvailable(false);
        slider.move(125);
        message = waitForAvailableMessage(client, 1000);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(125, response.getJsonObject("notification").getInt("value"));

        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "[{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]");

        //System.out.println(simulated);
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        client.setAvailable(false);
        slider.move(150);
        message = waitForAvailableMessage(client, 1000);
        Assertions.assertNull(message);
    }

    @Test
    public void testWsAccessMethodConditionalSUBSCRIBE_UNSUBSCIBE(@InjectService(timeout = 500) SliderSetterItf slider) throws Exception {
        JsonObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();

        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", "[{\"name\":\"conditions\",\"type\":\"array\",\"value\":" + "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]");

        System.out.println(simulated);

        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));

        String subscriptionId = response.getJsonObject("response").getString("subscriptionId");
        client.setAvailable(false);
        slider.move(2);
        String message = waitForAvailableMessage(client, 1000);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(2, response.getJsonObject("notification").getInt("value"));
        client.setAvailable(false);
        slider.move(200);
        message = waitForAvailableMessage(client, 1000);
        Assertions.assertNull(message);

        client.setAvailable(false);
        slider.move(199);
        message = waitForAvailableMessage(client, 1000);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(199, response.getJsonObject("notification").getInt("value"));
        client.setAvailable(false);
        slider.move(201);
        message = waitForAvailableMessage(client, 1000);
        Assertions.assertNull(message);
        client.setAvailable(false);
        slider.move(185);
        message = waitForAvailableMessage(client, 1000);
        response = provider.createReader(new StringReader(message)).readObject();
        response = response.getJsonArray("messages").getJsonObject(0);
        Assertions.assertEquals(185, response.getJsonObject("notification").getInt("value"));

        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/slider/services/cursor/resources/position/UNSUBSCRIBE", "[{\"name\":\"subscriptionId\", \"type\":\"string\", \"value\":\"" + subscriptionId + "\"}]");

        //System.out.println(simulated);
        response = provider.createReader(new StringReader(simulated)).readObject();

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        client.setAvailable(false);
        slider.move(150);
        message = waitForAvailableMessage(client, 1000);
        Assertions.assertNull(message);
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
        String message = waitForAvailableMessage(client, 10000);
        return message;
    }

    private String waitForAvailableMessage(WsServiceTestClient client, long delay) {
        String message = null;
        long wait = delay;
        while (!client.isAvailable() && wait > 0) {
            wait -= 100;
            try {
                Thread.sleep(100);
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
