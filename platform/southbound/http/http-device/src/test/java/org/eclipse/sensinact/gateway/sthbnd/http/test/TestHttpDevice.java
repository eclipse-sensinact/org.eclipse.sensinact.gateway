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
package org.eclipse.sensinact.gateway.sthbnd.http.test;

import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.InstalledBundleExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(InstalledBundleExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestHttpDevice {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    public static int HTTP_PORT = 8898;
    public static int SERVER_PORT = 8899;
    public static String HTTP_ROOTURL = "http://127.0.0.1:" + HTTP_PORT;

    public static String newRequest(String configuration) throws IOException {
        SimpleResponse response;
        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>(configuration);
        SimpleRequest request = new SimpleRequest(builder);
        response = request.send();
        byte[] responseContent = response.getContent();
        String contentStr = (responseContent == null ? null : new String(responseContent));
        return contentStr;
    }

    public static String newRequest(String url, String content, String method) {
        SimpleResponse response;
        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
        builder.setUri(url);
        try {
            if (method.equals("GET")) 
                builder.setHttpMethod("GET");
            else if (method.equals("POST")) {
                builder.setContentType("application/json");
                builder.setHttpMethod("POST");
                if (content != null && content.length() > 0) {
                    JSONObject jsonData = new JSONObject(content);
                    builder.setContent(jsonData.toString());
                }
            } else 
                return null;
            builder.setAccept("application/json");
            SimpleRequest request = new SimpleRequest(builder);
            response = request.send();
            byte[] responseContent = response.getContent();
            String contentStr = (responseContent == null ? null : new String(responseContent));
            return contentStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JettyTestServer server = null;
    private static JettyServerTestCallback callback = null;

    @BeforeAll
    public static void beforeClass() throws Exception {
        if (server != null) {
            if (server.isStarted()) {
                server.stop();
                server.join();
            }
            server = null;
        }
        server = new JettyTestServer(SERVER_PORT);
        new Thread(server).start();

        server.join();
        callback = new JettyServerTestCallback();
        server.registerCallback(callback);
    }

    @AfterAll
    public static void afterClass() throws Exception {
        server.stop();
        server.join();
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

//    Method getDescription = null;
//    Method getMethod = null;
//    Method setMethod = null;
//    Method actMethod = null;
//
//    public TestHttpDevice() throws Exception {
//        super();
//        getDescription = Describable.class.getDeclaredMethod("getDescription");
//        getMethod = Resource.class.getDeclaredMethod("get", new Class<?>[]{String.class, Object[].class});
//        setMethod = Resource.class.getDeclaredMethod("set", new Class<?>[]{String.class, Object.class, Object[].class});
//        actMethod = ActionResource.class.getDeclaredMethod("act", new Class<?>[]{Object[].class});
//    }

    public boolean isExcluded(String fileName) {
        if ("org.apache.felix.framework.security.jar".equals(fileName)) {
            return true;
        }
        return false;
    }

    @Test
    public void testHttpTask(
    		@InjectService(timeout = 500) Core core, 
    		@InjectInstalledBundle(value = "resources.jar", start = true) Bundle bundle
    		) throws Throwable {
        callback.setRemoteEntity(new JSONObject().put("serviceProviderId", "TestForSensiNactGateway").put("serviceId", "service1").put("resourceId", "temperature").put("data", 24));

        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway");
        Service service = provider.getService("service1");
        Resource variable = service.getResource("temperature");
//        MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);

        SnaMessage response = variable.get(DataResource.VALUE, (Object[]) null);
        
//        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});

        JSONObject jsonObject = new JSONObject(response.getJSON());
        assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));

        response = variable.set(DataResource.VALUE, 25, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(setMethod, new Object[]{DataResource.VALUE, 25, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));

        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
        core.close();
    }

    @Test
    public void testHttpTaskWithProcessingContext(@InjectService(timeout = 500) Core core, @InjectInstalledBundle(value = "resources5.jar", start = true) Bundle bundle) throws Throwable {
        callback.setRemoteEntity(new JSONObject().put("serviceProviderId", "TestForSensiNactGateway5").put("serviceId", "service1").put("resourceId", "temperature").put("data", 24));

//        this.initializeMoke(new File("src/test/resources/resources5.xml").toURI().toURL(), new File("./extra-src5/test/resources/MANIFEST.MF"), new File("./extra-src5/test/resources/meta"), new File("./target/extra-test-classes5"));

//        Core core = mid.buildProxy();

        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway5");
        Service service = provider.getService("service1");
        Resource variable = service.getResource("temperature");

        SnaMessage response = variable.get(DataResource.VALUE, (Object[]) null);
//        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        JSONObject jsonObject = new JSONObject(response.getJSON());
        assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));

        response = variable.set(DataResource.VALUE, 25, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(setMethod, new Object[]{DataResource.VALUE, 25, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));

        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));

        core.close();
    }

    @Test
    public void testHttpTaskWithServicesEnumeration(@InjectService(timeout = 500) Core core, @InjectInstalledBundle(value = "resources4.jar", start = true) Bundle bundle) throws Throwable {
        callback.setRemoteEntity(new JSONObject().put("serviceProviderId", "TestForSensiNactGateway4").put("serviceId", "service1").put("resourceId", "temperature").put("data", 24));

        //        this.initializeMoke(new File("src/test/resources/resources4.xml").toURI().toURL(), new File("./extra-src4/test/resources/MANIFEST.MF"), new File("./extra-src4/test/resources/meta"), new File("./target/extra-test-classes4"));
//        Thread.sleep(1000);
//        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
//        Core core = mid.buildProxy();

        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway4");
        provider.getServices().forEach(s -> System.err.println(s.getName()));
        Service service = provider.getService("service1");
        Resource variable = service.getResource("temperature");

        SnaMessage response = variable.get(DataResource.VALUE, (Object[]) null);
//        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        JSONObject jsonObject = new JSONObject(response.getJSON());
        assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));

        response = variable.set(DataResource.VALUE, 25, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(setMethod, new Object[]{DataResource.VALUE, 25, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));

        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));

        core.close();
    }

    @Test
    public void testChainedHttpTask(@InjectService(timeout = 500) Core core, @InjectInstalledBundle(value = "resources3.jar", start = true) Bundle bundle) throws Throwable {
        callback.setRemoteEntity(new JSONObject().put("serviceProviderId", "TestForSensiNactGateway3").put("serviceId", "service1").put("resourceId", "temperature").put("data", 24));

//        this.initializeMoke(new File("src/test/resources/resources3.xml").toURI().toURL(), new File("./extra-src3/test/resources/MANIFEST.MF"), new File("./extra-src3/test/resources/meta"), new File("./target/extra-test-classes3"));

//        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
//        Core core = mid.buildProxy();

        Session session = core.getAnonymousSession();
        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway3");
        Service service = provider.getService("service1");
        Resource variable = service.getResource("temperature");
//        MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);

        SnaMessage response = variable.get(DataResource.VALUE, (Object[]) null);
//        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        JSONObject jsonObject = new JSONObject(response.getJSON());
        assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));

        core.close();
    }

    @Test
    public void testHttpDeviceReccurrent(
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(value = "resources2.jar") Bundle bundle) throws Throwable {
    	
    	CountDownLatch latch = new CountDownLatch(1);
    	
    	callback.setCountDownLatch(latch);
    	callback.setRemoteEntity(new JSONObject(
    			).put("serviceProviderId", "TestForSensiNactGateway2"
    			).put("serviceId", "service1"
    			).put("resourceId", "temperature"
    			).put("data", 24));
        
        bundle.start();

        assertTrue(latch.await(5, TimeUnit.SECONDS), "GET was never called");

//        this.initializeMoke(new File("src/test/resources/resources2.xml").toURI().toURL(), new File("./extra-src2/test/resources/MANIFEST.MF"), new File("./extra-src2/test/resources/meta"), new File("./target/extra-test-classes2"));
//        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
//        Core core = mid.buildProxy();

        Session session = core.getAnonymousSession();
        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway2");
        Service service = provider.getService("service1");
        Resource variable = service.getResource("temperature");
//        MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);
        
        SnaMessage response = variable.get(DataResource.VALUE, (Object[]) null);
//        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});

        JSONObject jsonObject = new JSONObject(response.getJSON());
        assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));

        callback.setRemoteEntity(new JSONObject(
        		).put("serviceProviderId", "TestForSensiNactGateway2"
        		).put("serviceId", "service1"
        		).put("resourceId", "temperature"
        		).put("data", 25));

        Thread.sleep(2000);
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
        
        callback.setRemoteEntity(new JSONObject(
        		).put("serviceProviderId", "TestForSensiNactGateway2"
        		).put("serviceId", "service1"
        		).put("resourceId", "temperature"
        		).put("data", 32));
        
        Thread.sleep(2000);
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(32, (int) jsonObject.getJSONObject("response").getInt("value"));
        Thread.sleep(16 * 1000);

        callback.setRemoteEntity(new JSONObject(
        		).put("serviceProviderId", "TestForSensiNactGateway2"
        		).put("serviceId", "service1"
        		).put("resourceId", "temperature"
        		).put("data", 45));
        
        Thread.sleep(2000);
        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(32, (int) jsonObject.getJSONObject("response").getInt("value"));
        
        core.close();
    }

    protected void doInit(Map configuration) {
       
        configuration.put("felix.auto.start.4", 
        		"file:target/felix/bundle/dynamicBundle.jar ");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");

        configuration.put("org.eclipse.sensinact.gateway.location.latitude", "45.2d");
        configuration.put("org.eclipse.sensinact.gateway.location.longitude", "5.7d");

        configuration.put("org.osgi.service.http.port", "8898");
        configuration.put("org.apache.felix.http.jettyEnabled", true);
        configuration.put("org.apache.felix.http.whiteboardEnabled", true);

//        try {
//        	String fileName = "sensinact.config";
//            File testFile = new File(new File("src/test/resources"), fileName);
//            URL testFileURL = testFile.toURI().toURL();
//            FileOutputStream output = new FileOutputStream(new File(loadDir,fileName));
//            byte[] testCng = IOUtils.read(testFileURL.openStream(), true);
//            IOUtils.write(testCng, output);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    
    }

