/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.test;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.spi.JsonProvider;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestJsonPathFiltering {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//

    protected static final String HTTP_ROOTURL = "http://127.0.0.1:8899";
    protected static final String WS_ROOTURL = "/sensinact";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    private final JsonProvider json = JsonProviderFactory.getProvider();

    @Test
    public void testHttpFiltered(@InjectBundleContext BundleContext context) throws Exception {
    	Mediator mediator = new Mediator(context);
		String location = ModelInstance.defaultLocation(mediator);
        String simulated3 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact?jsonpath=$.[?(@.name=='slider')]", null, "GET");
        JsonObject expected = json.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"$.[?(@.name=='slider')]\",\"type\":\"jsonpath\"}]," 
        + "\"providers\":" 
        + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" 
        + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
        + "}]" 
        + ",\"location\":\""+location.replace("\"", "\\\"")+"\"}],"
        + "\"type\":\"COMPLETE_LIST\",\"uri\":\"/\",\"statusCode\":200}")).readObject();
        JsonObject actual = json.createReader(new StringReader(simulated3)).readObject();
        
		assertEquals(expected, actual);

        String simulated1 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact/providers", null, "GET");
        actual = json.createReader(new StringReader(simulated1)).readObject();
        
        assertEquals(200, actual.getInt("statusCode"));
        assertEquals("PROVIDERS_LIST", actual.getString("type"));
        assertEquals("/", actual.getString("uri"));
        assertEquals(new HashSet<>(Arrays.asList("slider", "light")), actual.getJsonArray("providers").stream()
				.map(JsonString.class::cast)
    			.map(JsonString::getString)
    			.collect(toSet()));

        String simulated2 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact/providers?jsonpath=$.[:1]", null, "GET");
        expected = json.createReader(new StringReader("{\"statusCode\":200,\"providers\":[\"" + 
        		json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getString(0) + 
        		"\"],\"filters\":[{\"type\":\"jsonpath\", \"definition\":\"$.[:1]\"}], " 
        		+ "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}")).readObject();
        assertEquals(expected, json.createReader(new StringReader(simulated2)).readObject());
    }

    @Test
    public void testWsFiltered(@InjectBundleContext BundleContext context) throws Exception {
    	Mediator mediator = new Mediator(context);
		String location = ModelInstance.defaultLocation(mediator);
        JsonObject expected;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        String simulated3 = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"jsonpath\",\"type\":\"string\",\"value\":\"$.[?(@.name=='slider')]\"}]");

        //System.out.println(simulated3);

        expected = json.createReader(new StringReader("{\"filters\":[{\"definition\":\"$.[?(@.name=='slider')]\",\"type\":\"jsonpath\"}]," 
        + "\"providers\":" 
        + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" 
        + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
        + "{\"name\":\"cursor\"," 
        + "\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]}]" 
        + ",\"location\":\""+location.replace("\"", "\\\"")+"\"}],"
        + "\"type\":\"COMPLETE_LIST\",\"uri\":\"/\",\"statusCode\":200}")).readObject();
        JsonObject actual = json.createReader(new StringReader(simulated3)).readObject();
        assertEquals(expected, actual);

        String simulated1 = this.synchronizedRequest(client, "/sensinact/providers", null);

        //System.out.println(simulated1);
        actual = json.createReader(new StringReader(simulated1)).readObject();
        
        assertEquals(200, actual.getInt("statusCode"));
        assertEquals("PROVIDERS_LIST", actual.getString("type"));
        assertEquals("/", actual.getString("uri"));
        assertEquals(new HashSet<>(Arrays.asList("slider", "light")), actual.getJsonArray("providers").stream()
				.map(JsonString.class::cast)
    			.map(JsonString::getString)
    			.collect(toSet()));
        
        String simulated2 = this.synchronizedRequest(client, "/sensinact/providers", "[{\"name\":\"jsonpath\",\"type\":\"string\",\"value\":\"$.[:1]\"}]");
        expected = json.createReader(new StringReader("{\"statusCode\":200,\"providers\":[\"" + 
        		json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getString(0) 
        		+ "\"], \"filters\":[{\"type\":\"jsonpath\", \"definition\":\"$.[:1]\"}], " 
        		+ "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}")).readObject();

        
        String provider1 = actual.getJsonArray("providers").getString(0);
        
        actual = json.createReader(new StringReader(simulated2)).readObject();

        assertEquals(200, actual.getInt("statusCode"));
        assertEquals("PROVIDERS_LIST", actual.getString("type"));
        assertEquals("/", actual.getString("uri"));
        assertEquals("jsonpath", actual.getJsonArray("filters").getJsonObject(0).getString("type"));
        assertEquals("$.[:1]", actual.getJsonArray("filters").getJsonObject(0).getString("definition"));
        assertEquals(Collections.singleton(provider1), actual.getJsonArray("providers").stream()
				.map(JsonString.class::cast)
    			.map(JsonString::getString)
    			.collect(toSet()));

        client.close();
    }

    private String synchronizedRequest(WsServiceTestClient client, String url, String content) {
        String simulated = null;
        long wait = 1000;

        client.newRequest(url, content);

        while (!client.isAvailable() && wait > 0) {
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
