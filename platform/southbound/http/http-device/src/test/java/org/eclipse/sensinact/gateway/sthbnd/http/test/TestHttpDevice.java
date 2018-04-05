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
package org.eclipse.sensinact.gateway.sthbnd.http.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Map;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

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
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.eclipse.sensinact.gateway.util.IOUtils;

@SuppressWarnings({"unchecked","rawtypes"})
public class TestHttpDevice extends MidOSGiTest
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	public static int HTTP_PORT = 8893;
	public static String HTTP_ROOTURL = "http://127.0.0.1:" + HTTP_PORT;
	
	public static String newRequest(String configuration) throws IOException
	{
		SimpleResponse response;
		
        ConnectionConfigurationImpl<SimpleResponse,SimpleRequest> builder = 
        		new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>(
        				configuration);

    	SimpleRequest request = new SimpleRequest(builder);
    	response = request.send();

    	byte[] responseContent = response.getContent();
    	String contentStr =  (responseContent==null?null:new String(responseContent));        	
    	return contentStr;
	}
	
	public static String newRequest(String url, String content, String method) 
	{	
		SimpleResponse response;
        ConnectionConfigurationImpl<SimpleResponse,SimpleRequest> builder = 
        		new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();

        builder.setUri(url);
        try 
        {
            if(method.equals("GET"))
            {
            	builder.setHttpMethod("GET");
            	
            } else if(method.equals("POST")) 
            {
            	builder.setContentType("application/json");
            	builder.setHttpMethod("POST");

                if(content != null && content.length() > 0)
                {
                    JSONObject jsonData = new JSONObject(content);
                    builder.setContent(jsonData.toString());
                }
            } else 
            {
                return null;
            }
            builder.setAccept("application/json");

        	SimpleRequest request = new SimpleRequest(builder);
        	response = request.send();

        	byte[] responseContent = response.getContent();
        	String contentStr =  (responseContent==null?null:new String(responseContent));        	
        	return contentStr;

        } catch(Exception e)
        {  
			e.printStackTrace();
        }
        return null;
	}
	
	private static JettyTestServer server = null;
	private static JettyServerTestCallback callback= null;

	@BeforeClass
	public static void beforeClass() throws Exception
	{
		if(server != null)
		{
			if(server.isStarted())
			{
				server.stop();
				server.join();	
			}
			server = null;
		}
		server = new JettyTestServer(HTTP_PORT);
		new Thread(server).start();
		
		server.join();	
		callback = new JettyServerTestCallback();
		server.registerCallback(callback);
	}
	
	@AfterClass
	public static void  afterClass() throws Exception
	{
		server.stop();
		server.join();	
	}
	
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
    Method getDescription = null;
    Method getMethod = null;
    Method setMethod = null;
    Method actMethod = null;
 
	public TestHttpDevice() throws Exception
	{
		super();
	    getDescription = Describable.class.getDeclaredMethod("getDescription");
	    getMethod = Resource.class.getDeclaredMethod("get", 
	               new Class<?>[]{String.class});
	    setMethod = Resource.class.getDeclaredMethod("set", 
	               new Class<?>[]{String.class, Object.class});
        actMethod = ActionResource.class.getDeclaredMethod("act", 
                new Class<?>[]{Object[].class});
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see MidOSGiTest#isExcluded(java.lang.String)
	 */
	public boolean isExcluded(String fileName)
	{
		if("org.apache.felix.framework.security.jar".equals(fileName))
		{
			return true;
		}
		return false;
	}

	@Test
	public void testHttpTask() throws Throwable
	{
		callback.setRemoteEntity(new JSONObject(
				).put("serviceProviderId", "TestForSensiNactGateway"
				).put("serviceId", "service1"
				).put("resourceId", "temperature"
				).put("data", 24));
		
		this.initializeMoke(
				new File("src/test/resources/resources.xml").toURI().toURL(),
				new File("./extra-src/test/resources/MANIFEST.MF"),
				new File("./extra-src/test/resources/meta"),
				new File("./target/extra-test-classes"));
		
		MidProxy<Core> mid = new MidProxy<Core>(classloader, 
				this, Core.class);
		Core core = mid.buildProxy();
		
		Session session = core.getAnonymousSession();
		
		ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway");
		Service service = provider.getService("service1");
		Resource variable = service.getResource("temperature");

		MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);
				
		SnaMessage response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		
		
		JSONObject jsonObject = new JSONObject(response.getJSON());
		assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));
		
		response = (SnaMessage) midVariable.toOSGi(
			setMethod, new Object[]{ DataResource.VALUE,25 });		

		jsonObject = new JSONObject(response.getJSON());
		assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
		
		response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		jsonObject = new JSONObject(response.getJSON());
		assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
	}

	@Test
	public void testHttpTaskWithProcessingContext() throws Throwable
	{
		callback.setRemoteEntity(new JSONObject(
				).put("serviceProviderId", "TestForSensiNactGateway5"
				).put("serviceId", "service1"
				).put("resourceId", "temperature"
				).put("data", 24));
		
		this.initializeMoke(
				new File("src/test/resources/resources5.xml").toURI().toURL(),
				new File("./extra-src5/test/resources/MANIFEST.MF"),
				new File("./extra-src5/test/resources/meta"),
				new File("./target/extra-test-classes5"));
		

		MidProxy<Core> mid = new MidProxy<Core>(classloader, 
				this, Core.class);
		Core core = mid.buildProxy();
		
		Session session = core.getAnonymousSession();
		
		ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway5");
		Service service = provider.getService("service1");
		Resource variable = service.getResource("temperature");

		MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);
				
		SnaMessage response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		JSONObject jsonObject = new JSONObject(response.getJSON());
		assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));
		
		response = (SnaMessage) midVariable.toOSGi(
			setMethod, new Object[]{ DataResource.VALUE,25 });		

		jsonObject = new JSONObject(response.getJSON());
		assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
		
		response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		jsonObject = new JSONObject(response.getJSON());
		assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
	}

	@Test
	public void testHttpTaskWithServicesEnumeration() throws Throwable
	{
		callback.setRemoteEntity(new JSONObject(
				).put("serviceProviderId", "TestForSensiNactGateway4"
				).put("serviceId", "service1"
				).put("resourceId", "temperature"
				).put("data", 24));
		
		this.initializeMoke(
				new File("src/test/resources/resources4.xml").toURI().toURL(),
				new File("./extra-src4/test/resources/MANIFEST.MF"),
				new File("./extra-src4/test/resources/meta"),
				new File("./target/extra-test-classes4"));
		Thread.sleep(1000);

		MidProxy<Core> mid = new MidProxy<Core>(classloader, 
				this, Core.class);
		Core core = mid.buildProxy();
		
		Session session = core.getAnonymousSession();
		
		ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway4");
		Service service = provider.getService("service1");
		Resource variable = service.getResource("temperature");

		MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);
				
		SnaMessage response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		JSONObject jsonObject = new JSONObject(response.getJSON());
		assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));
		
		response = (SnaMessage) midVariable.toOSGi(
			setMethod, new Object[]{ DataResource.VALUE,25 });		

		jsonObject = new JSONObject(response.getJSON());
		assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
		
		response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		jsonObject = new JSONObject(response.getJSON());
		assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));
	}

	@Test
	public void testChainedHttpTask() throws Throwable
	{
		callback.setRemoteEntity(new JSONObject(
				).put("serviceProviderId", "TestForSensiNactGateway3"
				).put("serviceId", "service1"
				).put("resourceId", "temperature"
				).put("data", 24));
		
		this.initializeMoke(
				new File("src/test/resources/resources3.xml").toURI().toURL(),
				new File("./extra-src3/test/resources/MANIFEST.MF"),
				new File("./extra-src3/test/resources/meta"),
				new File("./target/extra-test-classes3"));
		

		MidProxy<Core> mid = new MidProxy<Core>(classloader, 
				this, Core.class);
		Core core = mid.buildProxy();
		
		Session session = core.getAnonymousSession();
		ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway3");
		Service service = provider.getService("service1");
		Resource variable = service.getResource("temperature");

		MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);
				
		SnaMessage response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		JSONObject jsonObject = new JSONObject(response.getJSON());
		assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));
	}
	
	@Test
	public void testHttpDeviceReccurrent() throws Throwable
	{
		callback.setRemoteEntity(new JSONObject(
				).put("serviceProviderId", "TestForSensiNactGateway2"
				).put("serviceId", "service1"
				).put("resourceId", "temperature"
				).put("data", 24));
		
		this.initializeMoke(
				new File("src/test/resources/resources2.xml").toURI().toURL(),
				new File("./extra-src2/test/resources/MANIFEST.MF"),
				new File("./extra-src2/test/resources/meta"),
				new File("./target/extra-test-classes2"));


		MidProxy<Core> mid = new MidProxy<Core>(classloader, 
				this, Core.class);
		Core core = mid.buildProxy();
		
		Session session = core.getAnonymousSession();
		ServiceProvider provider = session.serviceProvider("TestForSensiNactGateway2");
		Service service = provider.getService("service1");
		Resource variable = service.getResource("temperature");

		MidProxy midVariable = (MidProxy) Proxy.getInvocationHandler(variable);
		SnaMessage response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		JSONObject jsonObject = new JSONObject(response.getJSON());		
		assertEquals(24, (int) jsonObject.getJSONObject("response").getInt("value"));

		callback.setRemoteEntity(new JSONObject(
				).put("serviceProviderId", "TestForSensiNactGateway2"
				).put("serviceId", "service1"
				).put("resourceId", "temperature"
				).put("data", 25));
		
		Thread.sleep(1500);
		
		response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		jsonObject = new JSONObject(response.getJSON());
		assertEquals(25, (int) jsonObject.getJSONObject("response").getInt("value"));

		callback.setRemoteEntity(new JSONObject(
				).put("serviceProviderId", "TestForSensiNactGateway2"
				).put("serviceId", "service1"
				).put("resourceId", "temperature"
				).put("data", 32));
		
		Thread.sleep(1500);
		
		response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		jsonObject = new JSONObject(response.getJSON());		
		assertEquals(32, (int) jsonObject.getJSONObject("response").getInt("value"));
		
		Thread.sleep(10000);
		
		callback.setRemoteEntity(new JSONObject(
				).put("serviceProviderId", "TestForSensiNactGateway2"
				).put("serviceId", "service1"
				).put("resourceId", "temperature"
				).put("data", 45));
		
		response = (SnaMessage) midVariable.toOSGi(
			getMethod, new Object[]{ DataResource.VALUE });		

		jsonObject = new JSONObject(response.getJSON());		
		assertEquals(32, (int) jsonObject.getJSONObject("response").getInt("value"));

	}

	/**
	 * @inheritDoc
	 * @see MidOSGiTest#doInit(java.util.Map)
	 */
	@Override
	protected void doInit(Map configuration)
	{
		configuration.put("felix.auto.start.1",
		    "file:target/felix/bundle/org.osgi.compendium.jar "
		  + "file:target/felix/bundle/org.apache.felix.configadmin.jar "
		  + "file:target/felix/bundle/org.apache.felix.framework.security.jar ");
		
		configuration.put("felix.auto.install.2",
		    "file:target/felix/bundle/sensinact-utils.jar "
		  + "file:target/felix/bundle/sensinact-common.jar "
		  + "file:target/felix/bundle/sensinact-datastore-api.jar "
		  + "file:target/felix/bundle/sensinact-framework-extension.jar "
		  + "file:target/felix/bundle/sensinact-security-none.jar "
		  + "file:target/felix/bundle/sensinact-generic.jar "
		  + "file:target/felix/bundle/http.jar "
		  + "file:target/felix/bundle/sensinact-northbound-access.jar ");
		
		configuration.put("felix.auto.start.2",
			"file:target/felix/bundle/sensinact-test-configuration.jar "
		  + "file:target/felix/bundle/sensinact-core.jar "
		  + "file:target/felix/bundle/sensinact-signature-validator.jar ");

		configuration.put("felix.auto.start.3", 
			"file:target/felix/bundle/dynamicBundle.jar");

		configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
		configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");
	}
	
	private void initializeMoke(URL resource, File manifestFile,
			File... sourceDirectories) throws Exception
	{		
		File tmpDirectory = new File("./target/felix/tmp");
		new File(tmpDirectory,"resources.xml").delete();
		new File(tmpDirectory,"dynamicBundle.jar").delete();
				
		FileOutputStream output = null;

		byte[] resourcesBytes = IOUtils.read(resource.openStream());
		output = new FileOutputStream(new File(
				tmpDirectory,"resources.xml"));
		IOUtils.write(resourcesBytes, output);
		
		int length =  (sourceDirectories==null?0:sourceDirectories.length);
		File[] sources = new File[length+1];
		int index = 0;
		if(length > 0)
		{
			for(;index < length;index++)
			{
				sources[index] = sourceDirectories[index];
			}
		}
		sources[index] = new File(tmpDirectory,"resources.xml");
		super.createDynamicBundle(manifestFile, tmpDirectory,
			sources);
		
		Bundle bundle = super.installDynamicBundle(new File(tmpDirectory,
				"dynamicBundle.jar").toURI().toURL());
		
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(super.classloader);
		try
		{
			bundle.start();
			
		}finally
		{
			Thread.currentThread().setContextClassLoader(current);
		}
		Thread.sleep(10*1000);
	}
}