/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.InstalledBundleExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

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
    
    private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();

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
                    builder.setContent(content);
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
		@SuppressWarnings("serial")
    	Map<String, Object> map = new HashMap<String, Object>() {
    		{
    			put("serviceProviderId", "TestForSensiNactGateway");
    			put("serviceId", "service1");
    			put("resourceId", "temperature");
    			put("data", 24);
    		}
    	};
        callback.setRemoteEntity(mapper.convertValue(map, JsonObject.class));

        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway");
        Service service = provider.getService("service1");
        Resource variable = service.getResource("temperature");
//        MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);

        SnaMessage response = variable.get(DataResource.VALUE, (Object[]) null);
        
//        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});

        JsonObject jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(24, (int) jsonObject.getJsonObject("response").getInt("value"));

        response = variable.set(DataResource.VALUE, 25, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(setMethod, new Object[]{DataResource.VALUE, 25, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(25, (int) jsonObject.getJsonObject("response").getInt("value"));

        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(25, (int) jsonObject.getJsonObject("response").getInt("value"));
        core.close();
    }

	@Test
    public void testHttpTaskWithProcessingContext(@InjectService(timeout = 500) Core core, @InjectInstalledBundle(value = "resources5.jar", start = true) Bundle bundle) throws Throwable {
		@SuppressWarnings("serial")
    	Map<String, Object> map = new HashMap<String, Object>() {
    		{
    			put("serviceProviderId", "TestForSensiNactGateway5");
    			put("serviceId", "service1");
    			put("resourceId", "temperature");
    			put("data", 24);
    		}
    	};
    	
    	callback.setRemoteEntity(mapper.convertValue(map, JsonObject.class));

//        this.initializeMoke(new File("src/test/resources/resources5.xml").toURI().toURL(), new File("./extra-src5/test/resources/MANIFEST.MF"), new File("./extra-src5/test/resources/meta"), new File("./target/extra-test-classes5"));

//        Core core = mid.buildProxy();

        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway5");
        Service service = provider.getService("service1");
        Resource variable = service.getResource("temperature");

        SnaMessage response = variable.get(DataResource.VALUE, (Object[]) null);
//        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        JsonObject jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(24, (int) jsonObject.getJsonObject("response").getInt("value"));

        response = variable.set(DataResource.VALUE, 25, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(setMethod, new Object[]{DataResource.VALUE, 25, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(25, (int) jsonObject.getJsonObject("response").getInt("value"));

        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(25, (int) jsonObject.getJsonObject("response").getInt("value"));

        core.close();
    }

    @Test
    public void testHttpTaskWithServicesEnumeration(@InjectService(timeout = 500) Core core, @InjectInstalledBundle(value = "resources4.jar", start = true) Bundle bundle) throws Throwable {
    	@SuppressWarnings("serial")
    	Map<String, Object> map = new HashMap<String, Object>() {
    		{
    			put("serviceProviderId", "TestForSensiNactGateway4");
    			put("serviceId", "service1");
    			put("resourceId", "temperature");
    			put("data", 24);
    		}
    	};
        callback.setRemoteEntity(mapper.convertValue(map, JsonObject.class));

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
        JsonObject jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(24, (int) jsonObject.getJsonObject("response").getInt("value"));

        response = variable.set(DataResource.VALUE, 25, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(setMethod, new Object[]{DataResource.VALUE, 25, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(25, (int) jsonObject.getJsonObject("response").getInt("value"));

        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(25, (int) jsonObject.getJsonObject("response").getInt("value"));

        core.close();
    }

    @Test
    public void testChainedHttpTask(@InjectService(timeout = 500) Core core, @InjectInstalledBundle(value = "resources3.jar", start = true) Bundle bundle) throws Throwable {
        
    	@SuppressWarnings("serial")
    	Map<String, Object> map = new HashMap<String, Object>() {
    		{
    			put("serviceProviderId", "TestForSensiNactGateway3");
    			put("serviceId", "service1");
    			put("resourceId", "temperature");
    			put("data", 24);
    		}
    	};
        callback.setRemoteEntity(mapper.convertValue(map, JsonObject.class));

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
        JsonObject jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(24, (int) jsonObject.getJsonObject("response").getInt("value"));

        core.close();
    }

    @Test
    public void testHttpDeviceReccurrent(
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(value = "resources2.jar") Bundle bundle) throws Throwable {
    	
    	CountDownLatch latch = new CountDownLatch(1);
    	
    	callback.setCountDownLatch(latch);
    	@SuppressWarnings("serial")
    	Map<String, Object> map = new HashMap<String, Object>() {
    		{
    			put("serviceProviderId", "TestForSensiNactGateway2");
    			put("serviceId", "service1");
    			put("resourceId", "temperature");
    			put("data", 24);
    		}
    	};
        callback.setRemoteEntity(mapper.convertValue(map, JsonObject.class));        
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

        JsonObject jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(24, (int) jsonObject.getJsonObject("response").getInt("value"));

    	map.put("data", 25);
        callback.setRemoteEntity(mapper.convertValue(map, JsonObject.class));

        Thread.sleep(2000);
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(25, (int) jsonObject.getJsonObject("response").getInt("value"));
        
    	map.put("data", 32);
        callback.setRemoteEntity(mapper.convertValue(map, JsonObject.class));
        
        Thread.sleep(2000);
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(32, (int) jsonObject.getJsonObject("response").getInt("value"));
        Thread.sleep(16 * 1000);

        map.put("data", 45);
        callback.setRemoteEntity(mapper.convertValue(map, JsonObject.class));
        
        Thread.sleep(2000);
        response = variable.get(DataResource.VALUE, (Object[]) null);
//        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        jsonObject = mapper.readValue(response.getJSON(), JsonObject.class);
        assertEquals(32, (int) jsonObject.getJsonObject("response").getInt("value"));
        
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
    
    }

}