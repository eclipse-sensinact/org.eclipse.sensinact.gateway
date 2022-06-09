/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
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
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
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

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

@ExtendWith(InstalledBundleExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class TestGenericImplementation {

	private static final String LOCATION_FORMAT = "{\"type\":\"FeatureCollection\",\"features\":"
			 + "[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"coordinates\":[%s,%s],\"type\":\"Point\"}}]}";

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

        SnaMessage<?> response = variable.get(DataResource.VALUE, (Object[]) null);
        JsonObject jsonObject = JsonProviderFactory.readObject(response.getJSON());

        assertEquals(1, (int) jsonObject.getJsonObject("response").getInt("value"));

        Resource resource = service.getResource("turnon");
        
        ActionResource action = (ActionResource) resource;
        action.act(new Object[0]);
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
        assertEquals(1, jsonObject.getJsonObject("response").getInt("value"));

        resource = service.getResource("turnoff");
        
        action = (ActionResource) resource;
        action.act(new Object[0]);
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
        jsonObject = JsonProviderFactory.readObject(response.getJSON());

        assertEquals(0, (int) jsonObject.getJsonObject("response").getInt("value"));
        response = variation.get(DataResource.VALUE, (Object[]) null);
        jsonObject = JsonProviderFactory.readObject(response.getJSON());

        assertEquals(0.2f, (float) jsonObject.getJsonObject("response").getJsonNumber("value").doubleValue(), 0.0f);
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
        JsonObject jsonObject;

        SnaMessage<?> response = temperature.get(DataResource.VALUE, (Object[]) null);
        jsonObject = JsonProviderFactory.readObject(response.getJSON());
        assertEquals(5.0f, (float) jsonObject.getJsonObject("response").getJsonNumber("value").doubleValue(), 0.0f);

        response = temperature.set(DataResource.VALUE, -24.5f, (Object[]) null);
        jsonObject = JsonProviderFactory.readObject(response.getJSON());
        assertEquals(-24.5f, (float) jsonObject.getJsonObject("response").getJsonNumber("value").doubleValue(), 0.0f);
        response = temperature.set(DataResource.VALUE, 45.1f, (Object[]) null);
        jsonObject = JsonProviderFactory.readObject(response.getJSON());
        assertEquals(520, jsonObject.getInt("statusCode"));
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
        JsonObject jsonObject = JsonProviderFactory.readObject(description.getJSON());
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
        JsonObject responseDescription = JsonProviderFactory.readObject(response.getJSONDescription());

        JsonArray attributes = responseDescription.getJsonArray("attributes");

        int index = 0;
        int length = attributes.size();
        JsonObject valueDescription = null;

        for (; index < length; index++) {
            JsonObject object = attributes.getJsonObject(index);
            if ("value".equals(object.getString("name", null))) {
                valueDescription = object;
                break;
            }
        }
        JsonObject expected = JsonProviderFactory.readObject("{\"name\":\"value\",\"type\":\"float\",\"metadata\":" 
        		+ "[{\"name\":\"modifiable\",\"value\":" 
        		+ "\"UPDATABLE\",\"type\":\"org.eclipse.sensinact.gateway.common.primitive.Modifiable\"}," 
        		+ "{\"name\":\"nickname\",\"value\":\"value\",\"type\":\"string\"}," 
        		+ "{\"name\":\"Description\",\"value\":" 
        		+ "\"Detected light/darkness\",\"type\":\"string\"}," 
        		+ "{\"name\":\"Unit\"," + "\"value\":\"LUX\",\"type\":\"string\"}]}");
		assertEquals(expected.get("name"), valueDescription.get("name"));
		assertEquals(expected.get("type"), valueDescription.get("type"));
		assertEquals(new HashSet<>(expected.getJsonArray("metadata")), 
				new HashSet<>(valueDescription.getJsonArray("metadata")));
        core.close();
        
    }

    @Test
    public void testAnnotationResolver(
    		@InjectService(timeout = 500) Core core,
    		@InjectInstalledBundle(start = true, value = "extra-2.jar") Bundle bundle
    		) throws Throwable {
        Session session = core.getAnonymousSession();

        DescribeResponse<String> providers = session.getProviders();
        String resp = providers.getJSON();
        System.out.println(resp);
        
        DescribeResponse<String> services = session.getServices("providerTest");
        resp = services.getJSON();
        System.out.println(resp);

        DescribeResponse<String> resources = session.getResources("providerTest","measureTest");
        resp = resources.getJSON();
        System.out.println(resp);

        resources = session.getResources("providerTest","serviceTest");
        resp = resources.getJSON();
        System.out.println(resp);
        
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

        Description response = service.getDescription();
        SnaMessage<?> message = resource.set("value", String.format(LOCATION_FORMAT, "5.9333","45.5667"), (Object[])null);
        JsonObject jsonObject = JsonProviderFactory.readObject(message.getJSON());

        long timestamp = jsonObject.getJsonObject("response").getJsonNumber("timestamp").longValue();
        assertEquals(JsonProviderFactory.readObject("{\"statusCode\":200,\"response\":{\"name\":\"location\",\"value\":\""+
        String.format(LOCATION_FORMAT,"5.9333", "45.5667").replace("\"","\\\"")
        +"\"," + "\"type\":\"string\",\"timestamp\":" + timestamp + "},\"type\":\"SET_RESPONSE\",\"uri\":\"/weather_7/admin/location\"}"), jsonObject);
//        MidProxy<ProcessorService> processor = new MidProxy<ProcessorService>(classloader, this, ProcessorService.class);
//
//        ProcessorService processorService = processor.buildProxy();

        ProcessorService processorService = processorServiceAware.waitForService(500);
        
        processorService.process("weather_7,null,admin,location,"+String.format(LOCATION_FORMAT, "6.11667","45.900002"));
        
//        message = (SnaMessage) midResource.toOSGi(getMethod, new Object[]{"value", null});
        message = resource.get("value", (Object[]) null);

        jsonObject = JsonProviderFactory.readObject(message.getJSON());
        timestamp = jsonObject.getJsonObject("response").getJsonNumber("timestamp").longValue();
        
        String expected = "{\"statusCode\":200,\"response\":{\"name\":\"location\",\"value\":\""+
        		String.format(LOCATION_FORMAT, "6.11667","45.900002").replace("\"","\\\"")+"\",\"type\":\"string\",\"timestamp\":" + timestamp + "},\"type\""
				+ ":\"GET_RESPONSE\",\"uri\":\"/weather_7/admin/location\"}";

        assertEquals(JsonProviderFactory.readObject(expected), jsonObject);
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
    			       "\"location\": \""+String.format(LOCATION_FORMAT, "5.7","45.2").replace("\"", "\\\"")+"\","+
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
    			       "\"location\": \""+String.format(LOCATION_FORMAT, "5.7","45.2").replace("\"", "\\\"")+"\","+
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
    			       "\"location\": \""+String.format(LOCATION_FORMAT, "5.7","45.2").replace("\"", "\\\"")+"\","+
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
}
