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
package org.eclipse.sensinact.gateway.nthbnd.filter.attributes.test;

import org.eclipse.sensinact.gateway.nthbnd.filter.attributes.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.attributes.ws.test.WsServiceTestClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestAttributesFiltering {
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
    
    @BeforeEach
    public void beforeEach() throws Exception {
    	HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact/slider/admin/friendlyName/SET",
    	        "[{\"name\":\"attributeName\",\"type\":\"string\",\"value\":\"value\"},{\"name\":\"value\",\"type\":\"string\",\"value\":\"startName\"}]", "POST");
    	Thread.sleep(2000);
    }

    @Test
    public void testHttpFiltered() throws Exception {
        String response = HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact?attrs=[friendlyName]", null, "GET");
        JSONObject result = new JSONObject(response);
        JSONObject expected = new JSONObject(
        "{\"filters\":[{\"definition\":\"[friendlyName]\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\"45.2:5.7\""
        + ",\"friendlyName\":\"startName\"}]}");
        
        System.out.println("==============================================");
        System.out.println("result: ");
        System.out.println(response);
        System.out.println(expected);
        System.out.println("==============================================");
        
        JSONAssert.assertEquals(expected, new JSONObject(response), false);
        
        HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact/slider/admin/friendlyName/SET",
        "[{\"name\":\"attributeName\",\"type\":\"string\",\"value\":\"value\"},{\"name\":\"value\",\"type\":\"string\",\"value\":\"mySlider\"}]", "POST");

        Thread.sleep(2000);
        response = HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact?attrs=[friendlyName]", null, "GET");
        
        result = new JSONObject(response);
        expected = new JSONObject(
        "{\"filters\":[{\"definition\":\"[friendlyName]\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\"45.2:5.7\""
        + ",\"friendlyName\":\"mySlider\"}]}");
        JSONAssert.assertEquals(expected, new JSONObject(response), false);
        
        response = HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact?attrs={friendlyName,icon}", null, "GET");
        result = new JSONObject(response);
        expected = new JSONObject(
        "{\"filters\":[{\"definition\":\"{friendlyName,icon}\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\"45.2:5.7\""
        + ",\"icon\":null"
        + ",\"friendlyName\":\"mySlider\"}]}");
        JSONAssert.assertEquals(expected, result, false);

        response = HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact?attrs=friendlyName,icon,bridge", null, "GET");
        result = new JSONObject(response);
        expected = new JSONObject(
        "{\"filters\":[{\"definition\":\"friendlyName,icon,bridge\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\"45.2:5.7\""
        + ",\"friendlyName\":\"mySlider\""
        + ",\"icon\":null"
        + ",\"bridge\":\"org.eclipse.sensinact.gateway.simulated.devices.slider\"}]}");
        JSONAssert.assertEquals(expected, result, false);
    }

    @Test
    public void testWsFiltered() throws Exception {
        WsServiceTestClient client = new WsServiceTestClient();
        new Thread(client).start();
        
        String response = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"attrs\",\"type\":\"string\",\"value\":\"[friendlyName]\"}]");
        JSONObject result = new JSONObject(response);
        JSONObject expected = new JSONObject(
        "{\"filters\":[{\"definition\":\"[friendlyName]\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\"45.2:5.7\""
        + ",\"friendlyName\":\"startName\"}]}");
        JSONAssert.assertEquals(expected, result, false);

        this.synchronizedRequest(client, "/sensinact/slider/admin/friendlyName/SET", "[{\"name\":\"attributeName\",\"type\":\"string\",\"value\":\"value\"},{\"name\":\"value\",\"type\":\"string\",\"value\":\"mySlider\"}]");

        Thread.sleep(2000);

        response = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"attrs\",\"type\":\"string\",\"value\":\"[friendlyName]\"}]");
        result = new JSONObject(response);
        expected = new JSONObject(
        "{\"filters\":[{\"definition\":\"[friendlyName]\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\"45.2:5.7\""
        + ",\"friendlyName\":\"mySlider\"}]}");
        JSONAssert.assertEquals(expected, result, false);

        response = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"attrs\",\"type\":\"string\",\"value\":\"{friendlyName,icon}\"}]");
        result = new JSONObject(response);
        expected = new JSONObject(
        "{\"filters\":[{\"definition\":\"{friendlyName,icon}\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\"45.2:5.7\""
        + ",\"icon\":null"
        + ",\"friendlyName\":\"mySlider\"}]}");
        JSONAssert.assertEquals(expected, result, false);

        response = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"attrs\",\"type\":\"string\",\"value\":\"friendlyName,icon,bridge\"}]");
        System.err.println(response);
        result = new JSONObject(response);
        expected = new JSONObject(
        "{\"filters\":[{\"definition\":\"friendlyName,icon,bridge\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\"45.2:5.7\""
        + ",\"friendlyName\":\"mySlider\""
        + ",\"icon\":null"
        + ",\"bridge\":\"org.eclipse.sensinact.gateway.simulated.devices.slider\"}]}");
        JSONAssert.assertEquals(expected, result, false);
        client.close();
    }

	private String synchronizedRequest(WsServiceTestClient client, String url, String content) {

		String response = null;
        long wait = 2000;

        client.newRequest(url, content);

        while (!client.isAvailable() && wait > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        if (client.isAvailable()) {
        	response = client.getResponseMessage();
        }
        return response;
	}
}
