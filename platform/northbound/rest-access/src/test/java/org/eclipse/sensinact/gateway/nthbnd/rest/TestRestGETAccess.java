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

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.nthbnd.rest.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.rest.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.spi.JsonProvider;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestRestGETAccess {

	@InjectBundleContext BundleContext context;
	
	private String location =null;
	
	private JsonProvider provider = JsonProviderFactory.getProvider();
	
	@BeforeEach
	public void before() {
		Mediator mediator = new Mediator(context);
		location = ModelInstance.defaultLocation(mediator);
		String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/admin/resources/location/SET", 
			"{\"parameters\":[{\"name\": \"location\",\"value\": \""+location.replace("\"", "\\\"")+"\",\"type\": \"string\"}]}", "POST");
        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
	}
	
    @Test
    public void testHttpAccessMethodRawDescription() throws Exception {
    	Mediator mediator = new Mediator(context);

        String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers?rawDescribe=true", null, "GET");
        System.out.println(simulated);

        assertEquals(new HashSet<>(Arrays.asList("slider", "light")), 
        		provider.createReader(new StringReader(simulated)).readArray().stream()
        			.map(JsonString.class::cast)
        			.map(JsonString::getString)
        			.collect(toSet()));
    }

    @Test
    public void testHttpRoot(@InjectBundleContext BundleContext context) throws Exception {
    	Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL, null, "GET");    
        String loc = location.replace("\"", "\\\"");
        
        JsonObject slider = provider.createReader(new StringReader(
        "{\"name\":\"slider\",\"location\":\""+loc+"\",\"services\":[{\"name\":\"admin\",\"resources\":"
        + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"},{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"},{\"name\":"
        + "\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"},{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]},{\"name\":\"cursor\",\"resources\":"
        + "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]}]}")).readObject();
        
        JsonObject light = provider.createReader(new StringReader(
        "{\"name\":\"light\",\"location\":\""+loc+"\",\"services\":"
        + "[{\"name\":\"admin\",\"resources\":[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"},{\"name\":"
        + "\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"},{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"},{\"name\":\"icon\",\"type\":"
        + "\"PROPERTY\",\"rws\":\"RW\"}]},{\"name\":\"switch\",\"resources\":[{\"name\":\"status\",\"type\":\"STATE_VARIABLE\",\"rws\":\"RO\"},"
        + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\",\"rws\":\"RO\"},{\"name\":\"turn_on\",\"type\":\"ACTION\"},"
        + "{\"name\":\"turn_off\",\"type\":\"ACTION\"},{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}")).readObject();
        
        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
        
        assertEquals("/", response.getString("uri"));
		assertEquals(200, response.getInt("statusCode"));
		assertEquals("COMPLETE_LIST", response.getString("type"));
		assertEquals(new HashSet<>(Arrays.asList(slider, light)), response.getJsonArray("providers").stream()
    			.collect(toSet()));
    }

    @Test
    public void testHttpAccessMethodGET(@InjectBundleContext BundleContext context, @InjectService(timeout = 500) SliderSetterItf slider) throws Exception {
        Mediator mediator = new Mediator(context);
        
        String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers", null, "GET");

        System.out.println(simulated);

        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
		assertEquals("/", response.getString("uri"));
		assertEquals(200, response.getInt("statusCode"));
		assertEquals("PROVIDERS_LIST", response.getString("type"));
		assertEquals(new HashSet<>(Arrays.asList("slider", "light")), response.getJsonArray("providers").stream()
				.map(JsonString.class::cast)
    			.map(JsonString::getString)
    			.collect(toSet()));

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/slider/services", null, "GET");
        System.out.println(simulated);

        response = provider.createReader(new StringReader(simulated)).readObject();

        JsonArray array = response.getJsonArray("services");
        assertTrue(array.size() == 2);
        assertEquals(provider.createReader(new StringReader("[\"admin\",\"cursor\"]")).readArray(), array);

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/cursor/resources", null, "GET");

        response = provider.createReader(new StringReader(simulated)).readObject();
        array = response.getJsonArray("resources");
        assertEquals(provider.createReader(new StringReader("[\"position\"]")).readArray(), array);

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position", null, "GET");
//        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
//        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(1);
        Thread.sleep(1000);
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/providers/slider/services/" 
        + "cursor/resources/position/GET", null, "GET");
        response = provider.createReader(new StringReader(simulated)).readObject();
        System.out.println(response);

        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        assertEquals(1, response.getJsonObject("response").getInt("value"));
    }

    @Test
    public void testSimplifiedHttpAccessMethodGET(@InjectBundleContext BundleContext context, @InjectService(timeout = 500) SliderSetterItf slider) throws Exception {
    	Mediator mediator = new Mediator(context);

        String simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/slider", null, "GET");
        System.out.println(simulated);
        JsonObject response = provider.createReader(new StringReader(simulated)).readObject().getJsonObject("response");

        JsonArray array = response.getJsonArray("services");
        assertTrue(array.size() == 2);
        assertEquals(provider.createReader(new StringReader("[\"admin\",\"cursor\"]")).readArray(), array);

        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/slider/cursor", null, "GET");

        response = provider.createReader(new StringReader(simulated)).readObject().getJsonObject("response");
        array = response.getJsonArray("resources");

        assertEquals(provider.createReader(new StringReader("[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]")).readArray(), array);

//        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
//        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(0);
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/slider/cursor/position/GET", null, "GET");
        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        assertEquals(0, response.getJsonObject("response").getInt("value"));
        slider.move(1);
        simulated = HttpServiceTestClient.newRequest(mediator, TestRestAccess.HTTP_ROOTURL + "/slider/cursor/position/GET", null, "GET");
        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        assertEquals(1, response.getJsonObject("response").getInt("value"));
    }

    @Test
    public void testWsAccessMethodRawDescription() throws Exception {
        WsServiceTestClient client = new WsServiceTestClient();
        new Thread(client).start();

        String simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers?rawDescribe=true", null);        
        System.out.println(simulated);
        JsonArray response = provider.createReader(new StringReader(simulated)).readArray();
        assertEquals(new HashSet<>(Arrays.asList("slider", "light")), response.stream()
				.map(JsonString.class::cast)
    			.map(JsonString::getString)
    			.collect(toSet()));
    }

    @Test
    public void testWsAccessMethodGET(@InjectService(timeout = 500) SliderSetterItf slider) throws Exception {
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        String simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers", null);
        System.out.println(simulated);

        JsonObject response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals("/", response.getString("uri"));
		assertEquals(200, response.getInt("statusCode"));
		assertEquals("PROVIDERS_LIST", response.getString("type"));
		assertEquals(new HashSet<>(Arrays.asList("slider", "light")), response.getJsonArray("providers").stream()
				.map(JsonString.class::cast)
    			.map(JsonString::getString)
    			.collect(toSet()));
		
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/slider/services", null);

        response = provider.createReader(new StringReader(simulated)).readObject();
        JsonArray array = response.getJsonArray("services");
        assertTrue(array.size() == 2);
        assertEquals(provider.createReader(new StringReader("[\"admin\",\"cursor\"]")).readArray(), array);
        
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/slider/services/cursor/resources", null);

        response = provider.createReader(new StringReader(simulated)).readObject();
        array = response.getJsonArray("resources");
        //JSONAssert.assertEquals(new JsonArray("[\"location\",\"position\"]"), array, false);
        assertEquals(provider.createReader(new StringReader("[\"position\"]")).readArray(), array);
//        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
//        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(1);
        Thread.sleep(1000);
        
        simulated = null;        
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/providers/slider/services/cursor/resources/position/GET", null);
        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        assertEquals(1, response.getJsonObject("response").getInt("value"));
        client.close();
    }

    @Test
    public void testSimplifiedWsAccessMethodGET(@InjectService(timeout = 500) SliderSetterItf slider) throws Exception {
        JsonObject response;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        String simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/slider", null);

        response = provider.createReader(new StringReader(simulated)).readObject().getJsonObject("response");
        JsonArray array = response.getJsonArray("services");
        assertTrue(array.size() == 2);
        assertEquals(provider.createReader(new StringReader("[\"admin\",\"cursor\"]")).readArray(), array);
        
        simulated = null;
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/slider/cursor", null);
        response = provider.createReader(new StringReader(simulated)).readObject().getJsonObject("response");
        array = response.getJsonArray("resources");
        assertEquals(provider.createReader(new StringReader("[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]")).readArray(), array);
//        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
//        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(1);
        Thread.sleep(1000);
        
        simulated = null;
        simulated = this.synchronizedRequest(client, TestRestAccess.WS_ROOTURL + "/slider/cursor/position/GET", null);
        response = provider.createReader(new StringReader(simulated)).readObject();
        assertEquals(200, response.getInt("statusCode"));
        assertEquals("/slider/cursor/position", response.getString("uri"));
        assertEquals(1, response.getJsonObject("response").getInt("value"));
        client.close();
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
