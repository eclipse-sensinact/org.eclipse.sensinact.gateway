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
package org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.test;

import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.ws.test.WsServiceTestClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
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
    public void testHttpFiltered() throws Exception {

        String simulated3 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact?jsonpath=$.[?(@.name=='slider')]", null, "GET");
        JSONObject response = new JSONObject("{\"filters\":[{\"definition\":\"$.[?(@.name=='slider')]\",\"type\":\"jsonpath\"}]," + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"cursor\",\"resources\":" + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" + "}]" + ",\"location\":\"45.2:5.7\"}]}");
        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

        String simulated1 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact/providers", null, "GET");
        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"slider\",\"light\"]," + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        JSONAssert.assertEquals(response, new JSONObject(simulated1), false);

        String simulated2 = HttpServiceTestClient.newRequest( HTTP_ROOTURL + "/sensinact/providers?jsonpath=$.[:1]", null, "GET");
        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"" + new JSONObject(simulated1).getJSONArray("providers").getString(0) + "\"]," + "\"filters\":[{\"type\":\"jsonpath\", \"definition\":\"$.[:1]\"}], " + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        JSONAssert.assertEquals(response, new JSONObject(simulated2), false);
    }

    @Test
    public void testWsFiltered() throws Exception {
        JSONObject response;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        String simulated3 = this.synchronizedRequest(client, "/sensinact", "[{\"name\":\"jsonpath\",\"type\":\"string\",\"value\":\"$.[?(@.name=='slider')]\"}]");

        //System.out.println(simulated3);

        response = new JSONObject("{\"filters\":[{\"definition\":\"$.[?(@.name=='slider')]\",\"type\":\"jsonpath\"}]," + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"cursor\"," + "\"resources\":" + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]}]" + ",\"location\":\"45.2:5.7\"}]}");
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
