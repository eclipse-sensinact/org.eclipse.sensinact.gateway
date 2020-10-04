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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.filter.geojson.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.geojson.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestGeoJsonFiltering extends MidOSGiTest {
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

    /**
     * @throws MalformedURLException
     * @throws IOException
     */
    public TestGeoJsonFiltering() throws Exception {
        super();
    }

    /**
     * @inheritDoc
     * @see MidOSGiTest#isExcluded(java.lang.String)
     */
    public boolean isExcluded(String fileName) {
        if ("org.apache.felix.framework.security.jar".equals(fileName)) {
            return true;
        }
        return false;
    }

    /**
     * @inheritDoc
     * @see MidOSGiTest#doInit(java.util.Map)
     */
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
        		"file:target/felix/bundle/dynamicBundle.jar " + 
                "file:target/felix/bundle/slider.jar " + 
        		"file:target/felix/bundle/light.jar ");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");

        configuration.put("org.eclipse.sensinact.gateway.location.latitude", "45.2d");
        configuration.put("org.eclipse.sensinact.gateway.location.longitude", "5.7d");

        configuration.put("org.osgi.service.http.port", "8899");
        configuration.put("org.apache.felix.http.jettyEnabled", true);
        configuration.put("org.apache.felix.http.whiteboardEnabled", true);

        try {
        	String fileName = "sensinact.config";
            File testFile = new File(new File("src/test/resources"), fileName);
            URL testFileURL = testFile.toURI().toURL();
            FileOutputStream output = new FileOutputStream(new File(loadDir,fileName));
            byte[] testCng = IOUtils.read(testFileURL.openStream(), true);
            IOUtils.write(testCng, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHttpFiltered() throws Exception {
        Mediator mediator = new Mediator(context);
        //(&(latitude <= 45.20899800276024)(latitude >= 45.191001997239766)(longitude <= 5.712727172127145)(longitude >= 5.687272827872856))

        String simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?geojson={'latitude':45.2,'longitude':5.7,'distance':1000}", null, "GET");
        System.out.println(simulated3);
        
        JSONObject response = new JSONObject("{\"providers\":[{\"name\":\"slider\",\"location\":\"45.2:5.7\"," + "\"services\":[{\"name\":\"admin\",\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"cursor\",\"resources\":" + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]}]}," + "{\"name\":\"light\",\"location\":\"45.2:5.7\",\"services\":" + "[{\"name\":\"admin\",\"resources\":[" + "{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"switch\",\"resources\":[" + "{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"}," + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"}," + "{\"name\":\"turn_on\",\"type\":\"ACTION\"}," + "{\"name\":\"turn_off\",\"type\":\"ACTION\"}," + "{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}]," + "\"filters\":[{\"definition\":\"{'latitude':45.2,'longitude':5.7,'distance':1000}\"," + "\"type\":\"geojson\"}],\"type\":\"COMPLETE_LIST\"," + "\"uri\":\"/\",\"statusCode\":200}");

        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

        simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?geojson={'latitude':45.2,'longitude':5.7,'distance':1000,'output':true}", null, "GET");
        System.out.println(simulated3);
        
        response = new JSONObject("{\"providers\":" + "{\"type\": \"FeatureCollection\", \"features\": [" + "{\"properties\":{\"name\":\"slider\"},\"type\":\"Feature\",\"geometry\":" + "{\"type\":\"Point\",\"coordinates\":[45.2,5.7]}}," + "{\"properties\":{\"name\":\"light\"},\"type\":\"Feature\",\"geometry\":" + "{\"type\":\"Point\",\"coordinates\":[45.2,5.7]}}]}," + "\"statusCode\":200,\"type\":\"COMPLETE_LIST\"," + "\"uri\":\"/\",\"filters\":[{\"definition\":" + "\"{'latitude':45.2,'longitude':5.7,'distance':1000,'output':true}\"," + "\"type\":\"geojson\"}]}");

        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

        simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/slider/admin/location/SET", "[{\"name\":\"value\",\"type\":\"string\",\"value\":\"44.0:5.7\"}]", "POST");
        System.out.println(simulated3);
        Thread.sleep(1000);

        simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?geojson={'latitude':45.2,'longitude':5.7,'distance':1000}", null, "GET");
        System.out.println(simulated3);
        response = new JSONObject("{\"providers\":[{\"name\":\"light\",\"location\":\"45.2:5.7\",\"services\":" + "[{\"name\":\"admin\",\"resources\":[" + "{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"switch\",\"resources\":[" + "{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"}," + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"}," + "{\"name\":\"turn_on\",\"type\":\"ACTION\"}," + "{\"name\":\"turn_off\",\"type\":\"ACTION\"}," + "{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}],\"type\":\"COMPLETE_LIST\"," + "\"uri\":\"/\",\"statusCode\":200}");
        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);
  
    }

    @Test
    public void testWsFiltered() throws Exception {
        WsServiceTestClient client = new WsServiceTestClient();
        new Thread(client).start();
        String simulated3 = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"geojson\",\"type\":\"string\",\"value\":\"{'latitude':45.2,'longitude':5.7,'distance':1000}\"}]");
        System.out.println(simulated3);
        
        JSONObject response = new JSONObject("{\"providers\":[{\"name\":\"slider\",\"location\":\"45.2:5.7\"," + "\"services\":[{\"name\":\"admin\",\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"cursor\",\"resources\":" + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]}]}," + "{\"name\":\"light\",\"location\":\"45.2:5.7\",\"services\":" + "[{\"name\":\"admin\",\"resources\":[" + "{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"switch\",\"resources\":[" + "{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"}," + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"}," + "{\"name\":\"turn_on\",\"type\":\"ACTION\"}," + "{\"name\":\"turn_off\",\"type\":\"ACTION\"}," + "{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}]," + "\"filters\":[{\"definition\":\"{'latitude':45.2,'longitude':5.7,'distance':1000}\"," + "\"type\":\"geojson\"}],\"type\":\"COMPLETE_LIST\"," + "\"uri\":\"/\",\"statusCode\":200}");

        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

        simulated3 = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"geojson\",\"type\":\"string\",\"value\":\"{'latitude':45.2,'longitude':5.7,'distance':1000,'output':true}\"}]");
        System.out.println(simulated3);
        
        response = new JSONObject("{\"providers\":" + "{\"type\": \"FeatureCollection\", \"features\": [" + "{\"properties\":{\"name\":\"slider\"},\"type\":\"Feature\",\"geometry\":" + "{\"type\":\"Point\",\"coordinates\":[45.2,5.7]}}," + "{\"properties\":{\"name\":\"light\"},\"type\":\"Feature\",\"geometry\":" + "{\"type\":\"Point\",\"coordinates\":[45.2,5.7]}}]}," + "\"statusCode\":200,\"type\":\"COMPLETE_LIST\"," + "\"uri\":\"/\",\"filters\":[{\"definition\":" + "\"{'latitude':45.2,'longitude':5.7,'distance':1000,'output':true}\"," + "\"type\":\"geojson\"}]}");

        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);
        simulated3 = this.synchronizedRequest(client, "/sensinact/slider/admin/location/SET", "[{\"name\":\"value\",\"type\":\"string\",\"value\":\"44.0:5.7\"}]");
        System.out.println(simulated3);
        Thread.sleep(1000);

        simulated3 = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"geojson\",\"type\":\"string\",\"value\":\"{'latitude':45.2,'longitude':5.7,'distance':1000}\"}]");
        System.out.println(simulated3);
        
        response = new JSONObject("{\"providers\":[{\"name\":\"light\",\"location\":\"45.2:5.7\",\"services\":" + "[{\"name\":\"admin\",\"resources\":[" + "{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"switch\",\"resources\":[" + "{\"name\":\"status\",\"type\":\"STATE_VARIABLE\"}," + "{\"name\":\"brightness\",\"type\":\"STATE_VARIABLE\"}," + "{\"name\":\"turn_on\",\"type\":\"ACTION\"}," + "{\"name\":\"turn_off\",\"type\":\"ACTION\"}," + "{\"name\":\"dim\",\"type\":\"ACTION\"}]}]}],\"type\":\"COMPLETE_LIST\"," + "\"uri\":\"/\",\"statusCode\":200}");
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
