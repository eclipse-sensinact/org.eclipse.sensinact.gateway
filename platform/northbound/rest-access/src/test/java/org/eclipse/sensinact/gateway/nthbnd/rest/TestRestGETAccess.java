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
import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertTrue;


public class TestRestGETAccess extends TestRestAccess {
    public TestRestGETAccess() throws Exception {
        super();
    }

    @Test
    public void testHttpAccessMethodRawDescription() throws Exception {
    	Mediator mediator = new Mediator(context);

        String simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers?rawDescribe=true", null, "GET");
        System.out.println(simulated);

        JSONArray response = new JSONArray("[\"slider\",\"light\"]");

        JSONAssert.assertEquals(response, new JSONArray(simulated), false);
    }

    @Test
    public void testHttpRoot() throws Exception {
    	Mediator mediator = new Mediator(context);
        String simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL, null, "GET");        
        JSONObject response = new JSONObject("{\"providers\":[{\"name\":\"slider\",\"location\":\"45.2:5.7\",\"services\":[{\"name\":\"admin\",\"resources\":[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"},{\"name\":\"location\",\"type\":\"PROPERTY\"},{\"name\":\"bridge\",\"type\":\"PROPERTY\"},{\"name\":\"icon\",\"type\":\"PROPERTY\"}]},{\"name\":\"cursor\",\"resources\":[{\"name\":\"position\",\"type\":\"SENSOR\"}]}]},{\"name\":\"light\",\"location\":\"45.2:5.7\",\"services\":[{\"name\":\"admin\",\"resources\":[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"},{\"name\":\"location\",\"type\":\"PROPERTY\"},{\"name\":\"bridge\",\"type\":\"PROPERTY\"},{\"name\":\"icon\",\"type\":\"PROPERTY\"}]},{\"name\":\"switch\",\"resources\":[{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"},{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"},{\"name\":\"turn_on\",\"type\":\"ACTION\"},{\"name\":\"turn_off\",\"type\":\"ACTION\"},{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}],\"type\":\"COMPLETE_LIST\",\"uri\":\"/\",\"statusCode\":200}");
        JSONAssert.assertEquals(response, new JSONObject(simulated), false);
    }

    @Test
    public void testHttpAccessMethodGET() throws Exception {
        Mediator mediator = new Mediator(context);

        String simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers", null, "GET");

        System.out.println(simulated);

        JSONObject response = new JSONObject("{\"statusCode\":200,\"providers\":[\"slider\",\"light\"]," + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");

        JSONAssert.assertEquals(response, new JSONObject(simulated), false);

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services", null, "GET");
        System.out.println(simulated);

        response = new JSONObject(simulated);

        JSONArray array = response.getJSONArray("services");
        assertTrue(array.length() == 2);
        JSONAssert.assertEquals(new JSONArray("[\"admin\",\"cursor\"]"), array, false);

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/cursor/resources", null, "GET");

        response = new JSONObject(simulated);
        array = response.getJSONArray("resources");
        JSONAssert.assertEquals(new JSONArray("[\"position\"]"), array, false);

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/cursor/resources/position", null, "GET");
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(1);
        Thread.sleep(1000);
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/providers/slider/services/" + "cursor/resources/position/GET", null, "GET");
        response = new JSONObject(simulated);
        System.out.println(response);

        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        assertTrue(response.getJSONObject("response").get("value").equals(1));
    }

    @Test
    public void testSimplifiedHttpAccessMethodGET() throws Exception {
    	Mediator mediator = new Mediator(context);

        String simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/slider", null, "GET");
        System.out.println(simulated);
        JSONObject response = new JSONObject(simulated).getJSONObject("response");

        JSONArray array = response.getJSONArray("services");
        assertTrue(array.length() == 2);
        JSONAssert.assertEquals(new JSONArray("[\"admin\",\"cursor\"]"), array, false);

        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/slider/cursor", null, "GET");

        response = new JSONObject(simulated).getJSONObject("response");
        array = response.getJSONArray("resources");

        JSONAssert.assertEquals(new JSONArray("[{\"name\":\"position\",\"type\":\"SENSOR\"}]"), array, false);

        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(0);
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/slider/cursor/position/GET", null, "GET");
        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        assertTrue(response.getJSONObject("response").get("value").equals(0));
        slider.move(1);
        simulated = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/slider/cursor/position/GET", null, "GET");
        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        assertTrue(response.getJSONObject("response").get("value").equals(1));
    }

    @Test
    public void testWsAccessMethodRawDescription() throws Exception {
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers?rawDescribe=true", null);
        
        System.out.println(simulated);
        JSONArray response = new JSONArray("[\"slider\",\"light\"]");
        JSONAssert.assertEquals(response, new JSONArray(simulated), false);
    }

    @Test
    public void testWsAccessMethodGET() throws Exception {
        JSONObject response;
        String simulated;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers", null);

        System.out.println(simulated);

        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"slider\",\"light\"]," + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        JSONAssert.assertEquals(response, new JSONObject(simulated), false);
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services", null);

        response = new JSONObject(simulated);
        JSONArray array = response.getJSONArray("services");
        assertTrue(array.length() == 2);
        JSONAssert.assertEquals(new JSONArray("[\"admin\",\"cursor\"]"), array, false);
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/cursor/resources", null);
        response = new JSONObject(simulated);
        array = response.getJSONArray("resources");
        //JSONAssert.assertEquals(new JSONArray("[\"location\",\"position\"]"), array, false);
        JSONAssert.assertEquals(new JSONArray("[\"position\"]"), array, false);
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(1);
        Thread.sleep(1000);
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/providers/slider/services/cursor/resources/position/GET", null);
        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        assertTrue(response.getJSONObject("response").get("value").equals(1));
        client.close();
    }

    @Test
    public void testSimplifiedWsAccessMethodGET() throws Exception {
        JSONObject response;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        String simulated = this.synchronizedRequest(client, WS_ROOTURL + "/slider", null);

        response = new JSONObject(simulated).getJSONObject("response");
        JSONArray array = response.getJSONArray("services");
        assertTrue(array.length() == 2);
        JSONAssert.assertEquals(new JSONArray("[\"admin\",\"cursor\"]"), array, false);
        
        simulated = null;
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/slider/cursor", null);
        response = new JSONObject(simulated).getJSONObject("response");
        array = response.getJSONArray("resources");
        JSONAssert.assertEquals(new JSONArray("[{\"name\":\"position\",\"type\":\"SENSOR\"}]"), array, false);
        MidProxy<SliderSetterItf> sliderProxy = new MidProxy<SliderSetterItf>(classloader, this, SliderSetterItf.class);
        SliderSetterItf slider = sliderProxy.buildProxy();
        slider.move(1);
        Thread.sleep(1000);
        
        simulated = null;
        simulated = this.synchronizedRequest(client, WS_ROOTURL + "/slider/cursor/position/GET", null);
        response = new JSONObject(simulated);
        assertTrue(response.get("statusCode").equals(200));
        assertTrue(response.getString("uri").equals("/slider/cursor/position"));
        assertTrue(response.getJSONObject("response").get("value").equals(1));
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
