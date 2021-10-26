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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.InstalledBundleExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ExtendWith(InstalledBundleExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(BundleContextExtension.class)
public class TestLdapFiltering {
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
    public void testLdapFilter(
    			@InjectInstalledBundle(value = "extra.jar", start = true) Bundle bundle,
    			@InjectBundleContext BundleContext context
    		) throws Exception {
//        File tmpDirectory = new File("./target/felix/tmp");
//
//        new File(tmpDirectory, "props.xml").delete();
//        new File(tmpDirectory, "resources.xml").delete();
//        new File(tmpDirectory, "dynamicBundle.jar").delete();
//
//        super.createDynamicBundle(new File("./extra-src/test/resources/MANIFEST.MF"), tmpDirectory, new File("./extra-src/test/resources/meta"), new File("./src/test/resources/resources.xml"), new File("./target/extra-test-classes"));
//
//        super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL()).start();
        Thread.sleep(5000);

        Mediator mediator = new Mediator(context);
        String simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.humidity.accessible=false)'", null, "GET");

        System.out.println(simulated1);

        assertTrue(new JSONObject(simulated1).getJSONArray("providers").length() == 0);

        String simulated2 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/sensor0/service1/humidity/SET", "[{\"name\":\"attributeName\",\"type\":\"string\", \"value\":\"accessible\"}," + "{\"name\":\"value\",\"type\":\"boolean\", \"value\":false}]", "POST");
        System.out.println(simulated2);

        Thread.sleep(2000);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.humidity.accessible=false)'", null, "GET");
        assertTrue(new JSONObject(simulated1).getJSONArray("providers").length() == 1);
        assertEquals("sensor0", new JSONObject(simulated1).getJSONArray("providers").getJSONObject(0).getString("name"));

        System.out.println(simulated1);
        String simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/sensor3/service1/humidity/SET", "[{\"name\":\"attributeName\",\"type\":\"string\", \"value\":\"accessible\"}," + "{\"name\":\"value\",\"type\":\"boolean\", \"value\":false}]", "POST");
        System.out.println(simulated3);

        Thread.sleep(2000);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.humidity.accessible=false)'", null, "GET");

        System.out.println(simulated1);
        List<String> list = new ArrayList<String>();
        list.add(new JSONObject(simulated1).getJSONArray("providers").getJSONObject(0).getString("name"));
        list.add(new JSONObject(simulated1).getJSONArray("providers").getJSONObject(1).getString("name"));

        assertTrue(new JSONObject(simulated1).getJSONArray("providers").length() == 2);
        assertTrue(list.contains("sensor0"));
        assertTrue(list.contains("sensor3"));
        String simulated4 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/sensor3/service1/temperature/SET", "[{\"name\":\"value\",\"type\":\"float\", \"value\":2.5}]", "POST");

        System.out.println(simulated4);

        Thread.sleep(2000);
        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.temperature.value%20<=%204.0)'", null, "GET");

        System.out.println(simulated1);
        assertTrue(new JSONObject(simulated1).getJSONArray("providers").length() == 1);
        assertEquals("sensor3", new JSONObject(simulated1).getJSONArray("providers").getJSONObject(0).getString("name"));

        String simulated5 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/sensor2/service1/temperature/SET", "[{\"name\":\"value\",\"type\":\"float\", \"value\":2.5}]", "POST");
        System.out.println(simulated5);
        Thread.sleep(2000);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.temperature.value<=4.0)'", null, "GET");

        list = new ArrayList<String>();
        list.add(new JSONObject(simulated1).getJSONArray("providers").getJSONObject(0).getString("name"));
        list.add(new JSONObject(simulated1).getJSONArray("providers").getJSONObject(1).getString("name"));

        assertTrue(new JSONObject(simulated1).getJSONArray("providers").length() == 2);
        assertTrue(list.contains("sensor2"));
        assertTrue(list.contains("sensor3"));
        System.out.println(simulated1);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(%26(service1.humidity.accessible=false)(service1.temperature.value<=4))'", null, "GET");
        assertTrue(new JSONObject(simulated1).getJSONArray("providers").length() == 1);
        assertEquals("sensor3", new JSONObject(simulated1).getJSONArray("providers").getJSONObject(0).getString("name"));

        System.out.println(simulated1);
    }
}
