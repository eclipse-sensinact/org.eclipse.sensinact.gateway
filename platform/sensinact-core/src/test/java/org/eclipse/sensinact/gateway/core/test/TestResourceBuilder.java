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
package org.eclipse.sensinact.gateway.core.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Changed;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.MaxLength;
import org.eclipse.sensinact.gateway.common.constraint.MinLength;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.props.TypedProperties;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.Metadata;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ModelInstanceBuilder;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.SensiNact;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResult;
import org.eclipse.sensinact.gateway.core.method.LinkedActMethod;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.method.Shortcut;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.trigger.Constant;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.skyscreamer.jsonassert.JSONAssert;

import junit.framework.Assert;

/**
 * Test ResourceFactory
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class TestResourceBuilder<R extends ModelInstance>
{ 
	protected TestContext testContext;
	protected Dictionary<String,Object> props;
	
	@Before
	public void init() 
			throws InvalidServiceProviderException, InvalidSyntaxException, 
			SecuredAccessException, BundleException 
	{	    
		this.testContext = new TestContext();
    }
	
    
	@After
	public void tearDown()
	{
		this.testContext.stop();
	}
	
	 @Test
    public void testSessionMethods() throws Exception
    {
    	ServiceImpl service = 
    		this.testContext.getModelInstance().getRootElement(
    			).addService("testService");
        
    	ResourceImpl r1impl = service.addDataResource(
    			PropertyResource.class, "TestProperty", 
    			String.class, "hello");  
    	
    	Session session = this.testContext.getSensiNact(
    			).getAnonymousSession();

        PropertyResource r1 = r1impl.<PropertyResource>getProxy(
        		SecuredAccess.ANONYMOUS_PKEY);

        //test shortcut
        Assert.assertEquals("TestProperty",r1.getName()); 
        Assert.assertEquals(Resource.Type.PROPERTY, r1.getType());

        String get1 = session.get("serviceProvider", "testService", 
        	"TestProperty", DataResource.VALUE).toString();	        
        String get2 = r1.get().getJSON();

        JSONAssert.assertEquals(get1,get2, false); 
        
		final JSONObject changed = new JSONObject(new Changed(
				Thread.currentThread().getContextClassLoader(),
				true).getJSON());

        JSONObject res = session.subscribe("serviceProvider", "testService", 
        	"TestProperty",  new Recipient()
   		{
			@Override
			public void callback(String callbackId, 
					SnaMessage[] messages) throws Exception
            {	
				boolean hasChanged = ((TypedProperties<?>
				)messages[0]).<Boolean>get("hasChanged");

				if(!hasChanged);
				{
					TestResourceBuilder.this.testContext.extraCallbackInc();
				}
            }

			@Override
            public String getJSON()
            {
	            return null;
            }

			@Override
            public SnaCallback.Type getSnaCallBackType()
            {
	            return  SnaCallback.Type.UNARY;
            }

			@Override
            public long getLifetime()
            {
	            return -1;
            }

			@Override
            public int getBufferSize()
            {
	            return 0;
            }

			@Override
            public int getSchedulerDelay()
            {
	            return 0;
            }
		},
   		new JSONArray()
		{{
			this.put(changed);
		}}
		);

        session.set("serviceProvider", "testService", 
            "TestProperty", DataResource.VALUE, "hello");

        Thread.sleep(500);

        Assert.assertEquals("the message should have been processed even if the value has not changed",
        	1, this.testContext.getExtraCallbackCount());
        
    	ResourceImpl r2impl = service.addDataResource(PropertyResource.class, 
    		"TestProperty2", String.class, null); 
    	
        //PropertyResource r2 = r2impl.<PropertyResource>getProxy(SecuredAccess.ANONYMOUS_PKEY);        		
        ResourceImpl r3impl = service.addLinkedResource("LinkedProperty", r1impl);
    	//PropertyResource r3 = r3impl.<PropertyResource>getProxy(SecuredAccess.ANONYMOUS_PKEY);

    	service.addLinkedResource(LocationResource.LOCATION,
    		this.testContext.getModelInstance().getRootElement(
    				).getAdminService().getResource(
				LocationResource.LOCATION));
    	
    	ResourceImpl r4impl = service.getResource("location");

        r4impl.registerExecutor(
        		AccessMethod.Type.valueOf(AccessMethod.GET), 
        		new Class<?>[0], 
        		new String[0],
        		new AccessMethodExecutor()
        		{
					@Override
                    public Void execute(AccessMethodResult result)
                            throws Exception
                    {
						JSONObject jsonObject = result.getAccessMethodObjectResult();
						
						jsonObject.put("value", new StringBuilder().append(
							jsonObject.get("value")).append(
								"_suffix").toString());
						
						result.setAccessMethodObjectResult(jsonObject);
	                    return null;
                    }}, 
        		AccessMethodExecutor.ExecutionPolicy.AFTER);

        String attributeValue = (String) r4impl.getAttribute("value").getValue();       
   	    //LocationResource r4 = r4impl.<LocationResource>getProxy(SecuredAccess.ANONYMOUS_PKEY);    	 
   	    
   	    Thread.sleep(250);
   	
        StringBuilder buffer = new StringBuilder();
        buffer.append(attributeValue);
        buffer.append("_suffix");
        
        JSONObject message = session.get("serviceProvider", "testService",
        		"location", DataResource.VALUE);
        
        assertEquals(buffer.toString(), message.getJSONObject("response"
        		).getString(DataResource.VALUE));
        
        //test linked resource
        JSONAssert.assertEquals(
        	session.get("serviceProvider", "testService",
            "TestProperty", null).getJSONObject("response"),
        	session.get("serviceProvider", "testService",
        	"LinkedProperty", DataResource.VALUE).getJSONObject("response"),
        	false);
        
        r1.set(new Object[]{"testLink"}).getJSON();
        
        session.set("serviceProvider", "testService", 
            "LinkedProperty", DataResource.VALUE, "testLink");

        JSONAssert.assertEquals(
            	session.get("serviceProvider", "testService",
                "TestProperty", DataResource.VALUE).getJSONObject("response"),
            	session.get("serviceProvider", "testService",
            	"LinkedProperty", null).getJSONObject("response"),
            	false);
//
//        //test subscription
//        String subId = ((SubscribeResponse)r2.subscribe(
//        		new Recipient()
//        		{
//					@Override
//					public void callback(String callbackId, 
//							SnaMessage[] messages) throws Exception
//		            {
//						TestResourceBuilder.this.callbackInc();	
//		            }
//		
//					@Override
//		            public String getJSON()
//		            {
//			            return null;
//		            }
//		
//					@Override
//		            public SnaCallback.Type getSnaCallBackType()
//		            {
//			            return  SnaCallback.Type.UNARY;
//		            }
//		
//					@Override
//		            public long getLifetime()
//		            {
//			            return -1;
//		            }
//		
//					@Override
//		            public int getBufferSize()
//		            {
//			            return 0;
//		            }
//		
//					@Override
//		            public int getSchedulerDelay()
//		            {
//			            return 0;
//		            }
//			})).getResponse(String.class,"subscriptionId");
//                
//         r3.subscribe(
//        		new Recipient()
//        		{
//					@Override
//					public void callback(String callbackId, 
//							SnaMessage[] messages) throws Exception
//		            {
//						TestResourceBuilder.this.linkCallbackInc();
//		            }
//		
//					@Override
//		            public String getJSON()
//		            {
//			            return null;
//		            }
//		
//					@Override
//		            public SnaCallback.Type getSnaCallBackType()
//		            {
//			            return  SnaCallback.Type.UNARY;
//		            }
//		
//					@Override
//		            public long getLifetime()
//		            {
//			            return -1;
//		            }
//		
//					@Override
//		            public int getBufferSize()
//		            {
//			            return 0;
//		            }
//		
//					@Override
//		            public int getSchedulerDelay()
//		            {
//			            return 0;
//		            }
//			});   
//        JSONObject set1 = r2.set("property3").getResponse();
//        Thread.sleep(250); 
//        
//        JSONObject set2 = r2.set("value","property3").getResponse();
//        
//        Assert.assertEquals(set1.get(DataResource.VALUE),set2.get(DataResource.VALUE)); 
//
//        JSONObject set3 = r1.set("value", "TEST LINKED SUBSCRIPTION").getResponse();
//        Thread.sleep(250);
//        
//        long time1 = (Long)set1.get(Metadata.TIMESTAMP);
//        long time2= (Long)set2.get(Metadata.TIMESTAMP);
//
//        Thread.sleep(500); 
//        Assert.assertTrue(time1 != time2);  
//        assertEquals(1, callbackCount); 
//        
//        r2.set("value","property5").getJSON();
//        Thread.sleep(500); 
//        assertEquals(2, callbackCount);
//        
//        String filter ="/serviceProvider/testService/TestProperty2/value";        
//        org.junit.Assert.assertEquals(1, instance.getHandler().count(filter));
//        r2.unsubscribe(subId);
//        org.junit.Assert.assertEquals(0, instance.getHandler().count(filter));
//
//	      Service proxy = service.<Service>getProxy(SecuredAccess.ANONYMOUS_PKEY);
//        SetResponse error = proxy.set("location","unknown");
//        assertTrue(error.getStatusCode()==403); 
//
//        assertEquals(1, linkCallbackCount);         
    }
	 
    @Test
    public void testResourceModel() throws Exception
    {
    	ServiceImpl service = 
    		this.testContext.getModelInstance().getRootElement(
    			).addService("testService");
        
    	ResourceImpl r1impl = service.addDataResource(
    			PropertyResource.class, "TestProperty", 
    			String.class, "hello");  
    	
        PropertyResource r1 = r1impl.<PropertyResource>getProxy(
        		SecuredAccess.ANONYMOUS_PKEY);

        //test shortcut
        Assert.assertEquals("TestProperty",r1.getName()); 
        Assert.assertEquals(Resource.Type.PROPERTY, r1.getType());

        String get1 = r1.get(DataResource.VALUE).getJSON();
        String get2 = r1.get().getJSON();

        JSONAssert.assertEquals(get1,get2, false); 
        SubscribeResponse res = r1.subscribe(new Recipient()
   		{
			@Override
			public void callback(String callbackId, 
					SnaMessage[] messages) throws Exception
            {	
				boolean hasChanged = ((TypedProperties<?>
				)messages[0]).<Boolean>get("hasChanged");

				if(!hasChanged);
				{
					TestResourceBuilder.this.testContext.extraCallbackInc();
				}
            }

			@Override
            public String getJSON()
            {
	            return null;
            }

			@Override
            public SnaCallback.Type getSnaCallBackType()
            {
	            return  SnaCallback.Type.UNARY;
            }

			@Override
            public long getLifetime()
            {
	            return -1;
            }

			@Override
            public int getBufferSize()
            {
	            return 0;
            }

			@Override
            public int getSchedulerDelay()
            {
	            return 0;
            }
		},
   		new HashSet<Constraint>()
		{{
			this.add(new Changed(Thread.currentThread(
					).getContextClassLoader(),true));
		}}
		);        
        
        r1.set("hello");        
        Thread.sleep(500);
      
        Assert.assertTrue(
        	"the message should have been processed even if the value has not changed",
        	1 == this.testContext.getExtraCallbackCount());
        
    	ResourceImpl r2impl = service.addDataResource(PropertyResource.class, 
    		"TestProperty2", String.class, null); 
    	
        PropertyResource r2 = r2impl.<PropertyResource>getProxy(SecuredAccess.ANONYMOUS_PKEY);        		
        ResourceImpl r3impl = service.addLinkedResource("LinkedProperty", r1impl);
    	PropertyResource r3 = r3impl.<PropertyResource>getProxy(SecuredAccess.ANONYMOUS_PKEY);

    	service.addLinkedResource(LocationResource.LOCATION,
    		this.testContext.getModelInstance().getRootElement(
    			).getAdminService().getResource(
				LocationResource.LOCATION));
    	
    	ResourceImpl r4impl = service.getResource("location");

        r4impl.registerExecutor(
        		AccessMethod.Type.valueOf(AccessMethod.GET), 
        		new Class<?>[0], 
        		new String[0],
        		new AccessMethodExecutor()
        		{
					@Override
                    public Void execute(AccessMethodResult result)
                            throws Exception
                    {
						JSONObject jsonObject = result.getAccessMethodObjectResult();
						
						jsonObject.put("value", new StringBuilder().append(
							jsonObject.get("value")).append(
								"_suffix").toString());
						
						result.setAccessMethodObjectResult(jsonObject);
	                    return null;
                    }}, 
        		AccessMethodExecutor.ExecutionPolicy.AFTER);

        String attributeValue = (String) r4impl.getAttribute("value").getValue();       
   	    LocationResource r4 = r4impl.<LocationResource>getProxy(SecuredAccess.ANONYMOUS_PKEY);    	 
   	    
   	    Thread.sleep(250);
   	
        StringBuilder buffer = new StringBuilder();
        buffer.append(attributeValue);
        buffer.append("_suffix");
        
        SnaMessage message = r4.get(DataResource.VALUE);
        
        JSONObject object = ((GetResponse)message).getResponse();
        assertEquals(buffer.toString(), object.getString(DataResource.VALUE));
        
        //test linked resource
        JSONAssert.assertEquals(((GetResponse) r1.get()).getResponse(),
                ((GetResponse) r3.get()).getResponse(), false);
        
        r1.set(new Object[]{"testLink"}).getJSON();
        
        JSONAssert.assertEquals(((GetResponse) r1.get()).getResponse(),
                ((GetResponse) r3.get()).getResponse(), false);

        //test subscription
        String subId = ((SubscribeResponse)r2.subscribe(
        		new Recipient()
        		{
					@Override
					public void callback(String callbackId, 
							SnaMessage[] messages) throws Exception
		            {
						TestResourceBuilder.this.testContext.callbackInc();	
		            }
		
					@Override
		            public String getJSON()
		            {
			            return null;
		            }
		
					@Override
		            public SnaCallback.Type getSnaCallBackType()
		            {
			            return  SnaCallback.Type.UNARY;
		            }
		
					@Override
		            public long getLifetime()
		            {
			            return -1;
		            }
		
					@Override
		            public int getBufferSize()
		            {
			            return 0;
		            }
		
					@Override
		            public int getSchedulerDelay()
		            {
			            return 0;
		            }
			})).getResponse(String.class,"subscriptionId");
                
         r3.subscribe(
        		new Recipient()
        		{
					@Override
					public void callback(String callbackId, 
							SnaMessage[] messages) throws Exception
		            {
						TestResourceBuilder.this.testContext.linkCallbackInc();
		            }
		
					@Override
		            public String getJSON()
		            {
			            return null;
		            }
		
					@Override
		            public SnaCallback.Type getSnaCallBackType()
		            {
			            return  SnaCallback.Type.UNARY;
		            }
		
					@Override
		            public long getLifetime()
		            {
			            return -1;
		            }
		
					@Override
		            public int getBufferSize()
		            {
			            return 0;
		            }
		
					@Override
		            public int getSchedulerDelay()
		            {
			            return 0;
		            }
			});   
        JSONObject set1 = r2.set("property3").getResponse();
        Thread.sleep(250); 
        
        JSONObject set2 = r2.set("value","property3").getResponse();
        
        Assert.assertEquals(set1.get(DataResource.VALUE),set2.get(DataResource.VALUE)); 

        JSONObject set3 = r1.set("value", "TEST LINKED SUBSCRIPTION").getResponse();
        Thread.sleep(250);
        
        long time1 = (Long)set1.get(Metadata.TIMESTAMP);
        long time2= (Long)set2.get(Metadata.TIMESTAMP);

        Thread.sleep(500); 
        Assert.assertTrue(time1 != time2);  
        assertEquals(1, this.testContext.getCallbackCount()); 
        
        r2.set("value","property5").getJSON();
        Thread.sleep(500); 
        assertEquals(2, this.testContext.getCallbackCount());
        
        String filter ="/serviceProvider/testService/TestProperty2/value";        
        org.junit.Assert.assertEquals(1,
        ((MyModelInstance)this.testContext.getModelInstance()).getHandler(
        		).count(filter));
        r2.unsubscribe(subId);
        org.junit.Assert.assertEquals(0, 
        ((MyModelInstance)this.testContext.getModelInstance()).getHandler(
        		).count(filter));

	    Service proxy = service.<Service>getProxy(SecuredAccess.ANONYMOUS_PKEY);
        SetResponse error = proxy.set("location","unknown");
        assertTrue(error.getStatusCode()==403); 

        assertEquals(1, this.testContext.getLinkCallbackCount());         
    }
    
    @Test
    public void testAgent() throws Exception
    {    	
    	ServiceImpl service1 = this.testContext.getModelInstance(
    		).getRootElement().addService("testService");
        
    	ResourceImpl r1impl = service1.addDataResource(
    			PropertyResource.class, "TestProperty", 
    			String.class, "hello"); 

    	ServiceImpl service2 = this.testContext.getModelInstance(
        	).getRootElement().addService("tostService");
        
    	ResourceImpl r2impl = service2.addDataResource(
    			PropertyResource.class, "TestProperty", 
    			String.class, "hello");
    	
       //wait for the previously generated events to be consumed
       Thread.sleep(1000);
       SnaFilter filter = new SnaFilter(this.testContext.getMediator(), 
    		   "/serviceProvider/(test).*", 
    		   true, false);
       
       filter.addHandledType(SnaMessage.Type.UPDATE);
       this.testContext.getSensiNact().registerAgent( 
       this.testContext.getMediator(), new AbstractSnaAgentCallback()
	   {
			@Override
			public void doHandle(SnaLifecycleMessageImpl message){
				/*System.out.println(message);*/}
	
			@Override
			public void doHandle(SnaErrorMessageImpl message){
				/*System.out.println(message);*/}
	
			@Override
			public void doHandle(SnaResponseMessage message){
				/*System.out.println(message);*/}
	
			@Override
			public void doHandle(SnaUpdateMessageImpl message)
			{
				TestResourceBuilder.this.testContext.agentCallbackInc();
				/*System.out.println("TestResourceBuilder.this.agentCallbackCount++");*/}
	
			@Override
			public  void stop()
	        {}
	   },filter);
       PropertyResource r1 = r1impl.<PropertyResource>getProxy(SecuredAccess.ANONYMOUS_PKEY);
       PropertyResource r2 = r2impl.<PropertyResource>getProxy(SecuredAccess.ANONYMOUS_PKEY);

   	   assertEquals(0, 
   	   TestResourceBuilder.this.testContext.getAgentCallbackCount());   	   
   	   r2.set("goodbye");

       Thread.sleep(250);
   	   assertEquals(0, 
   	   TestResourceBuilder.this.testContext.getAgentCallbackCount());

   	   r1.set("goodbye");
       
   	   Thread.sleep(250); 
   	   assertEquals(1,
   	   TestResourceBuilder.this.testContext.getAgentCallbackCount());
   	   
   	   Mockito.when(this.testContext.getMediator(
   	   ).getContext().getProperty(Mockito.anyString())
   	   ).thenAnswer(new Answer<Object>()
	   {
		@Override
		public String answer(InvocationOnMock invocation)
				throws Throwable
		{
			String parameter = (String) invocation.getArguments()[0];
			if("org.eclipse.sensinact.gateway.filter.suffix".equals(parameter))
			{
				return "test";
			}
			if("org.eclipse.sensinact.gateway.filter.types.test".equals(parameter))
			{
				return "LIFECYCLE";
			}
			if("org.eclipse.sensinact.gateway.filter.sender.test".equals(parameter))
			{
				return "/(serviceProvider)/(test).*";
			}
			if("org.eclipse.sensinact.gateway.filter.pattern.test".equals(parameter))
			{
				return "true";
			}
			if("org.eclipse.sensinact.gateway.filter.complement.test".equals(parameter))
			{
				return "true";
			}
			if("org.eclipse.sensinact.gateway.filter.conditions.test".equals(parameter))
			{
				return null;
			}
			return null;
		}   				   
	   });  	   
   	   TestResourceBuilder.this.testContext.agentCallbackCountReset();
   	   this.testContext.getSensiNact().registerAgent(
   			 this.testContext.getMediator(),
   	       new AbstractSnaAgentCallback()
   	       {
   			@Override
   	        public void doHandle(SnaLifecycleMessageImpl message){

   				TestResourceBuilder.this.testContext.agentCallbackInc();
   			}
   			@Override
   			public void doHandle(SnaErrorMessageImpl message){}

   			@Override
   			public void doHandle(SnaResponseMessage message){}

   			@Override
   			public void doHandle(SnaUpdateMessageImpl message)
   	        {
   	        }
			@Override
			public void stop()
            {				
            }}, null);
   	   
   	   this.testContext.getModelInstance().getRootElement().stop();
       Thread.sleep(500);
       
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/admin/location"}
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/admin/icon"}
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/admin/friendlyName"}
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/admin/bridge"}
//     {"notification":{"lifecycle":"SERVICE_DISAPPEARING"},"uri":"/serviceProvider/admin"}
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/tostService/TestProperty"}
//     {"notification":{"lifecycle":"SERVICE_DISAPPEARING"},"uri":"/serviceProvider/tostService"}
//     {"notification":{"lifecycle":"PROVIDER_DISAPPEARING"},"uri":"/serviceProvider"}
   	   assertEquals(8, this.testContext.getAgentCallbackCount());
    }
    
    @Test 
    public void testSecuredAccess() throws Exception
    {
    	ServiceImpl service = 
    		this.testContext.getModelInstance().getRootElement(
    			).addService("testService");   
    	
    	service.addLinkedResource(LocationResource.LOCATION, 
    	this.testContext.getModelInstance().getRootElement(
    		).getAdminService().getResource(
				LocationResource.LOCATION));
    	
    	ResourceImpl r4impl = service.getResource("location");
    	
        r4impl.registerExecutor(
		AccessMethod.Type.valueOf(AccessMethod.GET), 
		new Class<?>[0], 
		new String[0],
		new AccessMethodExecutor()
		{
			@Override
            public Void execute(AccessMethodResult result)
                    throws Exception
            {
				JSONObject jsonObject = result.getAccessMethodObjectResult();
				
				jsonObject.put("value", new StringBuilder().append(
					jsonObject.get("value")).append(
						"_suffix").toString());
				
				result.setAccessMethodObjectResult(jsonObject);
                return null;
            }}, 
		AccessMethodExecutor.ExecutionPolicy.AFTER);
	   
        String attributeValue = (String) r4impl.getAttribute("value").setValue("0:0");
    	Session session = this.testContext.getSensiNact().getAnonymousSession();  
    	
        Resource r4 = session.resource("serviceProvider", "testService", "location");       

		Assert.assertNotNull(r4);

        StringBuilder buffer = new StringBuilder();
        buffer.append(attributeValue);
        buffer.append("_suffix");
        
        GetResponse response =  r4.get(DataResource.VALUE);
        String value =  response.getResponse(String.class,DataResource.VALUE);
        assertEquals(buffer.toString(), value);
        
        r4 = session.resource("serviceProvider", "admin", "location");
        response =  r4.get(DataResource.VALUE);
        value =  response.getResponse(String.class,DataResource.VALUE);
        assertFalse(buffer.toString().equals(value));        
    }
    
    @Test(expected=InvalidValueException.class)
    public void testConstrained() throws Exception
    {    	
    	ServiceImpl service = 
    		this.testContext.getModelInstance().getRootElement(
    			).addService("testService");
    	
    	ResourceImpl r2impl = service.addDataResource(PropertyResource.class, 
    			"TestProperty2", String.class, null); 	
    	
    	Executable<Void,Object> extractor = service.getResourceValueExtractor("TestProperty2");
    	
        Attribute valueAttribute = r2impl.getAttribute(DataResource.VALUE);
        valueAttribute.addMetadata(new Metadata(
        	this.testContext.getMediator(), Metadata.CONSTRAINTS, Constraint[].class, 
        		new Constraint[]
        		{
        			new MinLength(this.testContext.getMediator().getClassLoader(), 5, false),
        			new MaxLength(this.testContext.getMediator().getClassLoader(), 10, false)
        			
        		}, Modifiable.FIXED));
        try
        {
        	valueAttribute.setValue("not much");
        	
        } catch(InvalidValueException e)
        {
        	fail("should have complied constraints");
        }
        assertEquals("not much", extractor.execute(null));
        valueAttribute.setValue("too much characters");
        fail("MaxLength constraint fails");	    
    }
    
    @Test
    public void testDynamicAdded() throws Exception
    {    	
    	ServiceImpl service = 
    			this.testContext.getModelInstance().getRootElement(
    			).addService("testService");
    	
    	ServiceProvider proxy = 
    			this.testContext.getModelInstance().getRootElementProxy(
    					SecuredAccess.ANONYMOUS_PKEY);
    	
    	List<Service> services = proxy.getServices();
    	Assert.assertEquals(2, services.size());
    	
    	this.testContext.getModelInstance(
    			).getRootElement().addService("dynamicService");
    	
    	services = proxy.getServices();
    	Assert.assertEquals(3, services.size());
    	
    	service.addDataResource(PropertyResource.class, "dynamicResource", 
    			String.class, "dynamic");

    	assertNotNull(proxy.getService("testService").getResource(
    			"dynamicResource"));
    }    

    @Test
    public void testLinkedAction() throws Exception
    {
    	ServiceImpl service = 
    		this.testContext.getModelInstance().getRootElement(
    			).addService("testService");  
    	
    	ResourceImpl action = service.addActionResource(
    			"action", ActionResource.class);

    	final AtomicInteger counter = new AtomicInteger(0);
    	
    	action.registerExecutor(new Signature(this.testContext.getMediator(), 
    		AccessMethod.Type.valueOf(AccessMethod.ACT),
    					new Class<?>[]{int.class}, 
    					new String[]{"count"}),
    			new AccessMethodExecutor(){

					@Override
                    public Void execute(AccessMethodResult parameter)
                            throws Exception
                    {
						int count = (Integer) parameter.getParameter(0);
						for(int i=0;i<count;i++)
						{
							counter.incrementAndGet();
						}
	                    return null;
                    }},
    			AccessMethodExecutor.ExecutionPolicy.AFTER);

    	action.registerExecutor(new Signature(this.testContext.getMediator(),
    		AccessMethod.Type.valueOf(AccessMethod.ACT),
    					new Class<?>[]{int.class, boolean.class}, 
    					new String[]{"count","plus"}),
    			new AccessMethodExecutor(){

					@Override
                    public Void execute(AccessMethodResult parameter)
                            throws Exception
                    {
						int count = (Integer) parameter.getParameter(0);
						boolean plus = (Boolean) parameter.getParameter(1);
						if(plus)
						{
							for(int i=0;i<count;i++)
							{
								counter.incrementAndGet();
							}
						} else
						{
							for(int i=0;i<count;i++)
							{
								counter.decrementAndGet();
							}
						}
	                    return null;
                    }},
    			AccessMethodExecutor.ExecutionPolicy.AFTER);

    	service.addLinkedActionResource("linkedAction", action, true); 

    	ResourceImpl emptyLinkedAction = service.addLinkedActionResource(
    			"emptyLinkedAction", action, false); 
    	
    	Method method = ResourceImpl.class.getDeclaredMethod("getAccessMethod", AccessMethod.Type.class);
    	method.setAccessible(true);
    	LinkedActMethod linkedActMethod = (LinkedActMethod) method.invoke(emptyLinkedAction, 
    			AccessMethod.Type.valueOf(AccessMethod.ACT));
    			
    	method = LinkedActMethod.class.getDeclaredMethod("createShortcut", new Class<?>[]
    			{Signature.class, Shortcut.class});
    	method.setAccessible(true);
    	
    	method.invoke(linkedActMethod, new Object[]{
    			new Signature(this.testContext.getMediator(), 
    					AccessMethod.Type.valueOf(AccessMethod.ACT), new Class<?>[]{int.class}, 
    					new String[]{"count"}),
    			new Shortcut(this.testContext.getMediator(), 
    					AccessMethod.Type.valueOf(AccessMethod.ACT), new Class<?>[0], new String[0], 
    					new HashMap<Integer,Parameter>(){{this.put(0, new Parameter(
    					TestResourceBuilder.this.testContext.getMediator(),"count",int.class,4));
    					}})});

    	//securedAccess.getAnonymousSession().registered(instance);
        //instance.getServiceProvider().start();
    	
    	ServiceProvider proxy = (ServiceProvider)
    		this.testContext.getModelInstance().getRootElement().getProxy(
    			SecuredAccess.ANONYMOUS_PKEY);
    	Service testService = proxy.getService("testService");    
    	
    	ActionResource resource = testService.<ActionResource>getResource("action");
    	ActionResource linkedResource = testService.<ActionResource>getResource("linkedAction");
    	ActionResource emptyLinkedResource = testService.<ActionResource>getResource("emptyLinkedAction");
    	
    	assertEquals(0,counter.get());
    	resource.act(5).getJSON();
    	assertEquals(5,counter.get());
    	resource.act(4,false).getJSON();
    	assertEquals(1,counter.get());
    	linkedResource.act(2).getJSON();
    	assertEquals(3,counter.get());
    	linkedResource.act(1, false).getJSON();
    	assertEquals(2,counter.get());
    	emptyLinkedResource.act().getJSON();
    	assertEquals(6,counter.get());
    	assertEquals(emptyLinkedResource.act(2).getStatus(), AccessMethodResponse.Status.ERROR);
    	assertEquals(6,counter.get());
    	assertEquals(emptyLinkedResource.act(2, false).getStatus(), AccessMethodResponse.Status.ERROR);
    	assertEquals(6,counter.get());
    }
    
    @Test
    public void testTrigger() throws Throwable
    {
    	ServiceImpl service = 
    		this.testContext.getModelInstance().getRootElement(
    			).addService("testService");

    	ResourceImpl r1impl = service.addActionResource(
    			"TestAction", ActionResource.class); 	
    	
    	ResourceImpl r2impl = service.addDataResource(
    			StateVariableResource.class, "TestVariable", 
    			String.class, "untriggered"); 	

       service.addActionTrigger("TestAction", "TestVariable", 
       new Signature(this.testContext.getMediator(), 
           AccessMethod.Type.valueOf(AccessMethod.ACT), null, null),
    		   new Constant("triggered", false),
    		       AccessMethodExecutor.ExecutionPolicy.AFTER);
       
       //test locked
       try
       {
    	   r2impl.set("triggered");
    	   fail("Attribute should be locked");
    	   
       }catch(Exception e)
       {
    	  //e.printStackTrace();
       }
   		//securedAccess.getAnonymousSession().registered(instance);
   		//instance.getServiceProvider().start();
       //test trigger
       assertEquals("untriggered", r2impl.getAttribute(DataResource.VALUE).getValue());
       ActionResource proxy = (ActionResource) r1impl.getProxy(SecuredAccess.ANONYMOUS_PKEY);
       proxy.act();
       assertEquals("triggered", r2impl.getAttribute(DataResource.VALUE).getValue());    		   
    }
}
