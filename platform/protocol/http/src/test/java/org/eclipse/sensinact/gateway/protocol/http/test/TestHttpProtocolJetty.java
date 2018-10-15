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
package org.eclipse.sensinact.gateway.protocol.http.test;

import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.MidClient;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.MidClientListener;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.Reusable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

public class TestHttpProtocolJetty {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    private class MidRequest extends SimpleRequest implements Reusable {
        private boolean reusable;

        /**
         * @param configuration
         */
        public MidRequest(ConnectionConfiguration<SimpleResponse, SimpleRequest> configuration, boolean reusable) {
            super(configuration);
            this.reusable = reusable;
        }

        /**
         * @inheritDoc
         * @see Reusable#isReusable()
         */
        @Override
        public boolean isReusable() {
            return this.reusable;
        }

        public MidRequest copy() {
            return new MidRequest((ConnectionConfiguration<SimpleResponse, SimpleRequest>) super.configuration, this.reusable);
        }
    }

    private class ClientListener implements MidClientListener<SimpleResponse> {
        private String message;
        private AtomicBoolean available;

        ClientListener() {
            available = new AtomicBoolean(false);
        }

        /**
         * @inheritDoc
         * @see MidClientListener#respond(java.lang.Object)
         */
        @Override
        public void respond(SimpleResponse response) {
            if (response != null) {
                this.message = new String(response.getContent());

            } else {
                this.message = "NO RESPONSE";
            }
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

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    public static int HTTP_PORT = 8895;
    public static String HTTP_ROOTURL = "http://127.0.0.1:" + HTTP_PORT;

    public static String newRequest(String configuration) throws IOException {
        SimpleResponse response;

        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>(configuration);
        SimpleRequest request = new SimpleRequest(builder);
        response = request.send();
        byte[] responseContent = response.getContent();
        String contentStr = (responseContent == null ? null : new String(responseContent));
        return contentStr;
    }

    public static String newRequest(String url, String content, String method) {
        SimpleResponse response;
        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
        builder.setUri(url);
        try {
            if (method.equals("GET")) {
                builder.setHttpMethod("GET");

            } else if (method.equals("POST")) {
                builder.setContentType("application/json");
                builder.setHttpMethod("POST");
                if (content != null && content.length() > 0) {
                    JSONObject jsonData = new JSONObject(content);
                    builder.setContent(jsonData.toString());
                }
            } else {
                return null;
            }
            builder.setAccept("application/json");
            SimpleRequest request = new SimpleRequest(builder);
            response = request.send();
            byte[] responseContent = response.getContent();
            String contentStr = (responseContent == null ? null : new String(responseContent));
            return contentStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject getExpected(URL url, String content, String method) {
        JSONObject requestDescription = new JSONObject();
        requestDescription.put("method", method);
        requestDescription.put("url", url.getPath());


        if (method.equals("POST")) {
            JSONObject message = new JSONObject(content);
            requestDescription.put("content-type", "application/json");
            requestDescription.put("content-length", content.length());
            requestDescription.put("message", message);
        }
        return requestDescription;
    }

    private static JettyTestServer server = null;

    @BeforeClass
    public static void init() throws Exception {
        System.out.println("Starting Jetty server ...");
        server = new JettyTestServer(HTTP_PORT);
        new Thread(server).start();

        server.join();
        server.registerCallback(new JettyServerTestCallback());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("Stopping Jetty server ...");
        server.stop();
        server.join();
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    @Test
    public void testHttpProtocolGet() throws Exception {
        JSONObject expected = getExpected(new URL(HTTP_ROOTURL + "/providers"), null, "GET");
        String simulated = TestHttpProtocolJetty.newRequest(HTTP_ROOTURL + "/providers", null, "GET");

        JSONAssert.assertEquals(expected, new JSONObject(simulated), false);

        JSONObject configuration = new JSONObject();
        configuration.put("uri", HTTP_ROOTURL + "/providers");
        configuration.put("httpMethod", "GET");
        //configuration.put("content", null);
        configuration.put("acceptType", "application/json");
        //configuration.put("contentType", "application/json");
        configuration.put("connectTimeout", 2000);
        configuration.put("readTimeout", 2000);
        //configuration.put("parameters", new JSONArray());
        simulated = TestHttpProtocolJetty.newRequest(configuration.toString());
        JSONAssert.assertEquals(expected, new JSONObject(simulated), false);

        StringBuilder builder = new StringBuilder();
        builder.append("HTTP ");
        builder.append("GET");
        builder.append(" Request [");
        builder.append(HTTP_ROOTURL + "/providers");
        builder.append("]");
        builder.append("\n\tContent-Type:");
        builder.append("null");
        builder.append("\n\tAccept:");
        builder.append("application/json");
        builder.append("\n\tConnection Timeout:");
        builder.append(2000);
        builder.append("\n\tRead Timeout:");
        builder.append(2000);

        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> config = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>(configuration.toString());

        assertEquals(builder.toString(), config.toString());

        configuration.put("parameters", new JSONArray().put(new JSONObject().put("key", "param1").put("value", "value1")).put(new JSONObject().put("key", "param2").put("value", 5)));
        simulated = TestHttpProtocolJetty.newRequest(configuration.toString());
        JSONAssert.assertNotEquals(expected, new JSONObject(simulated), true);

        expected.remove("url");
        expected.put("url", "/providers?param1=value1&param2=5");
        JSONAssert.assertEquals(expected, new JSONObject(simulated), false);
    }

    @Test
    public void testHttpProtocolPost() throws Exception {
        String simulated;
        simulated = TestHttpProtocolJetty.newRequest(HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", "{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://127.0.0.1:54460/\"}," + "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" + "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]}", "POST");
        JSONAssert.assertEquals(getExpected(new URL(HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE"), "{\"parameters\":[{\"name\":\"callback\",\"type\":\"string\",\"value\":\"http://127.0.0.1:54460/\"}," + "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" + "[{\"operator\":\"<\",\"operand\":200,\"type\":\"int\",\"complement\":false}]}]}", "POST"), new JSONObject(simulated), false);
    }

    @Test
    public void testHttpMidClient() throws Exception {
        ClientListener listener = new ClientListener();

        MidClient<SimpleResponse, MidRequest> client = new MidClient<SimpleResponse, MidRequest>(listener);

        client.setInitialDelay(200);
        client.setMaxDelay(1000);
        JSONObject configuration = new JSONObject();
        configuration.put("uri", HTTP_ROOTURL + "/error");
        configuration.put("httpMethod", "GET");
        //configuration.put("content", null);
        configuration.put("acceptType", "*/*");
        //configuration.put("contentType", "application/json");
        configuration.put("connectTimeout", 2000);
        configuration.put("readTimeout", 2000);
        //configuration.put("parameters", new JSONArray());

        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>(configuration.toString());
        assertEquals(-1, client.getCurrentDelay());

        MidRequest request = new MidRequest(builder, true);
        client.addRequest(request);
        listener.setAvailable(false);
        waitForAvailableMessage(listener, 5000);
        assertEquals(200, client.getCurrentDelay());
        listener.setAvailable(false);
        waitForAvailableMessage(listener, 5000);
        assertEquals(400, client.getCurrentDelay());
        listener.setAvailable(false);
        waitForAvailableMessage(listener, 5000);
        assertEquals(800, client.getCurrentDelay());
        listener.setAvailable(false);
        waitForAvailableMessage(listener, 5000);
        assertEquals(1000, client.getCurrentDelay());
        listener.setAvailable(false);
        waitForAvailableMessage(listener, 5000);
        assertEquals(1000, client.getCurrentDelay());

        listener.setAvailable(false);
        waitForAvailableMessage(listener, 5000);
        assertEquals(1000, client.getCurrentDelay());

        client.close();
    }

    private String waitForAvailableMessage(ClientListener client, long delay) {
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
