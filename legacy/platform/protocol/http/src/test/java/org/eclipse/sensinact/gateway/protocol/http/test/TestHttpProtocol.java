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

import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

public class TestHttpProtocol {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    public static int HTTP_PORT = 8898;
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
                    JsonObject jsonData = JsonProviderFactory.getProvider().createReader(new StringReader(content)).readObject();
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

    private static JsonObject getExpected(URL url, String content, String method) {
        JsonObjectBuilder requestDescription = JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("method", method)
        		.add("url", url.getPath());


        if (method.equals("POST")) {
            JsonObject message = JsonProviderFactory.getProvider().createReader(new StringReader(content)).readObject();
            requestDescription.add("content-type", "application/json")
            	.add("content-length", content.length())
            	.add("message", message);
        }
        return requestDescription.build();
    }

    private static HttpTestServer server = null;

    @BeforeAll
    public static void init() throws Exception {
        if (server != null) {
            if (server.isStarted()) {
                server.stop();
                Thread.sleep(2000);
            }
            server = null;
        }
        server = new HttpTestServer(HTTP_PORT);
        new Thread(server).start();

        server.registerCallback(new HttpServerTestCallback());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        server.stop();
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    @Test
    public void testHttpProtocolGet() throws Exception {
        JsonObject expected = getExpected(new URL(HTTP_ROOTURL + "/providers"), null, "GET");
        String simulated = TestHttpProtocol.newRequest(HTTP_ROOTURL + "/providers", null, "GET");

        JsonProvider provider = JsonProviderFactory.getProvider();
		JsonObject actual = provider.createReader(new StringReader(simulated)).readObject();
		assertEquals(expected, actual);

        JsonObject configuration = provider.createObjectBuilder()
        		.add("uri", HTTP_ROOTURL + "/providers")
        		.add("httpMethod", "GET")
        //configuration.put("content", null);
        		.add("acceptType", "application/json")
        //configuration.put("contentType", "application/json");
        		.add("connectTimeout", 2000)
        		.add("readTimeout", 2000)
        		.build();
        //configuration.put("parameters", new JSONArray());
        simulated = TestHttpProtocol.newRequest(configuration.toString());
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
        
        simulated = TestHttpProtocol.newRequest(configuration.toString());
        actual = provider.createReader(new StringReader(simulated)).readObject();
        assertNotEquals(expected, actual);

        expected = provider.createObjectBuilder(expected)
        	.add("url", "/providers?param1=value1&param2=5")
        	.build();
        assertEquals(expected, actual);
    }

    @Test
    public void testHttpProtocolPost() throws Exception {
        String simulated = TestHttpProtocol.newRequest(HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE", 
        		"{\"parameters\" : [{\"name\":\"callback\", \"type\":\"string\",\"value\":\"http://127.0.0.1:8898/\"}," 
        				+ "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" 
        				+ "[{\"operator\":\"<\",\"operand\":200, \"type\":\"int\", \"complement\":false}]}]}", 
				"POST");

        JsonObject actual = JsonProviderFactory.getProvider().createReader(new StringReader(simulated)).readObject();
        assertEquals(getExpected(new URL(HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position/SUBSCRIBE"), 
        		"{\"parameters\":[{\"name\":\"callback\",\"type\":\"string\",\"value\":\"http://127.0.0.1:8898/\"}," 
        				+ "{\"name\":\"conditions\",\"type\":\"array\",\"value\":" 
        				+ "[{\"operator\":\"<\",\"operand\":200,\"type\":\"int\",\"complement\":false}]}]}", 
        		"POST"), actual);
    }

}
