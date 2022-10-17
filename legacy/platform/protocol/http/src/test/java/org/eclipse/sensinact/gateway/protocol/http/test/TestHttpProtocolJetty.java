/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.MidClient;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.MidClientListener;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.Reusable;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;

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

        @Override
        public void respond(SimpleResponse response) {
            if (response != null) 
            	if(response.getContent() == null)
                    this.message = "NO RESPONSE";
            	else
                    this.message = new String(response.getContent());
            else
                this.message = "NO RESPONSE";
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
    public static int HTTP_PORT = 8899;
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
                    builder.setContent(JsonProviderFactory.getProvider()
                    		.createReader(new StringReader(content)).readValue().toString());
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

    private static JsonObject getExpected(URL url, String content, String method) {
    	JsonObjectBuilder requestDescription = JsonProviderFactory.getProvider().createObjectBuilder();
        requestDescription.add("method", method);
        requestDescription.add("url", url.getPath());


        if (method.equals("POST")) {
            requestDescription.add("content-type", "application/json");
            JsonValue value = JsonProviderFactory.getProvider().createReader(new StringReader(content)).readValue();
            requestDescription.add("content-length", value.toString().length());
			requestDescription.add("message", value);
        }
        return requestDescription.build();
    }

    private static JettyTestServer server = null;

    @BeforeAll
    public static void init() throws Exception {
        System.out.println("Starting Jetty server ...");
        server = new JettyTestServer(HTTP_PORT);
        new Thread(server).start();
        server.join();
        server.registerCallback(new JettyServerTestCallback());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        System.out.println("Stopping Jetty server ...");
        server.stop();
        server.join();
    }

    //***************************************************************//
    //						INSTANCE DECLARATIONS						         //
    //***************************************************************//

    @Test
    public void testHttpProtocolGet() throws Exception {
        JsonObject expected = getExpected(new URL(HTTP_ROOTURL + "/providers"), null, "GET");
        String simulated = TestHttpProtocolJetty.newRequest(HTTP_ROOTURL + "/providers", null, "GET");

        JsonProvider provider = JsonProviderFactory.getProvider();
		JsonObject actual = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(expected, actual);

        JsonObject configuration = provider.createObjectBuilder()
        		.add("uri", HTTP_ROOTURL + "/providers")
        		.add("httpMethod", "GET")
        		.add("acceptType", "application/json")
        		.add("connectTimeout", 2000)
        		.add("readTimeout", 2000)
        		.build();
        simulated = TestHttpProtocolJetty.newRequest(configuration.toString());
        actual = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(expected, actual);

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

        configuration = provider.createObjectBuilder(configuration)
        		.add("parameters", provider.createArrayBuilder()
        				.add(provider.createObjectBuilder().add("key", "param1").add("value", "value1"))
        				.add(provider.createObjectBuilder().add("key", "param2").add("value", 5)))
        		.build();
        simulated = TestHttpProtocolJetty.newRequest(configuration.toString());
        actual = provider.createReader(new StringReader(simulated)).readObject();
        assertNotEquals(expected, actual);

        expected = provider.createObjectBuilder(expected).add("url", "/providers?param1=value1&param2=5").build();
        assertEquals(expected, actual);
    }

    @Test
    public void testHttpProtocolPost() throws Exception {
        String simulated = TestHttpProtocolJetty.newRequest(HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", 
        		"{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://127.0.0.1:8899/\"}," 
        				+ "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" 
        				+ "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]}", 
        		"POST");
        JsonObject actual = JsonProviderFactory.getProvider().createReader(new StringReader(simulated)).readObject();
        assertEquals(getExpected(new URL(HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE"), 
        		"{\"parameters\":[{\"name\":\"callback\",\"type\":\"string\",\"value\":\"http://127.0.0.1:8899/\"}," 
        				+ "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" 
        				+ "[{\"operator\":\"<\",\"operand\":200,\"type\":\"int\",\"complement\":false}]}]}", 
        				"POST"), actual);
    }

    @Test
    public void testHttpMidClient() throws Exception {
        ClientListener listener = new ClientListener();
        MidClient<SimpleResponse, MidRequest> client = new MidClient<SimpleResponse, MidRequest>(listener);

        client.setInitialDelay(200);
        client.setMaxDelay(1000);
        JsonObjectBuilder configuration = JsonProviderFactory.getProvider().createObjectBuilder()
        	.add("uri", HTTP_ROOTURL + "/error")
        	.add("httpMethod", "GET")
        	.add("acceptType", "*/*")
        	.add("connectTimeout", 2000)
        	.add("readTimeout", 2000);

        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = 
        		new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>(configuration.build().toString());
        MidRequest request = new MidRequest(builder, true);
        
        assertEquals(-1, client.getCurrentDelay());
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
