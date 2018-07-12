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
package org.eclipse.sensinact.gateway.nthbnd.forward.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TestForwardingService extends MidOSGiTest {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//

    protected static final String HTTP_ROOTURL = "http://localhost:8095";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     * @throws MalformedURLException
     * @throws IOException
     */
    public TestForwardingService() throws Exception {
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
    protected void doInit(Map configuration) {
        configuration.put("felix.auto.start.1", "file:target/felix/bundle/org.osgi.compendium.jar " + "file:target/felix/bundle/org.apache.felix.configadmin.jar " + "file:target/felix/bundle/org.apache.felix.framework.security.jar ");
        configuration.put("felix.auto.install.2", "file:target/felix/bundle/sensinact-utils.jar " + "file:target/felix/bundle/sensinact-common.jar " + "file:target/felix/bundle/sensinact-datastore-api.jar " + "file:target/felix/bundle/sensinact-framework-extension.jar " + "file:target/felix/bundle/sensinact-security-none.jar " + "file:target/felix/bundle/sensinact-generic.jar");

        configuration.put("felix.auto.start.2", "file:target/felix/bundle/sensinact-test-configuration.jar " + "file:target/felix/bundle/sensinact-signature-validator.jar " + "file:target/felix/bundle/sensinact-core.jar ");
        configuration.put("felix.auto.start.3", "file:target/felix/bundle/javax.servlet-api.jar " + "file:target/felix/bundle/org.apache.felix.http.api.jar " + "file:target/felix/bundle/org.apache.felix.http.jetty.jar " + "file:target/felix/bundle/http.jar " + "file:target/felix/bundle/sensinact-northbound-access.jar " + "file:target/felix/bundle/rest-access.jar " + "file:target/felix/bundle/dynamicBundle.jar");
        configuration.put("felix.auto.start.4", "file:target/felix/bundle/slider.jar " + "file:target/felix/bundle/light.jar ");
        configuration.put("felix.log.level", "4");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");
        configuration.put("org.osgi.service.http.port", "8095");
        configuration.put("org.apache.felix.http.jettyEnabled", "true");
    }

    @Test
    public void testForwarding() throws Exception {
        Mediator mediator = new Mediator(context);
        this.initializeMoke(new File("./extra-src/test/resources/MANIFEST.MF"), new File("./extra-src/test/resources/meta"), new File("./target/extra-test-classes"));

        String simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/providers", null, "GET");
        System.out.println(simulated1);

        String simulated2 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/forwardingTest1/0", null, "GET");
        System.out.println(simulated2);

        JSONAssert.assertEquals(new JSONObject(simulated1).getJSONArray("providers"), new JSONArray(simulated2), false);

        simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/sensinact/providers/slider", null, "GET");
        System.out.println(simulated1);

        simulated2 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/forwardingTest1/1", null, "GET");
        System.out.println(simulated2);

        JSONAssert.assertEquals(new JSONObject(simulated1), new JSONObject(simulated2), false);
    }

    @Test
    public void testCallback() throws Exception {
        Mediator mediator = new Mediator(context);
        this.initializeMoke(new File("./extra-src2/test/resources/MANIFEST.MF"), new File("./extra-src2/test/resources/meta"), new File("./target/extra-test-classes2"));
        try {
            String simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/callbackTest1", null, "GET");

            System.out.println(simulated1);
            assertEquals("[GET]/callbackTest1", simulated1);
            String simulated2 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/callbackTest1/withContent", "MyContent", "POST");

            System.out.println(simulated2);
            assertEquals("[POST]/callbackTest1/withContent", simulated2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeMoke(File manifestFile, File... sourceDirectories) throws Exception {
        File tmpDirectory = new File("./target/felix/tmp");
        new File(tmpDirectory, "dynamicBundle.jar").delete();

        int length = (sourceDirectories == null ? 0 : sourceDirectories.length);
        File[] sources = new File[length + 1];
        int index = 0;
        if (length > 0) {
            for (; index < length; index++) {
                sources[index] = sourceDirectories[index];
            }
        }
        sources[index] = new File(tmpDirectory, "resources.xml");
        super.createDynamicBundle(manifestFile, tmpDirectory, sources);

        Bundle bundle = super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL());

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(super.classloader);
        try {
            bundle.start();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        Thread.sleep(10 * 1000);
    }

}