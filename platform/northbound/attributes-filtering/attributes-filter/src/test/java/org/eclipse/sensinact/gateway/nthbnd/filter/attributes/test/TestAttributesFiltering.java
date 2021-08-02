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

import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.filter.attributes.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.attributes.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestAttributesFiltering extends MidOSGiTest {
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

    public TestAttributesFiltering() throws Exception {
        super();
    }

    public boolean isExcluded(String fileName) {
        if ("org.apache.felix.framework.security.jar".equals(fileName)) {
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void doInit(Map configuration) {
        configuration.put("felix.auto.start.1",  
                "file:target/felix/bundle/org.osgi.service.component.jar "+  
                "file:target/felix/bundle/org.osgi.service.cm.jar "+  
                "file:target/felix/bundle/org.osgi.service.metatype.jar "+  
                "file:target/felix/bundle/org.osgi.namespace.extender.jar "+  
                "file:target/felix/bundle/org.osgi.util.promise.jar "+  
                "file:target/felix/bundle/org.osgi.util.function.jar "+  
                "file:target/felix/bundle/org.osgi.util.pushstream.jar "+
                "file:target/felix/bundle/org.osgi.service.log.jar "  +
                "file:target/felix/bundle/org.apache.felix.log.jar " + 
                "file:target/felix/bundle/org.apache.felix.scr.jar " +
        		"file:target/felix/bundle/org.apache.felix.fileinstall.jar " +
        		"file:target/felix/bundle/org.apache.felix.configadmin.jar " + 
        		"file:target/felix/bundle/org.apache.felix.framework.security.jar ");
        configuration.put("felix.auto.install.2",  
        	    "file:target/felix/bundle/org.eclipse.paho.client.mqttv3.jar " + 
                "file:target/felix/bundle/mqtt-utils.jar " + 
        	    "file:target/felix/bundle/sensinact-utils.jar " + 
                "file:target/felix/bundle/sensinact-common.jar " + 
        	    "file:target/felix/bundle/sensinact-datastore-api.jar " + 
                "file:target/felix/bundle/sensinact-security-none.jar " + 
                "file:target/felix/bundle/sensinact-generic.jar " + 
                "file:target/felix/bundle/slf4j-api.jar " + 
                "file:target/felix/bundle/slf4j-simple.jar");
        configuration.put("felix.auto.start.2", 
        		"file:target/felix/bundle/sensinact-signature-validator.jar " + 
        		"file:target/felix/bundle/sensinact-core.jar ");
        configuration.put("felix.auto.start.3", 
        		"file:target/felix/bundle/org.apache.felix.http.servlet-api.jar " + 
                "file:target/felix/bundle/org.apache.felix.http.jetty.jar " + 
        		"file:target/felix/bundle/http.jar " +
        		"file:target/felix/bundle/sensinact-northbound-access.jar " + 
                "file:target/felix/bundle/rest-access.jar");
        configuration.put("felix.auto.start.4",
                "file:target/felix/bundle/slider.jar " + 
        		"file:target/felix/bundle/dynamicBundle.jar ");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");

        configuration.put("org.eclipse.sensinact.gateway.location.latitude", "45.2d");
        configuration.put("org.eclipse.sensinact.gateway.location.longitude", "5.7d");

        configuration.put("org.osgi.service.http.port", "8899");
        configuration.put("org.apache.felix.http.jettyEnabled", true);
        configuration.put("org.apache.felix.http.whiteboardEnabled", true);
    }

    @Test
    public void testHttpFiltered() throws Exception {
        Mediator mediator = new Mediator(context);
        String response = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?attrs=[friendlyName]", null, "GET");
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
        + ",\"friendlyName\":null}]}");
        JSONAssert.assertEquals(expected, new JSONObject(response), false);
        
        HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/slider/admin/friendlyName/SET",
        "[{\"name\":\"attributeName\",\"type\":\"string\",\"value\":\"value\"},{\"name\":\"value\",\"type\":\"string\",\"value\":\"mySlider\"}]", "POST");

        Thread.sleep(2000);
        response = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?attrs=[friendlyName]", null, "GET");
        
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
        
        response = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?attrs={friendlyName,icon}", null, "GET");
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

        response = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?attrs=friendlyName,icon,bridge", null, "GET");
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
        + ",\"bridge\":\"slider\"}]}");
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
        + ",\"friendlyName\":null}]}");
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
        + ",\"bridge\":\"slider\"}]}");
        JSONAssert.assertEquals(expected, result, false);
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
