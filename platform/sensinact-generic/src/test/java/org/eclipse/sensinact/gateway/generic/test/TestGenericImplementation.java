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
package org.eclipse.sensinact.gateway.generic.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.test.ProcessorService;
import org.eclipse.sensinact.gateway.test.StarterService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.InstalledBundleExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.skyscreamer.jsonassert.JSONAssert;

@ExtendWith(InstalledBundleExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestGenericImplementation {


    @Test
    public void testActionResourceModel(
    		@InjectService(cardinality = 0) ServiceAware<StarterService> starterServiceAware, 
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(start = true, value = "st-resource.jar") Bundle bundle
    		) throws Throwable {

    	StarterService starterService = starterServiceAware.waitForService(500);
    	
        starterService.start("SmartPlug");
        Thread.sleep(2000);

        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("SmartPlug");
        Service service = provider.getService("PowerService");
        Resource variable = service.getResource("status");
        Resource variation = service.getResource("variation");
        //System.out.println(service.getDescription().getDescription());

        SnaMessage response = variable.get(DataResource.VALUE, (Object[]) null);
        JSONObject jsonObject = new JSONObject(response.getJSON());

        assertEquals(1, (int) jsonObject.getJSONObject("response").getInt("value"));

        Resource resource = service.getResource("turnon");
        
        ActionResource action = (ActionResource) resource;
        action.act(new Object[0]);
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
        assertEquals(1, (int) jsonObject.getJSONObject("response").getInt("value"));

        resource = service.getResource("turnoff");
        
        action = (ActionResource) resource;
        action.act(new Object[0]);
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
        jsonObject = new JSONObject(response.getJSON());

        assertEquals(0, (int) jsonObject.getJSONObject("response").getInt("value"));
        response = variation.get(DataResource.VALUE, (Object[]) null);
        jsonObject = new JSONObject(response.getJSON());

        assertEquals(0.2f, (float) jsonObject.getJSONObject("response").getDouble("value"), 0.0f);
        core.close();
    }

    @Test
    public void testConstrainedResourceModel(
    		@InjectService(cardinality = 0) ServiceAware<StarterService> starterServiceAware, 
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(start = true, value = "temperature-resource.jar") Bundle bundle
    		) throws Throwable {

    	StarterService starterService = starterServiceAware.waitForService(500);
    	
    	starterService.start("TestForSensiNactGateway");
        Thread.sleep(2000);
        
        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway");
        Service service = provider.getService("sensor");
        Resource temperature = service.getResource("temperature");
        JSONObject jsonObject;
//        MidProxy midTemperature = (MidProxy) Proxy.getInvocationHandler(temperature);
//        SnaMessage response = (SnaMessage) midTemperature.toOSGi(getMethod, new Object[]{DataResource.VALUE, null});
        SnaMessage response = temperature.get(DataResource.VALUE, (Object[]) null);
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(5.0f, (float) jsonObject.getJSONObject("response").getDouble("value"), 0.0f);

        response = temperature.set(DataResource.VALUE, -24.5f, (Object[]) null);
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(-24.5f, (float) jsonObject.getJSONObject("response").getDouble("value"), 0.0f);
        response = temperature.set(DataResource.VALUE, 45.1f, (Object[]) null);
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(520, (int) jsonObject.getInt("statusCode"));
        core.close();
    }

    @Test
    public void testResourceModel(
    		@InjectService(cardinality = 0) ServiceAware<StarterService> starterServiceAware, 
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(start = true, value = "genova-resource.jar") Bundle bundle
    		) throws Throwable {
        
    	StarterService starterService = starterServiceAware.waitForService(500);
    	
        starterService.start("weather_5");        
        Thread.sleep(2000);
        
        Session session = core.getAnonymousSession();
        ServiceProvider provider = session.serviceProvider("weather_5");
        Service service = provider.getService("admin");
        Description description = service.getDescription();
        JSONObject jsonObject = new JSONObject(description.getJSON());
        core.close();
    }
    
    @Test
    public void testFactory(
    		@InjectService(cardinality = 0) ServiceAware<ProcessorService> processorAware, 
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(start = true, value = "test-resource.jar") Bundle bundle
    		) throws Throwable {
        
    	ProcessorService processor = processorAware.waitForService(500);
        processor.process("device1");
        Thread.sleep(2000);
        
        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("device1");
        Service ldrService = provider.getService("ldr");
        System.err.println("Resources");
        ldrService.getResources().stream().forEach(r -> System.err.println(r.getName()));
        Resource ldrResource = ldrService.getResource("value");
        
        Description response = ldrResource.getDescription();
        JSONObject responseDescription = new JSONObject(response.getJSONDescription());

        JSONArray attributes = responseDescription.getJSONArray("attributes");

        int index = 0;
        int length = attributes.length();
        JSONObject valueDescription = null;

        for (; index < length; index++) {
            JSONObject object = attributes.getJSONObject(index);
            if ("value".equals(object.optString("name"))) {
                valueDescription = object;
                break;
            }
        }
        JSONAssert.assertEquals(new JSONObject("{\"name\":\"value\",\"type\":\"float\",\"metadata\":" + "[{\"name\":\"modifiable\",\"value\":" + "\"UPDATABLE\",\"type\":\"org.eclipse.sensinact.gateway.common.primitive.Modifiable\"}," + "{\"name\":\"nickname\",\"value\":\"value\",\"type\":\"string\"}," + "{\"name\":\"Description\",\"value\":" + "\"Detected light/darkness\",\"type\":\"string\"}," + "{\"name\":\"Unit\"," + "\"value\":\"LUX\",\"type\":\"string\"}]}"), valueDescription, false);
        core.close();
        
    }

    @Test
    public void testAnnotationResolver(
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(start = true, value = "extra-2.jar") Bundle bundle
    		) throws Throwable {
        Session session = core.getAnonymousSession();

//        MidProxy midSession = (MidProxy) Proxy.getInvocationHandler(session);
//        Object providers = midSession.toOSGi(getProviders,null);        
//        MidProxy midDesc = (MidProxy) Proxy.getInvocationHandler(providers);
//        String resp = (String)midDesc.toOSGi(getJSON,null);
        DescribeResponse<String> providers = session.getProviders();
        String resp = providers.getJSON();
        System.out.println(resp);
        
        
//        Object services = midSession.toOSGi(getServices, new Object[] {"providerTest"});
//        midDesc = (MidProxy) Proxy.getInvocationHandler(services);
//        resp = (String)midDesc.toOSGi(getJSON,null);
        DescribeResponse<String> services = session.getServices("providerTest");
        resp = services.getJSON();
        System.out.println(resp);
        
//        Object resources = midSession.toOSGi(getResources, new Object[] {"providerTest","measureTest"});
//        midDesc = (MidProxy) Proxy.getInvocationHandler(resources);
//        resp = (String)midDesc.toOSGi(getJSON,null);
        DescribeResponse<String> resources = session.getResources("providerTest","measureTest");
        resp = resources.getJSON();
        System.out.println(resp);
        
//        resources = midSession.toOSGi(getResources, new Object[] {"providerTest","serviceTest"});
//        midDesc = (MidProxy) Proxy.getInvocationHandler(resources);
//        resp = (String)midDesc.toOSGi(getJSON,null);
        resources = session.getResources("providerTest","serviceTest");
        resp = resources.getJSON();
        System.out.println(resp);
        
//        Object res = midSession.toOSGi(resource, new Object[]{"providerTest", "measureTest", "condition"});        
//        MidProxy midResource = (MidProxy) Proxy.getInvocationHandler(res);
//        Description description = (Description) midResource.toOSGi(getDescription, null);
        Resource resource = session.resource("providerTest", "measureTest", "condition");
        Description description = resource.getDescription();
        System.out.println(description.getJSON());
        core.close();
    }

    @Test
    public void testAnnotatedPacket(
    		@InjectService(cardinality = 0) ServiceAware<StarterService> starterServiceAware, 
    		@InjectService(cardinality = 0) ServiceAware<ProcessorService> processorServiceAware, 
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(start = true, value = "extra-3.jar") Bundle bundle
    		) throws Throwable {

    	StarterService starterService = starterServiceAware.waitForService(500);
    	
    	Session session = core.getAnonymousSession();
        starterService.start("weather_7");
        Thread.sleep(2000);
        ServiceProvider provider = session.serviceProvider("weather_7");
        Service service = provider.getService("admin");
        Resource resource = service.getResource("location");

//        Description response = (Description) midAdmin.toOSGi(getDescription, null);
        Description response = service.getDescription();
//        MidProxy midResource = (MidProxy) Proxy.getInvocationHandler(resource);
//        SnaMessage message = (SnaMessage) midResource.toOSGi(setMethod, new Object[]{"value", "45.5667:5.9333", null});
        SnaMessage message = resource.set("value", "45.5667:5.9333", null);
        JSONObject jsonObject = new JSONObject(message.getJSON());

        jsonObject.getJSONObject("response").remove("timestamp");
        JSONAssert.assertEquals(new JSONObject("{\"statusCode\":200,\"response\":{\"name\":\"location\",\"value\":\"45.5667:5.9333\"," + "\"type\":\"string\"},\"type\":\"SET_RESPONSE\",\"uri\":\"/weather_7/admin/location\"}"), jsonObject, false);
//        MidProxy<ProcessorService> processor = new MidProxy<ProcessorService>(classloader, this, ProcessorService.class);
//
//        ProcessorService processorService = processor.buildProxy();

        ProcessorService processorService = processorServiceAware.waitForService(500);
        
        processorService.process("weather_7,null,admin,location,45.900002:6.11667");
        
//        message = (SnaMessage) midResource.toOSGi(getMethod, new Object[]{"value", null});
        message = resource.get("value", (Object[]) null);

        jsonObject = new JSONObject(message.getJSON());
        jsonObject.getJSONObject("response").remove("timestamp");

        JSONAssert.assertEquals(new JSONObject("{\"statusCode\":200,\"response\":{\"name\":\"location\",\"value\":\"45.900002:6.11667\"," + "\"type\":\"string\"},\"type\":\"GET_RESPONSE\",\"uri\":\"/weather_7/admin/location\"}"), jsonObject, false);
        core.close();
    }

    @Test
    @Disabled
    public void testExtraCatalogs(
    		@InjectInstalledBundle(start = true, value = "extra-4.jar") Bundle bundle4,
    		@InjectInstalledBundle( value = "extra-5.jar") Bundle bundle5,// do not start its a fragment
    		@InjectInstalledBundle( value = "extra-6.jar") Bundle bundle6,// do not start its a fragment
    		@InjectService(timeout = 500) Core core
    		) throws Throwable {
    	String all = "{"+
    			   "\"providers\": ["+
    			     "{"+
    			       "\"name\": \"weather_0\","+
    			       "\"location\": \"45.2:5.7\","+
    			       "\"services\": ["+
    			         "{"+
    			           "\"name\": \"admin\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"friendlyName\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"location\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"bridge\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"icon\","+
    			               "\"type\": \"PROPERTY\""+
    			             "}"+
    			           "]"+
    			         "},"+
    			         "{"+
    			           "\"name\": \"weather\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"pressure\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"temperature\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-chill\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"rainfall\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"humidity\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"dew-point\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-orientation\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-speed\","+
    			               "\"type\": \"SENSOR\""+
    			             "}"+
    			           "]"+
    			         "},"+
    			         "{"+
    			           "\"name\": \"hydrometers\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"value\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"alarm_name\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"alarm_limit\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"alarm_status\","+
    			               "\"type\": \"PROPERTY\""+
    			             "}"+
    			           "]"+
    			         "}"+
    			       "]"+
    			     "},"+
    			     "{"+
    			       "\"name\": \"weather_2\","+
    			       "\"location\": \"45.2:5.7\","+
    			       "\"services\": ["+
    			         "{"+
    			           "\"name\": \"admin\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"friendlyName\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"location\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"bridge\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"icon\","+
    			               "\"type\": \"PROPERTY\""+
    			             "}"+
    			           "]"+
    			         "},"+
    			         "{"+
    			           "\"name\": \"weather\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"pressure\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"temperature\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-chill\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"rainfall\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"humidity\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"dew-point\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-orientation\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-speed\","+
    			               "\"type\": \"SENSOR\""+
    			             "}"+
    			           "]"+
    			         "},"+
    			         "{"+
    			           "\"name\": \"hydrometers\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"value\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"alarm_name\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"alarm_limit\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"alarm_status\","+
    			               "\"type\": \"PROPERTY\""+
    			             "}"+
    			           "]"+
    			         "},"+
    			         "{"+
    			           "\"name\": \"pressure\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"atmospheric\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"submarine\","+
    			               "\"type\": \"PROPERTY\""+
    			             "}"+
    			           "]"+
    			         "}"+
    			       "]"+
    			     "},"+
    			     "{"+
    			       "\"name\": \"weather_1\","+
    			       "\"location\": \"45.2:5.7\","+
    			       "\"services\": ["+
    			         "{"+
    			           "\"name\": \"admin\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"friendlyName\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"location\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"bridge\","+
    			               "\"type\": \"PROPERTY\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"icon\","+
    			               "\"type\": \"PROPERTY\""+
    			             "}"+
    			           "]"+
    			         "},"+
    			         "{"+
    			           "\"name\": \"weather\","+
    			           "\"resources\": ["+
    			             "{"+
    			               "\"name\": \"pressure\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"temperature\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-chill\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"rainfall\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"humidity\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"dew-point\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-orientation\","+
    			               "\"type\": \"SENSOR\""+
    			             "},"+
    			             "{"+
    			               "\"name\": \"wind-speed\","+
    			               "\"type\": \"SENSOR\""+
    			             "}"+
    			           "]"+
    			         "}"+
    			       "]"+
    			     "}"+
    			   "],"+
    			   "\"type\": \"COMPLETE_LIST\","+
    			   "\"uri\": \"/\","+
    			   "\"statusCode\": 200"+
    			 "}";
    	
    	
    	Arrays.asList(bundle4.getBundleContext().getBundles())
    		.stream().forEach(b -> {
    			
    			System.err.println(b.getSymbolicName() + " - " + b.getState());
    		});
    	
        Session session = core.getAnonymousSession();
        DescribeResponse<String> response = session.getAll();
        
        System.err.println(response.getJSON());
        
        JSONAssert.assertEquals(all, response.getJSON(), false);
        core.close();
    }

    protected void doInit(Map<String, Object> configuration) {

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
                "file:target/felix/bundle/slf4j-api.jar " + 
                "file:target/felix/bundle/slf4j-simple.jar");
        configuration.put("felix.auto.start.2", 
        		"file:target/felix/bundle/sensinact-signature-validator.jar " + 
        		"file:target/felix/bundle/sensinact-core.jar ");
        configuration.put("felix.auto.start.3", 
        		"file:target/felix/bundle/dynamicBundle.jar ");
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
//            FileOutputStream output = new FileOutputStream(new File(loadDir,fileName));
//            byte[] testCng = IOUtils.read(testFileURL.openStream(), true);
//            IOUtils.write(testCng, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeMoke(URL resource, Map<?, ?> defaults, boolean startAtInitializationTime) throws Exception {
//        StringBuilder builder = new StringBuilder();
//        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
//        builder.append("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
//        builder.append("<properties>");
//        builder.append("<entry key=\"startAtInitializationTime\">");
//        builder.append(startAtInitializationTime);
//        builder.append("</entry>");
//
//        if (defaults != null && !defaults.isEmpty()) {
//            Iterator<Map.Entry> iterator = defaults.entrySet().iterator();
//            while (iterator.hasNext()) {
//                Map.Entry entry = iterator.next();
//                builder.append("<entry key=\"");
//                builder.append(entry.getKey());
//                builder.append("\">)");
//                builder.append(entry.getValue());
//                builder.append("</entry>");
//            }
//        }
//        builder.append("</properties>");
//
//        File tmpDirectory = new File("./target/felix/tmp");
//        if(!tmpDirectory.exists()) {
//        	tmpDirectory.mkdir();
//        } else {
//        	new File(tmpDirectory, "resources.xml").delete();
//        	new File(tmpDirectory, "dynamicBundle.jar").delete();
//        }
//        File confDirectory = new File("./target/felix/conf");
//        new File(confDirectory, "props.xml").delete();
//
//        FileOutputStream output = new FileOutputStream(new File(confDirectory, "props.xml"));
//        IOUtils.write(builder.toString().getBytes(), output);
//        
//        byte[] resourcesBytes = IOUtils.read(resource.openStream());        
//        output = new FileOutputStream(new File(tmpDirectory, "resources.xml"));
//        IOUtils.write(resourcesBytes, output);
//
//        super.createDynamicBundle(new File("./extra-src/test/resources/MANIFEST.MF"), tmpDirectory, new File("./extra-src/test/resources/meta"), new File(confDirectory, "props.xml"), new File(tmpDirectory, "resources.xml"), new File("./target/extra-test-classes"));
//
//        Bundle bundle = super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL());
//
//        ClassLoader current = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(classloader);
//        try {
//            bundle.start();
//        } catch(Exception e){
//        	e.printStackTrace();
//        }finally {
//            Thread.currentThread().setContextClassLoader(current);
//        }
//        Thread.sleep(5000);
    }
}
