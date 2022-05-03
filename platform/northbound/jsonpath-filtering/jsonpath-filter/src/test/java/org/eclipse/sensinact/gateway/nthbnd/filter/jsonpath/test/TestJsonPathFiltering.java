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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.ws.test.WsServiceTestClient;
import org.json.JSONObject;
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


    @Test
    public void testHttpFiltered(@InjectBundleContext BundleContext context) throws Exception {
    	Mediator mediator = new Mediator(context);
		String location = ModelInstance.defaultLocation(mediator);
        String simulated3 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact?jsonpath=$.[?(@.name=='slider')]", null, "GET");
        JSONObject response = new JSONObject(
        "{\"filters\":[{\"definition\":\"$.[?(@.name=='slider')]\",\"type\":\"jsonpath\"}]," 
        + "\"providers\":" 
        + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" 
        + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\",\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" 
        + "}]" 
        + ",\"location\":\""+location.replace("\"", "\\\"")+"\"}]}");
        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

        String simulated1 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact/providers", null, "GET");
        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"slider\",\"light\"]," 
        + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        JSONAssert.assertEquals(response, new JSONObject(simulated1), false);

        String simulated2 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact/providers?jsonpath=$.[:1]", null, "GET");
        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"" + new JSONObject(simulated1).getJSONArray("providers").getString(0) + "\"]," + "\"filters\":[{\"type\":\"jsonpath\", \"definition\":\"$.[:1]\"}], " + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        JSONAssert.assertEquals(response, new JSONObject(simulated2), false);
    }

    @Test
    public void testWsFiltered(@InjectBundleContext BundleContext context) throws Exception {
    	Mediator mediator = new Mediator(context);
		String location = ModelInstance.defaultLocation(mediator);
        JSONObject response;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        String simulated3 = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"jsonpath\",\"type\":\"string\",\"value\":\"$.[?(@.name=='slider')]\"}]");

        //System.out.println(simulated3);

        response = new JSONObject("{\"filters\":[{\"definition\":\"$.[?(@.name=='slider')]\",\"type\":\"jsonpath\"}]," 
        + "\"providers\":" 
        + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," 
        + "\"resources\":" 
        + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," 
        + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," 
        + "{\"name\":\"cursor\"," 
        + "\"resources\":" 
        + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]}]" 
        + ",\"location\":\""+location.replace("\"", "\\\"")+"\"}]}");
        JSONObject obj = new JSONObject(simulated3);
        JSONAssert.assertEquals(response, obj, false);

        String simulated1 = this.synchronizedRequest(client, "/sensinact/providers", null);

        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"slider\",\"light\"]," + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        //System.out.println(simulated1);
        obj = new JSONObject(simulated1);
        obj.remove("X-Auth-Token");
        JSONAssert.assertEquals(response, obj, false);
        String simulated2 = this.synchronizedRequest(client, "/sensinact/providers", "[{\"name\":\"jsonpath\",\"type\":\"string\",\"value\":\"$.[:1]\"}]");
        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"" + new JSONObject(simulated1).getJSONArray("providers").getString(0) + "\"]," + "\"filters\":[{\"type\":\"jsonpath\", \"definition\":\"$.[:1]\"}], " + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");

        obj = new JSONObject(simulated2);
        obj.remove("X-Auth-Token");

        //System.out.println(simulated2);
        JSONAssert.assertEquals(response, obj, false);

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
