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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.InstalledBundleExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.json.spi.JsonProvider;

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

    private final JsonProvider json = JsonProviderFactory.getProvider();
    
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

        assertTrue(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").size() == 0);

        String simulated2 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/sensor0/service1/humidity/SET", "[{\"name\":\"attributeName\",\"type\":\"string\", \"value\":\"accessible\"}," + "{\"name\":\"value\",\"type\":\"boolean\", \"value\":false}]", "POST");
        System.out.println(simulated2);

        Thread.sleep(2000);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.humidity.accessible=false)'", null, "GET");
        assertTrue(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").size() == 1);
        assertEquals("sensor0", json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getJsonObject(0).getString("name"));

        System.out.println(simulated1);
        String simulated3 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/sensor3/service1/humidity/SET", "[{\"name\":\"attributeName\",\"type\":\"string\", \"value\":\"accessible\"}," + "{\"name\":\"value\",\"type\":\"boolean\", \"value\":false}]", "POST");
        System.out.println(simulated3);

        Thread.sleep(2000);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.humidity.accessible=false)'", null, "GET");

        System.out.println(simulated1);
        List<String> list = new ArrayList<String>();
        list.add(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getJsonObject(0).getString("name"));
        list.add(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getJsonObject(1).getString("name"));

        assertTrue(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").size() == 2);
        assertTrue(list.contains("sensor0"));
        assertTrue(list.contains("sensor3"));
        String simulated4 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/sensor3/service1/temperature/SET", "[{\"name\":\"value\",\"type\":\"float\", \"value\":2.5}]", "POST");

        System.out.println(simulated4);

        Thread.sleep(2000);
        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.temperature.value%20<=%204.0)'", null, "GET");

        System.out.println(simulated1);
        assertTrue(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").size() == 1);
        assertEquals("sensor3", json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getJsonObject(0).getString("name"));

        String simulated5 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/sensor2/service1/temperature/SET", "[{\"name\":\"value\",\"type\":\"float\", \"value\":2.5}]", "POST");
        System.out.println(simulated5);
        Thread.sleep(2000);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(service1.temperature.value<=4.0)'", null, "GET");

        list = new ArrayList<String>();
        list.add(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getJsonObject(0).getString("name"));
        list.add(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getJsonObject(1).getString("name"));

        assertTrue(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").size() == 2);
        assertTrue(list.contains("sensor2"));
        assertTrue(list.contains("sensor3"));
        System.out.println(simulated1);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact?ldap='(%26(service1.humidity.accessible=false)(service1.temperature.value<=4))'", null, "GET");
        assertTrue(json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").size() == 1);
        assertEquals("sensor3", json.createReader(new StringReader(simulated1)).readObject().getJsonArray("providers").getJsonObject(0).getString("name"));

        System.out.println(simulated1);
    }
}
