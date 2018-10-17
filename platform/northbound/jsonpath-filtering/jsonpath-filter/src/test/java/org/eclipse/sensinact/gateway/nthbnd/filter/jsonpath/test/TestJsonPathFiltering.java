/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestJsonPathFiltering extends MidOSGiTest {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//

    protected static final String HTTP_ROOTURL = "http://localhost:8898";
    protected static final String WS_ROOTURL = "/sensinact";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     * @throws MalformedURLException
     * @throws IOException
     */
    public TestJsonPathFiltering() throws Exception {
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
        configuration.put("felix.auto.start.1", "file:target/felix/bundle/org.osgi.compendium.jar " + "file:target/felix/bundle/org.apache.felix.configadmin.jar " + "file:target/felix/bundle/org.apache.felix.framework.security.jar ");
        configuration.put("felix.auto.install.2", "file:target/felix/bundle/sensinact-utils.jar " + "file:target/felix/bundle/sensinact-common.jar " + "file:target/felix/bundle/sensinact-datastore-api.jar " + "file:target/felix/bundle/sensinact-framework-extension.jar " + "file:target/felix/bundle/sensinact-security-none.jar " + "file:target/felix/bundle/sensinact-generic.jar " + "file:target/felix/bundle/slf4j-api.jar " + "file:target/felix/bundle/slf4j-simple.jar");

        configuration.put("felix.auto.start.2", "file:target/felix/bundle/sensinact-test-configuration.jar " + "file:target/felix/bundle/sensinact-signature-validator.jar " + "file:target/felix/bundle/sensinact-core.jar ");
        configuration.put("felix.auto.start.3", "file:target/felix/bundle/javax.servlet-api.jar " + "file:target/felix/bundle/org.apache.felix.http.api.jar " + "file:target/felix/bundle/org.apache.felix.http.jetty.jar " + "file:target/felix/bundle/http.jar " + "file:target/felix/bundle/sensinact-northbound-access.jar " + "file:target/felix/bundle/rest-access.jar");
        configuration.put("felix.auto.install.4", "file:target/felix/bundle/json-provider-minimal.jar");
        configuration.put("felix.auto.start.4", "file:target/felix/bundle/json-path.jar " + "file:target/felix/bundle/dynamicBundle.jar " + "file:target/felix/bundle/slider.jar " + "file:target/felix/bundle/light.jar ");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");
        configuration.put("org.osgi.service.http.port", "8898");
        configuration.put("org.apache.felix.http.jettyEnabled", "true");
        configuration.put("org.apache.felix.http.whiteboardEnabled", "true");
    }

    @Test
    public void testHttpFiltered() throws Exception {
        Mediator mediator = new Mediator(context);
        String simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/jsonpath:sensinact?jsonpath=$.[?(@.name=='slider')]", null, "GET");
        JSONObject response = new JSONObject("{\"filters\":[{\"definition\":\"$.[?(@.name=='slider')]\",\"type\":\"jsonpath\"}]," + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"cursor\",\"resources\":" + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]" + "}]" + ",\"location\":\"45.19334890078532:5.706474781036377\"}]}");
        JSONAssert.assertEquals(response, new JSONObject(simulated3), false);

        String simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/providers", null, "GET");
        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"slider\",\"light\"]," + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        JSONAssert.assertEquals(response, new JSONObject(simulated1), false);

        String simulated2 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/jsonpath:sensinact/providers?jsonpath=$.[:1]", null, "GET");
        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"" + new JSONObject(simulated1).getJSONArray("providers").getString(0) + "\"]," + "\"filters\":[{\"type\":\"jsonpath\", \"definition\":\"$.[:1]\"}], " + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        JSONAssert.assertEquals(response, new JSONObject(simulated2), false);
    }

    @Test
    public void testWsFiltered() throws Exception {
        JSONObject response;
        WsServiceTestClient client = new WsServiceTestClient();

        new Thread(client).start();
        String simulated3 = this.synchronizedRequest(client, "/jsonpath:sensinact", "[{\"name\":\"jsonpath\",\"type\":\"string\",\"value\":\"$.[?(@.name=='slider')]\"}]");

        //System.out.println(simulated3);

        response = new JSONObject("{\"filters\":[{\"definition\":\"$.[?(@.name=='slider')]\",\"type\":\"jsonpath\"}]," + "\"providers\":" + "[{\"name\":\"slider\",\"services\":[{\"name\":\"admin\"," + "\"resources\":" + "[{\"name\":\"friendlyName\",\"type\":\"PROPERTY\"}," + "{\"name\":\"location\",\"type\":\"PROPERTY\"}," + "{\"name\":\"bridge\",\"type\":\"PROPERTY\"}," + "{\"name\":\"icon\",\"type\":\"PROPERTY\"}]}," + "{\"name\":\"cursor\"," + "\"resources\":" + "[{\"name\":\"position\",\"type\":\"SENSOR\"}]}]" + ",\"location\":\"45.19334890078532:5.706474781036377\"}]}");
        JSONObject obj = new JSONObject(simulated3);
        JSONAssert.assertEquals(response, obj, false);

        String simulated1 = this.synchronizedRequest(client, "/sensinact/providers", null);

        response = new JSONObject("{\"statusCode\":200,\"providers\":[\"slider\",\"light\"]," + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}");
        //System.out.println(simulated1);
        obj = new JSONObject(simulated1);
        obj.remove("X-Auth-Token");
        JSONAssert.assertEquals(response, obj, false);
        String simulated2 = this.synchronizedRequest(client, "/jsonpath:sensinact/providers", "[{\"name\":\"jsonpath\",\"type\":\"string\",\"value\":\"$.[:1]\"}]");
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
