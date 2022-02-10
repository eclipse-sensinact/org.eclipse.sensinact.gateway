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
package org.eclipse.sensinact.gateway.nthbnd.filter.geojson.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.nthbnd.filter.geojson.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.geojson.ws.test.WsServiceTestClient;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestGeoJsonFiltering{
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


    
    @AfterEach
    public void afterEach(@InjectBundleContext BundleContext context) throws InterruptedException {
    	Mediator mediator = new Mediator(context);

   	 String defaultLocation = "{\"type\":\"FeatureCollection\",\"features\":"
   	 + "[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"coordinates\":[5.7,45.2],\"type\":\"Point\"}}]}";
   	 
    	String response = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/slider/admin/location/SET", 
    			"[{\"name\":\"value\",\"type\":\"string\",\"value\":\""+defaultLocation.replace("\"", "\\\"")+"\"}]", "POST");
        System.out.println(response);
    	Thread.sleep(1000);
    }

    @Test
    public void testHttpFiltered(
    		@InjectBundleContext BundleContext context
    		) throws Exception {
        Mediator mediator = new Mediator(context);
		String location = ModelInstance.defaultLocation(mediator);
        //(&(latitude <= 45.20899800276024)(latitude >= 45.191001997239766)(longitude <= 5.712727172127145)(longitude >= 5.687272827872856))

        String simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?geojson={'latitude':45.2,'longitude':5.7,'distance':1000}", null, "GET");
        System.out.println(simulated3);
        
        JSONObject response = new JSONObject(
        "{\"providers\":[{\"name\":\"slider\",\"location\":\""+location.replace("\"", "\\\"")+"\"," 
        + "\"services\":[{\"name\":\"admin\",\"resources\":" 
        + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"},"
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]}]}," 
        + "{\"name\":\"light\",\"location\":\""+location.replace("\"", "\\\"")+"\",\"services\":" 
        + "[{\"name\":\"admin\",\"resources\":[" 
        + "{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"switch\",\"resources\":[" 
        + "{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"}," 
        + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"}," 
        + "{\"name\":\"turn_on\",\"type\":\"ACTION\"}," 
        + "{\"name\":\"turn_off\",\"type\":\"ACTION\"}," 
        + "{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}]," 
        + "\"filters\":[{\"definition\":\"{'latitude':45.2,'longitude':5.7,'distance':1000}\"," 
        + "\"type\":\"geojson\"}],\"type\":\"COMPLETE_LIST\"," 
        + "\"uri\":\"/\",\"statusCode\":200}");

        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

        simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?geojson={'latitude':45.2,'longitude':5.7,'distance':1000,'output':true}", null, "GET");
        System.out.println(simulated3);
        
        response = new JSONObject("{\"providers\":" 
        + "{\"type\": \"FeatureCollection\", \"features\": [" 
        + "{\"properties\":{},\"type\":\"Feature\",\"geometry\":" 
        + "{\"type\":\"Point\",\"coordinates\":[5.7,45.2]}},"
        + "{\"properties\":{},\"type\":\"Feature\",\"geometry\":" 
        + "{\"type\":\"Point\",\"coordinates\":[5.7,45.2]}}]}," 
        + "\"statusCode\":200,\"type\":\"COMPLETE_LIST\"," 
        + "\"uri\":\"/\",\"filters\":[{\"definition\":" 
        + "\"{'latitude':45.2,'longitude':5.7,'distance':1000,'output':true}\"," 
        + "\"type\":\"geojson\"}]}");

        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

      	 String newLocation = "{\"type\":\"FeatureCollection\",\"features\":"
      	 + "[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"coordinates\":[5.7,44.0],\"type\":\"Point\"}}]}";
      	 
        simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/slider/admin/location/SET", 
        		"[{\"name\":\"value\",\"type\":\"string\",\"value\":\""+newLocation.replace("\"", "\\\"")+"\"}]", "POST");
        
        System.out.println(simulated3);
        Thread.sleep(1000);

        simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?geojson={'latitude':45.2,'longitude':5.7,'distance':1000}", null, "GET");
        System.out.println(simulated3);
        response = new JSONObject(
        	"{\"providers\":[{\"name\":\"light\",\"location\":\""+location.replace("\"", "\\\"")+"\",\"services\":" 
        + "[{\"name\":\"admin\",\"resources\":[" 
        + "{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"switch\",\"resources\":[" 
        + "{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"}," 
        + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"}," 
        + "{\"name\":\"turn_on\",\"type\":\"ACTION\"}," 
        + "{\"name\":\"turn_off\",\"type\":\"ACTION\"}," 
        + "{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}],\"type\":\"COMPLETE_LIST\"," + "\"uri\":\"/\",\"statusCode\":200}");
        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);
  
    }

    @Test
    public void testWsFiltered(@InjectBundleContext BundleContext context) throws Exception {
    	Mediator mediator = new Mediator(context);
    	String location = ModelInstance.defaultLocation(mediator);
        WsServiceTestClient client = new WsServiceTestClient();
        new Thread(client).start();
        String simulated3 = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"geojson\",\"type\":\"string\",\"value\":\"{'latitude':45.2,'longitude':5.7,'distance':1000}\"}]");
        System.out.println(simulated3);
        
        JSONObject response = new JSONObject(
        "{\"providers\":[{\"name\":\"slider\",\"location\":\""+location.replace("\"", "\\\"")+"\"," 
        + "\"services\":[{\"name\":\"admin\",\"resources\":" 
        + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]}]}," 
        + "{\"name\":\"light\",\"location\":\""+location.replace("\"", "\\\"")+"\",\"services\":" 
        + "[{\"name\":\"admin\",\"resources\":[" 
        + "{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]},"
        + "{\"name\":\"switch\",\"resources\":[" 
        + "{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"}," 
        + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"}," 
        + "{\"name\":\"turn_on\",\"type\":\"ACTION\"}," 
        + "{\"name\":\"turn_off\",\"type\":\"ACTION\"}," 
        + "{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}]," 
        + "\"filters\":[{\"definition\":\"{'latitude':45.2,'longitude':5.7,'distance':1000}\"," 
        + "\"type\":\"geojson\"}],\"type\":\"COMPLETE_LIST\"," 
        + "\"uri\":\"/\",\"statusCode\":200}");

        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

        simulated3 = this.synchronizedRequest(client, "/sensinact", 
        		"[{\"name\":\"geojson\",\"type\":\"string\",\"value\":\"{'latitude':45.2,'longitude':5.7,'distance':1000,'output':true}\"}]");
        System.out.println(simulated3);
        
        response = new JSONObject("{\"providers\":" 
        + "{\"type\": \"FeatureCollection\", \"features\": [" 
        + "{\"properties\":{},\"type\":\"Feature\",\"geometry\":" 
        + "{\"type\":\"Point\",\"coordinates\":[5.7,45.2]}}," 
        + "{\"properties\":{},\"type\":\"Feature\",\"geometry\":" 
        + "{\"type\":\"Point\",\"coordinates\":[5.7,45.2]}}]},"
        + "\"statusCode\":200,\"type\":\"COMPLETE_LIST\"," 
        + "\"uri\":\"/\",\"filters\":[{\"definition\":" 
        + "\"{'latitude':45.2,'longitude':5.7,'distance':1000,'output':true}\"," 
        + "\"type\":\"geojson\"}]}");

     	 String newLocation = "{\"type\":\"FeatureCollection\",\"features\":"
     	 + "[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"coordinates\":[5.7,44.0],\"type\":\"Point\"}}]}";
     	 
        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);
        simulated3 = this.synchronizedRequest(client, "/sensinact/slider/admin/location/SET", 
        		"[{\"name\":\"value\",\"type\":\"string\",\"value\":\""+newLocation.replace("\"", "\\\"")+"\"}]");
        System.out.println(simulated3);
        Thread.sleep(1000);

        simulated3 = this.synchronizedRequest(client, "/sensinact", 
        		"[{\"name\":\"geojson\",\"type\":\"string\",\"value\":\"{'latitude':45.2,'longitude':5.7,'distance':1000}\"}]");
        System.out.println(simulated3);
        
        response = new JSONObject(
        "{\"providers\":[{\"name\":\"light\",\"location\":\""+location.replace("\"", "\\\"")+"\",\"services\":" 
        + "[{\"name\":\"admin\",\"resources\":[" 
        + "{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"switch\",\"resources\":[" 
        + "{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"}," 
        + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"}," 
        + "{\"name\":\"turn_on\",\"type\":\"ACTION\"}," 
        + "{\"name\":\"turn_off\",\"type\":\"ACTION\"}," 
        + "{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}],\"type\":\"COMPLETE_LIST\"," 
        + "\"uri\":\"/\",\"statusCode\":200}");
        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);
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
