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
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.Shortcut;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.trigger.Constant;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.skyscreamer.jsonassert.JSONAssert;

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
import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResult;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.LinkedActMethod;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import junit.framework.Assert;

/**
 * Test ResourceFactory
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class TestResourceBuilder<R extends ModelInstance>
{       
	private static final Session.Key KEY =  new Session.Key();
	
	static
	{
		KEY.setToken("FAKEKEY");
		KEY.setUid(0);
	}
	
	private static final String LOG_FILTER = "("+Constants.OBJECTCLASS+"="+
		LogService.class.getCanonicalName()+")";	

	private static final String DATA_STORE_FILTER = "("+Constants.OBJECTCLASS+"="+
		DataStoreService.class.getCanonicalName()+")";

	private static final String AUTHENTICATION_FILTER = "("+Constants.OBJECTCLASS+"="+
		AuthenticationService.class.getCanonicalName()+")";

	private static final String ACCESS_FILTER = "("+Constants.OBJECTCLASS+"="+
		SecuredAccess.class.getCanonicalName()+")";

    private static final String VALIDATION_FILTER = "("+Constants.OBJECTCLASS+"="+
        BundleValidation.class.getCanonicalName()+")";
	
	private static final String AGENT_FILTER = "("+Constants.OBJECTCLASS+"="+
		SnaAgent.class.getCanonicalName()+")";

	private static final String AUTHORIZATION_FILTER = "("+Constants.OBJECTCLASS+"="+
		AuthorizationService.class.getCanonicalName()+")";
	
	private static final String MOCK_BUNDLE_NAME = "MockedBundle";
	private static final long MOCK_BUNDLE_ID = 1;
	
	private final Filter filterAgent = Mockito.mock(Filter.class);	
	private final Filter filterAccess = Mockito.mock(Filter.class);
    private final Filter filterValidation = Mockito.mock(Filter.class);
	private final Filter filterDataStore = Mockito.mock(Filter.class);
	private final Filter filterAuthentication = Mockito.mock(Filter.class);
	private final Filter filterAuthorization = Mockito.mock(Filter.class);
	
	private final BundleContext context = Mockito.mock(BundleContext.class);
	private final Bundle bundle = Mockito.mock(Bundle.class);

    private final ServiceReference referenceAgent = 
			Mockito.mock(ServiceReference.class);
	private final ServiceRegistration snaObjectRegistration = 
			Mockito.mock(ServiceRegistration.class);
	private final ServiceReference referenceAuthorization = 
			Mockito.mock(ServiceReference.class);
	private final ServiceReference referenceAccess = 
			Mockito.mock(ServiceReference.class);
    private final ServiceReference referenceValidation =
            Mockito.mock(ServiceReference.class);
	private final ServiceRegistration registration = 
			Mockito.mock(ServiceRegistration.class);
	private final ServiceRegistration registrationAgent = 
			Mockito.mock(ServiceRegistration.class);
	private final ServiceReference referenceProvider = 
			Mockito.mock(ServiceReference.class);

	private BundleValidation bundleValidation;
	private SecuredAccess securedAccess;
	private Mediator mediator;
	private MyModelInstance instance;	
	
	private SnaAgent agent;

	private volatile int callbackCount;
	private volatile int linkCallbackCount;
	private volatile int extraCallbackCount;
	private volatile int agentCallbackCount;

	
	@Before
	public void init() throws InvalidServiceProviderException, InvalidSyntaxException 
	{
		Filter filter = Mockito.mock(Filter.class);
		Mockito.when(filter.toString()).thenReturn(LOG_FILTER);		
		
		Mockito.when(context.createFilter(LOG_FILTER)).thenReturn(
				filter);
		Mockito.when(context.getServiceReferences((String)Mockito.eq(null), 
				Mockito.eq(LOG_FILTER))).thenReturn(null);		
		Mockito.when(context.getServiceReference(LOG_FILTER)
				).thenReturn(null);	

		Mockito.when(context.createFilter(AGENT_FILTER)).thenReturn(filterAgent);
		Mockito.when(filterAgent.toString()).thenReturn(AGENT_FILTER);
		
		Mockito.when(context.createFilter(DATA_STORE_FILTER)
				).thenReturn(filterDataStore);
		Mockito.when(filterDataStore.toString()).thenReturn(
				DATA_STORE_FILTER);
		
		Mockito.when(context.createFilter(AUTHENTICATION_FILTER)
				).thenReturn(filterAuthentication);
		Mockito.when(filterAuthentication.toString()).thenReturn(
				AUTHENTICATION_FILTER);

		Mockito.when(context.createFilter(ACCESS_FILTER)
				).thenReturn(filterAccess);
		Mockito.when(filterAccess.toString()).thenReturn(
				ACCESS_FILTER);

		Mockito.when(context.createFilter(VALIDATION_FILTER)
				).thenReturn(filterValidation);
		Mockito.when(filterValidation.toString()).thenReturn(
				VALIDATION_FILTER);
		
		Mockito.when(context.createFilter(AUTHORIZATION_FILTER)
				).thenReturn(filterAuthorization);
		Mockito.when(filterAuthorization.toString()).thenReturn(
				AUTHORIZATION_FILTER);	
		
		Mockito.when(filterAuthorization.match(referenceAuthorization)
				).thenReturn(true);

    	Mockito.when(context.getServiceReferences(
    		Mockito.any(Class.class),
    		Mockito.anyString())).then(
    			new Answer<Collection<ServiceReference>>()
		{
			@Override
            public Collection<ServiceReference> answer(InvocationOnMock invocation)
                    throws Throwable
            {
				Object[] arguments = invocation.getArguments();
				if(arguments==null || arguments.length !=2)
				{
					return null;
				}
				if(arguments[0]!=null && arguments[0].equals(
						SnaAgent.class))
				{
					if(agent == null)
					{
						return null;
					}
					return Collections.singletonList(referenceAgent);
					
				} else if(arguments[0]!=null && arguments[0].equals(
						SensiNactResourceModel.class)
						&& arguments[1]!=null && arguments[1].equals(
								"(uri=/serviceProvider)"))
				{
					return Collections.singletonList(referenceProvider);
					
				} else if((arguments[0]!=null && arguments[0].equals(
						AuthorizationService.class)
						&& arguments[1]==null)||(arguments[0]==null &&
						arguments[1].equals(AUTHORIZATION_FILTER)))
				{
	                return Collections.singletonList(referenceAuthorization);
	                
				} else if((arguments[0]!=null && arguments[0].equals(
						SecuredAccess.class)
						&& arguments[1]==null)||(arguments[0]==null &&
						arguments[1].equals(ACCESS_FILTER)))
				{
	                return Collections.singletonList(referenceAccess);
				} else if((arguments[0]!=null && arguments[0].equals(
                        BundleValidation.class)
                        && arguments[1]==null)||(arguments[0]==null &&
                        arguments[1].equals(VALIDATION_FILTER)))
                {
                    return Collections.singletonList(referenceValidation);
                }

				return null;	
            }
		});
    	Mockito.when(context.getServiceReferences(
    			Mockito.anyString(),
    			Mockito.anyString())).then(
    					new Answer<ServiceReference[]>()
		{
			@Override
            public ServiceReference[] answer(InvocationOnMock invocation)
                    throws Throwable
            {
				Object[] arguments = invocation.getArguments();
				if(arguments==null || arguments.length !=2)
				{
					return null;
				}
				if(arguments[0]!=null && arguments[0].equals(
						SnaAgent.class.getCanonicalName()))
				{
					if(agent== null)
					{
						return null;
					}
					return new ServiceReference[]{referenceAgent};
					
				} else if(arguments[0]!=null && arguments[0].equals(
						SensiNactResourceModel.class.getCanonicalName())
						&& arguments[1]!=null && arguments[1].equals(
								"(uri=/serviceProvider)"))
				{
					return new ServiceReference[]{referenceProvider};
					
				} else if((arguments[0]!=null && arguments[0].equals(
						AuthorizationService.class.getCanonicalName())
						&& arguments[1]==null)||(arguments[0]==null &&
						arguments[1].equals(AUTHORIZATION_FILTER)))
				{
	                return new ServiceReference[]{referenceAuthorization};
	                
				} else if((arguments[0]!=null && arguments[0].equals(
						SecuredAccess.class.getCanonicalName())
						&& arguments[1]==null)||(arguments[0]==null &&
						arguments[1].equals(ACCESS_FILTER)))
				{
	                return new ServiceReference[]{referenceAccess};
				} else if((arguments[0]!=null && arguments[0].equals(
                        BundleValidation.class.getCanonicalName())
                        && arguments[1]==null)||(arguments[0]==null &&
                        arguments[1].equals(VALIDATION_FILTER)))
                {
                    return new ServiceReference[]{referenceValidation};
                }
				return null;	
            }
		});
    	Mockito.when(context.getServiceReference(
    			Mockito.any(Class.class))).then(
    					new Answer<ServiceReference>()
		{
			@Override
            public ServiceReference answer(InvocationOnMock invocation)
                    throws Throwable
            {
				Object[] arguments = invocation.getArguments();
				if(arguments[0]!=null && arguments[0].equals(
						AuthorizationService.class))
				{
	                return referenceAuthorization;
	                
				}
				if(arguments[0]!=null && arguments[0].equals(
						SecuredAccess.class))
				{
	                return referenceAccess;
				}
                if(arguments[0]!=null && arguments[0].equals(
                        BundleValidation.class))
                {
                    return referenceValidation;
                }
				return null;	
            }
		});
		Mockito.when(context.getService(Mockito.any(ServiceReference.class
				))).then(new Answer<Object>()
		{
			@Override
            public Object answer(InvocationOnMock invocation)
                    throws Throwable
            {
				Object[] arguments = invocation.getArguments();
				if(arguments==null || arguments.length!=1)
				{
					return null;					
				} 
				else if(arguments[0]==referenceAuthorization)
				{	
					return new MyAuthorization<R>();	
					
				}
				else if(arguments[0]==referenceAgent)
				{
					return agent;						
				}
				else if(arguments[0]==referenceAccess)
				{
					return securedAccess;
					
				}
				else if(arguments[0]==referenceValidation)
                {
                    return bundleValidation;
                }
				else if(arguments[0]==referenceProvider)
				{
					return instance;
				}
				return null;	
            }				
		}); 	
		Mockito.when(context.registerService(
			Mockito.any(Class.class), 
			Mockito.any(Object.class), 
			Mockito.any(Dictionary.class))).thenAnswer(
				new Answer<ServiceRegistration>()
		{
			@Override
            public ServiceRegistration answer(InvocationOnMock invocation)
                    throws Throwable
            {
				Object[] arguments = invocation.getArguments();
				if(arguments==null || arguments.length!=3)
				{
					return null;						
				} 
				else if(SnaAgent.class.isAssignableFrom(
						(Class)arguments[0]))
				{
					TestResourceBuilder.this.agentCallbackCount = 0;
					TestResourceBuilder.this.setAgent(
					(SnaAgent) arguments[1]);
                    return registrationAgent;
					
				}else if(ModelInstance.class.isAssignableFrom(
				(Class)arguments[0]) || SensiNactResourceModel.class.isAssignableFrom(
						(Class)arguments[0]))
				{
					return snaObjectRegistration;							
				}
				return null;
            }
		});	
	 	
		Mockito.when(registration.getReference()).thenReturn(referenceProvider);
		Mockito.when(registrationAgent.getReference()).thenReturn(referenceAgent);
		
		Mockito.when(context.getBundle()).thenReturn(bundle);
		Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
		Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);
		Mockito.when(bundle.getState()).thenReturn(Bundle.ACTIVE);
		
		mediator = new Mediator(context);
    	securedAccess = new MySecuredAccess(mediator);
        bundleValidation = new BundleValidation() {
            @Override
            public String check(Bundle bundle) throws BundleValidationException {
                return "xxxxxxxxxxxxxx000000";
            }
        };
    	
        instance = new ModelInstanceBuilder(mediator, MyModelInstance.class, 
        	ModelConfiguration.class).withStartAtInitializationTime(true
        			).build("serviceProvider", null);     
        
        callbackCount = 0;
        linkCallbackCount = 0;
        extraCallbackCount = 0;
    }

	@After
	public void tearDown()
	{
		//instance.getServiceProvider().stop();
	}
	
    @Test
    public void testResourceModel() throws Exception
    {
    	ServiceImpl service = instance.getRootElement(
    			).addService("testService");
        
    	ResourceImpl r1impl = service.addDataResource(
    			PropertyResource.class, "TestProperty", 
    			String.class, "hello");  
    	
        PropertyResource r1 = r1impl.<PropertyResource>getProxy(KEY);
        
        //test shortcut
        Assert.assertEquals("TestProperty",r1.getName()); 
        Assert.assertEquals(Resource.Type.PROPERTY, r1.getType());

        String get1 = r1.get(DataResource.VALUE).getJSON();
        String get2 = r1.get().getJSON();

        JSONAssert.assertEquals(get1,get2, false); 
        SubscribeResponse res = r1.subscribe(
       		new Recipient()
       		{
				@Override
				public void callback(String callbackId, 
						SnaMessage[] messages) throws Exception
	            {	
					boolean hasChanged = ((TypedProperties<?>
					)messages[0]).<Boolean>get("hasChanged");
					
					if(!hasChanged);
					{
						extraCallbackInc();
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
				this.add(new Changed(Thread.currentThread().getContextClassLoader(),true));
			}}
		);        
        
        r1.set("hello");        
        Thread.sleep(500);
      
        Assert.assertTrue(
        	"the message should have been processed even if the value has not changed",
        	1 == extraCallbackCount);
        
    	ResourceImpl r2impl = service.addDataResource(PropertyResource.class, 
    		"TestProperty2", String.class, null); 
    	
        PropertyResource r2 = r2impl.<PropertyResource>getProxy(KEY);        		
        ResourceImpl r3impl = service.addLinkedResource("LinkedProperty", r1impl);
    	PropertyResource r3 = r3impl.<PropertyResource>getProxy(KEY);

    	service.addLinkedResource(LocationResource.LOCATION,
    		instance.getRootElement().getAdminService().getResource(
				LocationResource.LOCATION));
    	
    	ResourceImpl r4impl = service.getResource("location");

        r4impl.registerExecutor(
        		AccessMethod.Type.GET, 
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
   	    LocationResource r4 = r4impl.<LocationResource>getProxy(KEY);    	 
   	    
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
						TestResourceBuilder.this.callbackInc();	
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
						TestResourceBuilder.this.linkCallbackInc();
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
        assertEquals(1, callbackCount); 
        
        r2.set("value","property5").getJSON();
        Thread.sleep(500); 
        assertEquals(2, callbackCount);
        
        String filter ="/serviceProvider/testService/TestProperty2/value";        
        org.junit.Assert.assertEquals(1, instance.getHandler().count(filter));
        r2.unsubscribe(subId);
        org.junit.Assert.assertEquals(0, instance.getHandler().count(filter));

	    Service proxy = service.<Service>getProxy(KEY);
        SetResponse error = proxy.set("location","unknown");
        assertTrue(error.getStatusCode()==403); 

        assertEquals(1, linkCallbackCount);         
    }
    
    @Test
    public void testAgent() throws Exception
    {    	
    	ServiceImpl service1 = instance.getRootElement(
    			).addService("testService");
        
    	ResourceImpl r1impl = service1.addDataResource(
    			PropertyResource.class, "TestProperty", 
    			String.class, "hello"); 

    	ServiceImpl service2 = instance.getRootElement(
    			).addService("tostService");
        
    	ResourceImpl r2impl = service2.addDataResource(
    			PropertyResource.class, "TestProperty", 
    			String.class, "hello");
    	
       //wait for the previously generated events to be consumed
       Thread.sleep(1000);
       SnaFilter filter = new SnaFilter(mediator, 
    		   "/(serviceProvider)/(test).*", 
    		   true, false);
       
       filter.addHandledType(SnaMessage.Type.UPDATE);
       
       securedAccess.registerAgent( mediator,
       new AbstractSnaAgentCallback()
       {
		@Override
		public void doHandle(SnaLifecycleMessageImpl message){}

		@Override
		public void doHandle(SnaErrorMessageImpl message){}

		@Override
		public void doHandle(SnaResponseMessage message){}

		@Override
		public void doHandle(SnaUpdateMessageImpl message)
        {
			TestResourceBuilder.this.agentCallbackCount++;
        }

		@Override
		public  void stop()
        {	        
        }},filter);

       PropertyResource r1 = r1impl.<PropertyResource>getProxy(KEY);
       PropertyResource r2 = r2impl.<PropertyResource>getProxy(KEY);
       
   	   assertEquals(0, this.agentCallbackCount);   	   
   	   r2.set("goodbye");
		
       Thread.sleep(250);
   	   assertEquals(0, this.agentCallbackCount);
   	   
   	   r1.set("goodbye");
       Thread.sleep(250);
   	   assertEquals(1, this.agentCallbackCount);
   	   
   	   Mockito.when(context.getProperty(Mockito.anyString())).thenAnswer(
	   new Answer<Object>()
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
   	   securedAccess.registerAgent( mediator,
   	       new AbstractSnaAgentCallback()
   	       {
   			@Override
   	        public void doHandle(SnaLifecycleMessageImpl message){

   				TestResourceBuilder.this.agentCallbackCount+=1;
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
   	   
   	   instance.getRootElement().stop();
       Thread.sleep(500);
       
//	   without filter we would have received those nine following events 
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/admin/location"}
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/admin/icon"}
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/admin/friendlyName"}
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/admin/bridge"}
//     {"notification":{"lifecycle":"SERVICE_DISAPPEARING"},"uri":"/serviceProvider/admin"}
//     {"notification":{"lifecycle":"RESOURCE_DISAPPEARING"},"uri":"/serviceProvider/tostService/TestProperty"}
//     {"notification":{"lifecycle":"SERVICE_DISAPPEARING"},"uri":"/serviceProvider/tostService"}
//     {"notification":{"lifecycle":"PROVIDER_DISAPPEARING"},"uri":"/serviceProvider"}
//     as those about the testService are omitted we receive only the six others
   	   assertEquals(8, this.agentCallbackCount);
    }
    
    @Test
    public void testSecuredAccess() throws Exception
    {
    	ServiceImpl service = instance.getRootElement(
    			).addService("testService");   
    	
    	service.addLinkedResource(LocationResource.LOCATION, 
    		instance.getRootElement().getAdminService().getResource(
				LocationResource.LOCATION));
    	
    	ResourceImpl r4impl = service.getResource("location");
    	
        r4impl.registerExecutor(
        		AccessMethod.Type.GET, 
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
    	Session session = securedAccess.getAnonymousSession();  
    	
        Resource r4 = session.getResource("serviceProvider", "testService", "location");       

		Assert.assertNotNull(r4);

        StringBuilder buffer = new StringBuilder();
        buffer.append(attributeValue);
        buffer.append("_suffix");
        
        GetResponse response =  r4.get(DataResource.VALUE);
        String value =  response.getResponse(String.class,DataResource.VALUE);
        assertEquals(buffer.toString(), value);
        
        r4 = session.getResource("serviceProvider", "admin", "location");
        response =  r4.get(DataResource.VALUE);
        value =  response.getResponse(String.class,DataResource.VALUE);
        assertFalse(buffer.toString().equals(value));        
    }
    
    @Test(expected=InvalidValueException.class)
    public void testConstrained() throws Exception
    {    	
    	ServiceImpl service = instance.getRootElement(
    			).addService("testService");
    	
    	ResourceImpl r2impl = service.addDataResource(PropertyResource.class, 
    			"TestProperty2", String.class, null); 	
    	
    	Executable<Void,Object> extractor = service.getResourceValueExtractor("TestProperty2");
    	
        Attribute valueAttribute = r2impl.getAttribute(DataResource.VALUE);
        valueAttribute.addMetadata(new Metadata(
        		mediator, Metadata.CONSTRAINTS, Constraint[].class, 
        		new Constraint[]
        		{
        			new MinLength(mediator.getClassLoader(), 5, false),
        			new MaxLength(mediator.getClassLoader(), 10, false)
        			
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
    	ServiceImpl service = instance.getRootElement(
    			).addService("testService");
    	
    	ServiceProvider proxy = instance.getRootElementProxy(KEY);
    	
    	List<Service> services = proxy.getServices();
    	Assert.assertEquals(2, services.size());
    	
    	instance.getRootElement().addService("dynamicService");
    	
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
    	ServiceImpl service = instance.getRootElement(
    			).addService("testService");  
    	
    	ResourceImpl action = service.addActionResource(
    			"action", ActionResource.class);

    	final AtomicInteger counter = new AtomicInteger(0);
    	
    	action.registerExecutor(
    			new Signature(mediator, AccessMethod.Type.ACT,
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

    	action.registerExecutor(
    			new Signature(mediator, AccessMethod.Type.ACT,
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
    			AccessMethod.Type.ACT);
    			
    	method = LinkedActMethod.class.getDeclaredMethod("createShortcut", new Class<?>[]
    			{Signature.class, Shortcut.class});
    	method.setAccessible(true);
    	
    	method.invoke(linkedActMethod, new Object[]{
    			new Signature(mediator, 
    					AccessMethod.Type.ACT, new Class<?>[]{int.class}, 
    					new String[]{"count"}),
    			new Shortcut(mediator, 
    					AccessMethod.Type.ACT, new Class<?>[0], new String[0], 
    					new HashMap<Integer,Parameter>(){{this.put(0,
    							new Parameter(mediator,"count",int.class,4));
    					}})});

    	//securedAccess.getAnonymousSession().registered(instance);
        //instance.getServiceProvider().start();
    	
    	ServiceProvider proxy = (ServiceProvider) instance.getRootElement().getProxy(KEY);
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
    	ServiceImpl service = instance.getRootElement(
    			).addService("testService");

    	ResourceImpl r1impl = service.addActionResource(
    			"TestAction", ActionResource.class); 	
    	
    	ResourceImpl r2impl = service.addDataResource(
    			StateVariableResource.class, "TestVariable", 
    			String.class, "untriggered"); 	

       service.addActionTrigger("TestAction", "TestVariable", 
    		   new Signature(mediator, AccessMethod.Type.ACT, null, null),
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
       ActionResource proxy = (ActionResource) r1impl.getProxy(KEY);
       proxy.act();
       assertEquals("triggered", r2impl.getAttribute(DataResource.VALUE).getValue());    		   
    }
    
    private final void setAgent(SnaAgent agent)
    {
    	this.agent = agent;
    }

    private final void callbackInc()
    {
    	this.callbackCount+=1;
    }    

    private final void extraCallbackInc()
    {
    	this.extraCallbackCount+=1;
    }    

    private final void linkCallbackInc()
    {
    	this.linkCallbackCount+=1;
    }
}
