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
package org.eclipse.sensinact.gateway.generic.test;

import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.eclipse.sensinact.gateway.test.ProcessorService;
import org.eclipse.sensinact.gateway.test.StarterService;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TestGenericImplementation extends MidOSGiTest {

    Method getDescription = null;
    Method getMethod = null;
    Method setMethod = null;
    Method actMethod = null;

    public TestGenericImplementation() throws Exception {
        super();
        getDescription = Describable.class.getDeclaredMethod("getDescription");
        getMethod = Resource.class.getDeclaredMethod("get", new Class<?>[]{String.class});
        setMethod = Resource.class.getDeclaredMethod("set", new Class<?>[]{String.class, Object.class});
        actMethod = ActionResource.class.getDeclaredMethod("act", new Class<?>[]{Object[].class});
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

    @Test
    public void testActionResourceModel() throws Throwable {
        this.initializeMoke(new File("src/test/resources/st-resource.xml").toURI().toURL(), null, false);
        ServiceReference reference = super.getBundleContext().getServiceReference(StarterService.class);

        StarterService starter = (StarterService) super.getBundleContext().getService(reference);
        starter.start("SmartPlug");
        Thread.sleep(2000);
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();

        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("SmartPlug");
        Service service = provider.getService("PowerService");
        Resource variable = service.getResource("status");
        Resource variation = service.getResource("variation");
        //System.out.println(service.getDescription().getDescription());
        MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);
        MidProxy midVariation = (MidProxy) Proxy.getInvocationHandler(variation);

        SnaMessage response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE});
        JSONObject jsonObject = new JSONObject(response.getJSON());

        assertEquals(1, (int) jsonObject.getJSONObject("response").getInt("value"));

        Resource resource = service.getResource("turnon");
        MidProxy midAction = (MidProxy) Proxy.getInvocationHandler(resource);

        MidProxy<ActionResource> actionProxy = new MidProxy<ActionResource>(classloader, this, ActionResource.class);

        ActionResource action = actionProxy.buildProxy(midAction.getInstance());
        actionProxy.toOSGi(actMethod, new Object[]{new Object[0]});
        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE});
        assertEquals(1, (int) jsonObject.getJSONObject("response").getInt("value"));

        resource = service.getResource("turnoff");
        midAction = (MidProxy) Proxy.getInvocationHandler(resource);
        actionProxy = new MidProxy<ActionResource>(classloader, this, ActionResource.class);

        action = actionProxy.buildProxy(midAction.getInstance());
        actionProxy.toOSGi(actMethod, new Object[]{new Object[0]});
        response = (SnaMessage) midVariable.toOSGi(getMethod, new Object[]{DataResource.VALUE});
        jsonObject = new JSONObject(response.getJSON());

        assertEquals(0, (int) jsonObject.getJSONObject("response").getInt("value"));
        response = (SnaMessage) midVariation.toOSGi(getMethod, new Object[]{DataResource.VALUE});

        jsonObject = new JSONObject(response.getJSON());

        assertEquals(0.2f, (float) jsonObject.getJSONObject("response").getDouble("value"), 0.0f);
        core.close();
    }

    @Test
    public void testConstrainedResourceModel() throws Throwable {
        this.initializeMoke(new File("src/test/resources/temperature-resource.xml").toURI().toURL(), null, false);
        ServiceReference reference = super.getBundleContext().getServiceReference(StarterService.class);

        StarterService starter = (StarterService) super.getBundleContext().getService(reference);
        starter.start("TestForSensiNactGateway");

        Thread.sleep(2000);
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway");
        Service service = provider.getService("sensor");
        Resource temperature = service.getResource("temperature");
        JSONObject jsonObject;
        MidProxy midTemperature = (MidProxy) Proxy.getInvocationHandler(temperature);
        SnaMessage response = (SnaMessage) midTemperature.toOSGi(getMethod, new Object[]{DataResource.VALUE});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(5.0f, (float) jsonObject.getJSONObject("response").getDouble("value"), 0.0f);

        response = (SnaMessage) midTemperature.toOSGi(setMethod, new Object[]{DataResource.VALUE, -24.5f});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(-24.5f, (float) jsonObject.getJSONObject("response").getDouble("value"), 0.0f);
        response = (SnaMessage) midTemperature.toOSGi(setMethod, new Object[]{DataResource.VALUE, 45.1f});
        jsonObject = new JSONObject(response.getJSON());
        assertEquals(520, (int) jsonObject.getInt("statusCode"));
        core.close();
    }

    @Test
    public void testResourceModel() throws Throwable {
        this.initializeMoke(new File("src/test/resources/genova-resource.xml").toURI().toURL(), null, false);
        Thread.sleep(5000);
        
        ServiceReference reference = super.getBundleContext().getServiceReference(StarterService.class);
        StarterService starter = (StarterService) super.getBundleContext().getService(reference);
        starter.start("weather_5");        
        Thread.sleep(2000);
        
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session session = core.getAnonymousSession();
        ServiceProvider provider = session.serviceProvider("weather_5");
        Service service = provider.getService("admin");
        Description description = service.getDescription();
        JSONObject jsonObject = new JSONObject(description.getJSON());
        core.close();
    }

    @Test
    public void testFactory() throws Throwable {
        this.initializeMoke(new File("src/test/resources/test-resource.xml").toURI().toURL(), new HashMap<String, String>() {{
            this.put("pir", "VALUE");
            this.put("ldr", "VALUE");
            this.put("gpr", "VALUE");

        }}, true);
        Thread.sleep(5000);
        
        ServiceReference reference = super.getBundleContext().getServiceReference(ProcessorService.class);
        ProcessorService processor = (ProcessorService) super.getBundleContext().getService(reference);
        processor.process("device1");
        
        Thread.sleep(2000);
        
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session session = core.getAnonymousSession();

        ServiceProvider provider = session.serviceProvider("device1");
        Service ldrService = provider.getService("ldr");
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
    public void testAnnotationResolver() throws Throwable {
        File tmpDirectory = new File("./target/felix/tmp");
        File confDirectory = new File("./target/felix/conf");
        new File(confDirectory, "props.xml").delete();
        if(!tmpDirectory.exists()) {
        	tmpDirectory.mkdir();
        } else {
        	new File(tmpDirectory, "resources.xml").delete();
        	new File(tmpDirectory, "dynamicBundle.jar").delete();
        }
        super.createDynamicBundle(new File("./extra-src2/test/resources/MANIFEST.MF"), tmpDirectory, new File("./extra-src2/test/resources/meta"), new File("./extra-src2/test/resources/test-resource.xml"), new File("./target/extra-test-classes2"));
        super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL()).start();
        Thread.sleep(5000);
        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session session = core.getAnonymousSession();

        Resource resource = session.resource("providerTest", "measureTest", "condition");
        MidProxy midResource = (MidProxy) Proxy.getInvocationHandler(resource);
        Description description = (Description) midResource.toOSGi(getDescription, null);
        core.close();
    }

    @Test
    public void testAnnotatedPacket() throws Throwable {
        File tmpDirectory = new File("./target/felix/tmp");
        File confDirectory = new File("./target/felix/conf");
        new File(confDirectory, "props.xml").delete();
        if(!tmpDirectory.exists()) {
        	tmpDirectory.mkdir();
        } else {
        	new File(tmpDirectory, "resources.xml").delete();
        	new File(tmpDirectory, "dynamicBundle.jar").delete();
        }
        super.createDynamicBundle(new File("./extra-src3/test/resources/MANIFEST.MF"), tmpDirectory, new File("./extra-src3/test/resources/meta"), new File("./src/test/resources/genova-resource.xml"), new File("./target/extra-test-classes3"));

        super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL()).start();
        Thread.sleep(5000);

        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session session = core.getAnonymousSession();
        MidProxy<StarterService> starter = new MidProxy<StarterService>(classloader, this, StarterService.class);
        StarterService starterService = starter.buildProxy();
        starterService.start("weather_7");
        Thread.sleep(2000);
        ServiceProvider provider = session.serviceProvider("weather_7");
        Service service = provider.getService("admin");
        Resource resource = service.getResource("location");

        MidProxy midAdmin = (MidProxy) Proxy.getInvocationHandler(service);
        Description response = (Description) midAdmin.toOSGi(getDescription, null);
        MidProxy midResource = (MidProxy) Proxy.getInvocationHandler(resource);
        SnaMessage message = (SnaMessage) midResource.toOSGi(setMethod, new Object[]{"value", "45.5667:5.9333"});
        JSONObject jsonObject = new JSONObject(message.getJSON());

        jsonObject.getJSONObject("response").remove("timestamp");
        JSONAssert.assertEquals(new JSONObject("{\"statusCode\":200,\"response\":{\"name\":\"location\",\"value\":\"45.5667:5.9333\"," + "\"type\":\"string\"},\"type\":\"SET_RESPONSE\",\"uri\":\"/weather_7/admin/location\"}"), jsonObject, false);
        MidProxy<ProcessorService> processor = new MidProxy<ProcessorService>(classloader, this, ProcessorService.class);

        ProcessorService processorService = processor.buildProxy();

        processorService.process("weather_7,null,admin,location,45.900002:6.11667");
        message = (SnaMessage) midResource.toOSGi(getMethod, new Object[]{"value"});

        jsonObject = new JSONObject(message.getJSON());
        jsonObject.getJSONObject("response").remove("timestamp");

        JSONAssert.assertEquals(new JSONObject("{\"statusCode\":200,\"response\":{\"name\":\"location\",\"value\":\"45.900002:6.11667\"," + "\"type\":\"string\"},\"type\":\"GET_RESPONSE\",\"uri\":\"/weather_7/admin/location\"}"), jsonObject, false);
        core.close();
    }

    @Test
    public void testExtraCatalogs() throws Throwable {
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
    	
        File tmpDirectory = new File("target/felix/tmp");
        File tmpDirectory1 = new File("target/felix/tmp1");
        File tmpDirectory2 = new File("target/felix/tmp2");
        
        File confDirectory = new File("target/felix/conf");
        new File(confDirectory, "props.xml").delete();
        
        if(!tmpDirectory1.exists()) {
        	tmpDirectory1.mkdir();
        }
        if(!tmpDirectory2.exists()) {
        	tmpDirectory2.mkdir();
        }
        if(!tmpDirectory.exists()) {
        	tmpDirectory.mkdir();
        } else {
        	new File(tmpDirectory, "resources.xml").delete();
        	new File(tmpDirectory, "dynamicBundle.jar").delete();
        }
        super.createDynamicBundle(
        	new File("./extra-src6/test/resources/MANIFEST.MF"), 
        	tmpDirectory2, 
        	new File("./extra-src6/test/resources/meta"),  
        	new File("./target/extra-test-classes6"));
        
        super.installDynamicBundle(new File(tmpDirectory2, 
        		"dynamicBundle.jar").toURI().toURL());
        
        Thread.sleep(2000);

        super.createDynamicBundle(
        	new File("./extra-src5/test/resources/MANIFEST.MF"), 
        	tmpDirectory1, 
        	new File("./extra-src5/test/resources/meta"),  
        	new File("./target/extra-test-classes5"));
        
        super.installDynamicBundle(new File(tmpDirectory1, 
        		"dynamicBundle.jar").toURI().toURL());
        
        Thread.sleep(2000);
        
        super.createDynamicBundle(
        	new File("./extra-src4/test/resources/MANIFEST.MF"), 
        	tmpDirectory, 
        	new File("./extra-src4/test/resources/meta"), 
        	new File("./src/test/resources/genova-resource_0.xml"), 
        	new File("./target/extra-test-classes4"));

        super.installDynamicBundle(new File(tmpDirectory, 
        		"dynamicBundle.jar").toURI().toURL()).start();
        
        Thread.sleep(5000);

        MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
        Core core = mid.buildProxy();
        Session session = core.getAnonymousSession();

        MidProxy midSession = (MidProxy) Proxy.getInvocationHandler(session);
        SnaMessage response = (SnaMessage) midSession.toOSGi(Session.class.getDeclaredMethod("getAll"), null);
        
        JSONAssert.assertEquals(all, response.getJSON(), false);
        core.close();
    }

    @Override
    protected void doInit(Map configuration) {

        configuration.put("felix.auto.start.1",  
                "file:target/felix/bundle/org.osgi.service.component.jar "+  
                "file:target/felix/bundle/org.osgi.service.cm.jar "+  
                "file:target/felix/bundle/org.osgi.service.metatype.jar "+  
                "file:target/felix/bundle/org.osgi.namespace.extender.jar "+  
                "file:target/felix/bundle/org.osgi.util.promise.jar "+  
                "file:target/felix/bundle/org.osgi.util.function.jar "+  
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
            FileOutputStream output = new FileOutputStream(new File(loadDir,fileName));
            byte[] testCng = IOUtils.read(testFileURL.openStream(), true);
            IOUtils.write(testCng, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeMoke(URL resource, Map defaults, boolean startAtInitializationTime) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        builder.append("<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">");
        builder.append("<properties>");
        builder.append("<entry key=\"startAtInitializationTime\">");
        builder.append(startAtInitializationTime);
        builder.append("</entry>");

        if (defaults != null && !defaults.isEmpty()) {
            Iterator<Map.Entry> iterator = defaults.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                builder.append("<entry key=\"");
                builder.append(entry.getKey());
                builder.append("\">)");
                builder.append(entry.getValue());
                builder.append("</entry>");
            }
        }
        builder.append("</properties>");

        File tmpDirectory = new File("./target/felix/tmp");
        if(!tmpDirectory.exists()) {
        	tmpDirectory.mkdir();
        } else {
        	new File(tmpDirectory, "resources.xml").delete();
        	new File(tmpDirectory, "dynamicBundle.jar").delete();
        }
        File confDirectory = new File("./target/felix/conf");
        new File(confDirectory, "props.xml").delete();

        FileOutputStream output = new FileOutputStream(new File(confDirectory, "props.xml"));
        IOUtils.write(builder.toString().getBytes(), output);
        
        byte[] resourcesBytes = IOUtils.read(resource.openStream());        
        output = new FileOutputStream(new File(tmpDirectory, "resources.xml"));
        IOUtils.write(resourcesBytes, output);

        super.createDynamicBundle(new File("./extra-src/test/resources/MANIFEST.MF"), tmpDirectory, new File("./extra-src/test/resources/meta"), new File(confDirectory, "props.xml"), new File(tmpDirectory, "resources.xml"), new File("./target/extra-test-classes"));

        Bundle bundle = super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL());

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classloader);
        try {
            bundle.start();
        } catch(Exception e){
        	e.printStackTrace();
        }finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        Thread.sleep(5000);
    }
}