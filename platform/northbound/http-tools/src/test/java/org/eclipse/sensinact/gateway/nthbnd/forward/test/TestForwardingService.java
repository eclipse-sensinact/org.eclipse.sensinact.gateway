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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.skyscreamer.jsonassert.JSONAssert;

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

    protected static final String HTTP_ROOTURL = "http://localhost:8899";
    protected static final String WS_ROOTURL = "ws://localhost:8899";

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
        configuration.put("org.apache.felix.http.jettyEnabled", "true");
        configuration.put("org.apache.felix.http.whiteboardEnabled", "true");

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
    

    @Test
    public void testCallbackHttpAndWebSocket() throws Exception {
        Mediator mediator = new Mediator(context);
        this.initializeMoke(new File("./extra-src3/test/resources/MANIFEST.MF"), new File("./extra-src3/test/resources/meta"), new File("./target/extra-test-classes3"));
        String simulated1 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/callbackTest1", null, "GET");
        System.out.println(simulated1);
        
        assertEquals("[GET]/callbackTest1", simulated1);            
        String simulated2 = HttpServiceTestClient.newRequest(mediator, HTTP_ROOTURL + "/callbackTest1/withContent", "MyContent", "POST");
        System.out.println(simulated2);
        assertEquals("[POST]/callbackTest1/withContent", simulated2);
        
        WsServiceTestClient client = new WsServiceTestClient();
        new Thread(client).start();
        
        simulated1 = this.synchronizedRequest(client, "/callbackTest1", null);
        System.out.println(simulated1);
        assertEquals("[WEBSOCKET]/callbackTest1", simulated1); 

        simulated1 = this.synchronizedRequest(client, "/callbackTest1/withContent", "{\"request\":\"MyContent\"}");
        System.out.println(simulated1);
        assertEquals("[WEBSOCKET]/callbackTest1/withContent", simulated1); 

        simulated1 = this.synchronizedRequest(client, null, null);
        System.out.println(simulated1);
        assertEquals("[WEBSOCKET]", simulated1); 
    }


    private String synchronizedRequest(WsServiceTestClient client, String url, String content) {
        String simulated = null;
        long wait = 10000;
        client.newRequest(url, content);

        while (!client.isAvailable() && wait > 0) {
            wait-=100;
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

    private void initializeMoke(File manifestFile, File... sourceDirectories) throws Exception {
        File tmpDirectory = new File("./target/felix/tmp");
        if(!tmpDirectory.exists()) {
        	tmpDirectory.mkdir();
        } else {
        	new File(tmpDirectory, "dynamicBundle.jar").delete();
        }
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
        Thread.sleep(5000);
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