//    private void initializeMoke(URL resource, File manifestFile, File... sourceDirectories) throws Exception {
//        File tmpDirectory = new File("./target/felix/tmp");
//        if(!tmpDirectory.exists()) {
//        	tmpDirectory.mkdir();
//        } else {
//        	new File(tmpDirectory, "resources.xml").delete();
//        	new File(tmpDirectory, "dynamicBundle.jar").delete();
//        }
//        FileOutputStream output = null;
//        byte[] resourcesBytes = IOUtils.read(resource.openStream());
//        output = new FileOutputStream(new File(tmpDirectory, "resources.xml"));
//        IOUtils.write(resourcesBytes, output);
//
//        int length = (sourceDirectories == null ? 0 : sourceDirectories.length);
//        File[] sources = new File[length + 1];
//        int index = 0;
//        if (length > 0) {
//            for (; index < length; index++) {
//                sources[index] = sourceDirectories[index];
//            }
//        }
//        sources[index] = new File(tmpDirectory, "resources.xml");
//        super.createDynamicBundle(manifestFile, tmpDirectory, sources);
//
//        Bundle bundle = super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL());
//
//        ClassLoader current = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(super.classloader);
//        try {
//            bundle.start();
//
//        } finally {
//            Thread.currentThread().setContextClassLoader(current);
//        }
//        Thread.sleep(5000);
//    }

}