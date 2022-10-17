/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.filter.attributes.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.nthbnd.filter.attributes.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.attributes.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

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

    private JsonProvider provider = JsonProviderFactory.getProvider();
	 private String location; 
	 
	@BeforeEach
	public void before(@InjectBundleContext BundleContext context) throws InterruptedException {
		Mediator mediator = new Mediator(context);
		location = ModelInstance.defaultLocation(mediator);
    	HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact/slider/admin/friendlyName/SET",
    	        "[{\"name\":\"attributeName\",\"type\":\"string\",\"value\":\"value\"},{\"name\":\"value\",\"type\":\"string\",\"value\":\"startName\"}]", "POST");
    	Thread.sleep(2000);
    }

    @Test
    public void testHttpFiltered() throws Exception {
        String response = HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact?attrs=[friendlyName]", null, "GET");
        JsonObject result = provider.createReader(new StringReader(response)).readObject();
        JsonObject expected = provider.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"[friendlyName]\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
        + "}]" 
        + ",\"location\":\""+location.replace("\"","\\\"")+"\""
        + ",\"friendlyName\":\"startName\"}]"
        + ",\"type\":\"COMPLETE_LIST\""
        + ",\"uri\":\"/\""
        + ",\"statusCode\":200}")).readObject();
        
        System.out.println("==============================================");
        System.out.println("result: ");
        System.out.println(response);
        System.out.println(expected);
        System.out.println("==============================================");
        
        assertEquals(expected, result);
        
        HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact/slider/admin/friendlyName/SET",
        "[{\"name\":\"attributeName\",\"type\":\"string\",\"value\":\"value\"},{\"name\":\"value\",\"type\":\"string\",\"value\":\"mySlider\"}]", "POST");

        Thread.sleep(2000);
        response = HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact?attrs=[friendlyName]", null, "GET");
        
        result = provider.createReader(new StringReader(response)).readObject();
        expected = provider.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"[friendlyName]\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
        + "}]" 
        + ",\"location\":\""+location.replace("\"","\\\"")+"\""
        + ",\"friendlyName\":\"mySlider\"}]"
        + ",\"type\":\"COMPLETE_LIST\""
        + ",\"uri\":\"/\""
        + ",\"statusCode\":200}")).readObject();
        assertEquals(expected, result);
        
        response = HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact?attrs={friendlyName,icon}", null, "GET");
        result = provider.createReader(new StringReader(response)).readObject();
        expected = provider.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"{friendlyName,icon}\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
        + "}]" 
        + ",\"location\":\""+location.replace("\"","\\\"")+"\""
        + ",\"icon\":null"
        + ",\"friendlyName\":\"mySlider\"}]"
        + ",\"type\":\"COMPLETE_LIST\""
        + ",\"uri\":\"/\""
        + ",\"statusCode\":200}")).readObject();
        assertEquals(expected, result);

        response = HttpServiceTestClient.newRequest(HTTP_ROOTURL + "/sensinact?attrs=friendlyName,icon,bridge", null, "GET");
        result = provider.createReader(new StringReader(response)).readObject();
        expected = provider.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"friendlyName,icon,bridge\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
        + "}]" 
        + ",\"location\":\""+location.replace("\"","\\\"")+"\""
        + ",\"friendlyName\":\"mySlider\""
        + ",\"icon\":null"
        + ",\"bridge\":\"org.eclipse.sensinact.gateway.simulated.devices.slider\"}]"
        + ",\"type\":\"COMPLETE_LIST\""
        + ",\"uri\":\"/\""
        + ",\"statusCode\":200}")).readObject();
        assertEquals(expected, result);
    }

    @Test
    public void testWsFiltered() throws Exception {
        WsServiceTestClient client = new WsServiceTestClient();
        new Thread(client).start();
        
        String response = this.synchronizedRequest(client, "/sensinact", 
        		"[{\"name\":\"attrs\",\"type\":\"string\",\"value\":\"[friendlyName]\"}]");
        JsonObject result = provider.createReader(new StringReader(response)).readObject();
        JsonObject expected = provider.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"[friendlyName]\",\"type\":\"attrs\"}]," 
    	+ "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
    	+ "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
    	+ "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
    	+ "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
    	+ "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
    	+ "{\"name\":\"cursor\",\"resources\":" 
    	+ "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
    	+ "}]" 
    	+ ",\"location\":\""+location.replace("\"","\\\"")+"\""
    	+ ",\"friendlyName\":\"startName\"}]"
    	+ ",\"type\":\"COMPLETE_LIST\""
    	+ ",\"uri\":\"/\""
    	+ ",\"statusCode\":200}")).readObject();
        assertEquals(expected, result);

        this.synchronizedRequest(client, "/sensinact/slider/admin/friendlyName/SET", 
        		"[{\"name\":\"attributeName\",\"type\":\"string\",\"value\":\"value\"},{\"name\":\"value\",\"type\":\"string\",\"value\":\"mySlider\"}]");

        Thread.sleep(2000);

        response = this.synchronizedRequest(client, "/sensinact", 
        		"[{\"name\":\"attrs\",\"type\":\"string\",\"value\":\"[friendlyName]\"}]");
        result = provider.createReader(new StringReader(response)).readObject();
        expected = provider.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"[friendlyName]\",\"type\":\"attrs\"}]," 
		+ "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
		+ "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
		+ "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
		+ "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
		+ "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
		+ "{\"name\":\"cursor\",\"resources\":" 
		+ "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
		+ "}]" 
		+ ",\"location\":\""+location.replace("\"","\\\"")+"\""
		+ ",\"friendlyName\":\"mySlider\"}]"
		+ ",\"type\":\"COMPLETE_LIST\""
		+ ",\"uri\":\"/\""
		+ ",\"statusCode\":200}")).readObject();
        assertEquals(expected, result);

        response = this.synchronizedRequest(client, "/sensinact", 
        		"[{\"name\":\"attrs\",\"type\":\"string\",\"value\":\"{friendlyName,icon}\"}]");
        result = provider.createReader(new StringReader(response)).readObject();
        expected = provider.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"{friendlyName,icon}\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
    	+ "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
    	+ "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
    	+ "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
    	+ "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
    	+ "{\"name\":\"cursor\",\"resources\":" 
    	+ "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
    	+ "}]" 
    	+ ",\"location\":\""+location.replace("\"","\\\"")+"\""
    	+ ",\"icon\":null"
    	+ ",\"friendlyName\":\"mySlider\"}]"
    	+ ",\"type\":\"COMPLETE_LIST\""
    	+ ",\"uri\":\"/\""
    	+ ",\"statusCode\":200}")).readObject();
        assertEquals(expected, result);

        response = this.synchronizedRequest(client, "/sensinact", 
        		"[{\"name\":\"attrs\",\"type\":\"string\",\"value\":\"friendlyName,icon,bridge\"}]");
        System.err.println(response);
        result = provider.createReader(new StringReader(response)).readObject();
        expected = provider.createReader(new StringReader(
        "{\"filters\":[{\"definition\":\"friendlyName,icon,bridge\",\"type\":\"attrs\"}]," 
        + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\",\"rws\":\"RO\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\",\"rws\":\"RW\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\",\"rws\":\"RO\"}]" 
        + "}]" 
        + ",\"location\":\""+location.replace("\"","\\\"")+"\""
        + ",\"friendlyName\":\"mySlider\""
        + ",\"icon\":null"
        + ",\"bridge\":\"org.eclipse.sensinact.gateway.simulated.devices.slider\"}]"
        + ",\"type\":\"COMPLETE_LIST\""
        + ",\"uri\":\"/\""
        + ",\"statusCode\":200}")).readObject();
        assertEquals(expected, result);
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
