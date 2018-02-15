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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ModelInstanceBuilder;
import org.eclipse.sensinact.gateway.core.SensiNact;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;
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

/**
 * Test Context
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class TestContext<R extends ModelInstance>
{          
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

	private static final String FILTERING_FILTER = "("+Constants.OBJECTCLASS+"="+
		Filtering.class.getCanonicalName()+")";
	
	private static final String MOCK_BUNDLE_NAME = "MockedBundle";
	private static final long MOCK_BUNDLE_ID = 1;
	
	private final Filter filterAgent = Mockito.mock(Filter.class);	
	private final Filter filterAccess = Mockito.mock(Filter.class);
	private final Filter filterValidation = Mockito.mock(Filter.class);
	private final Filter filterDataStore = Mockito.mock(Filter.class);
	private final Filter filterAuthentication = Mockito.mock(Filter.class);
	private final Filter filterAuthorization = Mockito.mock(Filter.class);
	private final Filter filtering = Mockito.mock(Filter.class);
	
	private final BundleContext context = Mockito.mock(BundleContext.class);
	private final Bundle bundle = Mockito.mock(Bundle.class);

    private final ServiceReference referenceAgent = 
			Mockito.mock(ServiceReference.class);
	private final ServiceRegistration referenceCore = 
			Mockito.mock(ServiceRegistration.class);
	private final ServiceReference referenceAuthorization = 
			Mockito.mock(ServiceReference.class);
	private final ServiceReference referenceAccess = 
			Mockito.mock(ServiceReference.class);
	private final ServiceReference referenceValidation = 
			Mockito.mock(ServiceReference.class);
	private final ServiceRegistration registration = 
			Mockito.mock(ServiceRegistration.class);
	private final ServiceRegistration registrationAccess = 
			Mockito.mock(ServiceRegistration.class);
	private final ServiceRegistration registrationValidation = 
			Mockito.mock(ServiceRegistration.class);
	private final ServiceRegistration registrationAgent = 
			Mockito.mock(ServiceRegistration.class);
	private final ServiceReference referenceProvider = 
			Mockito.mock(ServiceReference.class);
	
	private final ServiceReference filteringReference = 
			Mockito.mock(ServiceReference.class);
	private final ServiceRegistration registrationFiltering = 
			Mockito.mock(ServiceRegistration.class);

	private MyModelInstance instance;	
	private SnaAgent agent;

	private volatile int callbackCount;
	private volatile int linkCallbackCount;
	private volatile int extraCallbackCount;
	private volatile int agentCallbackCount;
	
	private SecuredAccess securedAccess;
	private SensiNact sensinact;
	private Mediator mediator;
	
	protected Dictionary<String,Object> props;
	
	
	public TestContext() 
			throws InvalidServiceProviderException, InvalidSyntaxException, 
			SecuredAccessException, BundleException 
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
		Mockito.when(filterAgent.match(referenceAgent)).thenReturn(true);
		
		Mockito.when(context.createFilter(DATA_STORE_FILTER)).thenReturn(filterDataStore);
		Mockito.when(filterDataStore.toString()).thenReturn(DATA_STORE_FILTER);
		
		Mockito.when(context.createFilter(AUTHENTICATION_FILTER)).thenReturn(filterAuthentication);
		Mockito.when(filterAuthentication.toString()).thenReturn(AUTHENTICATION_FILTER);

		Mockito.when(context.createFilter(ACCESS_FILTER)).thenReturn(filterAccess);
		Mockito.when(filterAccess.toString()).thenReturn(ACCESS_FILTER);
		Mockito.when(filterAccess.match(referenceAccess)).thenReturn(true);

		Mockito.when(context.createFilter(VALIDATION_FILTER)).thenReturn(filterValidation);
		Mockito.when(filterValidation.toString()).thenReturn(VALIDATION_FILTER);		
		Mockito.when(filterValidation.match(referenceValidation)).thenReturn(true);
		
		Mockito.when(context.createFilter(AUTHORIZATION_FILTER)).thenReturn(filterAuthorization);
		Mockito.when(filterAuthorization.toString()).thenReturn(AUTHORIZATION_FILTER);		
		Mockito.when(filterAuthorization.match(referenceAuthorization)).thenReturn(true);

		Mockito.when(context.createFilter(FILTERING_FILTER)).thenReturn(filtering);
		Mockito.when(filtering.toString()).thenReturn(FILTERING_FILTER);
		Mockito.when(filtering.match(filteringReference)).thenReturn(true);
        
		Mockito.when(context.getServiceReferences(
	    		Mockito.any(Class.class), Mockito.anyString())).then(
	    			new Answer<Collection<ServiceReference>>()
			{
				@Override
	            public Collection<ServiceReference> answer(
	            		InvocationOnMock invocation)
	                    throws Throwable
	            {
					Object[] arguments = invocation.getArguments();
					if(SnaAgent.class.equals(arguments[0]))
					{
						return Collections.singletonList(referenceAgent);
						
					} else if(SensiNactResourceModel.class.equals(arguments[0]))
					{
						if(initialized())
						{
							return Collections.singletonList(referenceProvider);
							
						} else
						{
							return Collections.emptyList();
						}
					} else if((Filtering.class.equals(arguments[0])))
					{
		                return Collections.singletonList(filteringReference);
		                
					} else if((AuthorizationService.class.equals(arguments[0])
							&& arguments[1]==null)||(arguments[0]==null &&
							arguments[1].equals(AUTHORIZATION_FILTER)))
					{
		                return Collections.singletonList(referenceAuthorization);
		                
					} else if((SecuredAccess.class.equals(arguments[0])&& 
							arguments[1]==null)||(arguments[0]==null &&
							arguments[1].equals(ACCESS_FILTER)))
					{
		                return Collections.singletonList(referenceAccess);
		                
					} else if((BundleValidation.class.equals(arguments[0])
							&& arguments[1]==null)||(arguments[0]==null &&
							arguments[1].equals(VALIDATION_FILTER)))
					{
		                return Collections.singletonList(referenceValidation);
					}
					return null;	
	            }
			});
    	Mockito.when(context.getServiceReferences(
    		Mockito.anyString(), Mockito.anyString())).then(
    				new Answer<ServiceReference[]>()
		{
			@Override
            public ServiceReference[] answer(InvocationOnMock invocation)
                    throws Throwable
            {
				Object[] arguments = invocation.getArguments();
				if(SnaAgent.class.getCanonicalName().equals(arguments[0])
					||(arguments[0]==null && arguments[1].equals(AGENT_FILTER)))
				{
					return new ServiceReference[]{referenceAgent};
					
				} else if(SensiNactResourceModel.class.getCanonicalName(
						).equals(arguments[0]))
				{ 
					if(initialized())
					{
						return new ServiceReference[]{referenceProvider};
					} else
					{
						return new ServiceReference[]{};
					}
					
				} else if((Filtering.class.getCanonicalName().equals(arguments[0])))
				{
	                return new ServiceReference[]{filteringReference};
	                
				} else if((AuthorizationService.class.getCanonicalName().equals(arguments[0])
						&& arguments[1]==null)||(arguments[0]==null &&
						arguments[1].equals(AUTHORIZATION_FILTER)))
				{
	                return new ServiceReference[]{referenceAuthorization};
	                
				} else if((SecuredAccess.class.getCanonicalName().equals(arguments[0])
						&& arguments[1]==null)||(arguments[0]==null &&
						arguments[1].equals(ACCESS_FILTER)))
				{
	                return new ServiceReference[]{referenceAccess};
	                
				} else if((BundleValidation.class.getCanonicalName().equals(arguments[0])
						&& arguments[1]==null)||(arguments[0]==null &&
						arguments[1].equals(VALIDATION_FILTER)))
				{
	                return new ServiceReference[]{referenceValidation};
				}
				return null;	
            }
		});
    	Mockito.when(context.getServiceReference(
    		Mockito.any(Class.class))).then(new Answer<ServiceReference>()
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
	                
				}if(arguments[0]!=null && arguments[0].equals(
						SecuredAccess.class))
				{
	                return referenceAccess;
	                
				}
				if(arguments[0]!=null && arguments[0].equals(
						Filtering.class))
				{
	                return filteringReference;
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
				else if(arguments[0]==filteringReference)
				{	
					return new XFilter();	
				}
				else if(arguments[0]==referenceAgent)
				{
					return TestContext.this.getAgent();						
				}
				else if(arguments[0]==referenceAccess)
				{
					return TestContext.this.getSecuredAccess();
					
				}
				else if(arguments[0]==referenceCore)
				{
					return sensinact;
					
				}else if(arguments[0] ==referenceProvider)
				{
					return instance;
					
				} else if (arguments[0] == referenceValidation)
				{
					return new BundleValidation()
					{
						@Override
						public String check(Bundle bundle) 
								throws BundleValidationException
						{							
							return "xxxxxxxxxxx00000000";
						}
					};
				}
				return null;	
            }				
		}); 	
		Mockito.when(context.registerService(Mockito.any(Class.class), 
			Mockito.any(Object.class), Mockito.any(Dictionary.class))
			).thenAnswer(new Answer<ServiceRegistration>()
		{
			@Override
            public ServiceRegistration answer(InvocationOnMock invocation)
                    throws Throwable
            {
				Object[] arguments = invocation.getArguments();
				if(SecuredAccess.class.equals(arguments[0]))
				{
					securedAccess = (SecuredAccess) arguments[1];
                    return registrationAccess;
					
				}else if(SnaAgent.class.equals(arguments[0]))
				{
					TestContext.this.setAgent((SnaAgent) arguments[1]);
                    return registrationAgent;
					
				}else if(SensiNactResourceModel.class.equals(arguments[0]))
				{
					TestContext.this.props = (Dictionary<String, Object>)
							arguments[2];
					return registration;	
					
				}else if(BundleValidation.class.equals(arguments[0]))
				{
					return registrationValidation;				
				}
				return null;
            }
		});	 	
		Mockito.when(referenceProvider.getProperty(Mockito.anyString())
				).thenAnswer(new Answer<Object>()
				{
					@Override
					public Object answer(InvocationOnMock invocation)
							throws Throwable
					{
						return TestContext.this.props.get(
								invocation.getArguments()[0]);
					}
			
				});	
		Mockito.when(referenceProvider.getPropertyKeys()
				).thenAnswer(new Answer<String[]>()
				{
					@Override
					public String[] answer(InvocationOnMock invocation)
							throws Throwable
					{
						Enumeration<String> e = TestContext.this.props.keys();
						List<String> l = new ArrayList<String>();
						while(e.hasMoreElements())
						{
							l.add(e.nextElement());
						}
						return l.toArray(new String[0]);
					}
			
				});
		Mockito.doAnswer(new Answer<Void>()
		{
			@Override
			public Void answer(InvocationOnMock invocation)
					throws Throwable
			{
				TestContext.this.props= (Dictionary<String, Object>) 
						invocation.getArguments()[0];
				return null;
			}
	
		}).when(registration).setProperties(Mockito.any(
				Dictionary.class));

		Mockito.when(registration.getReference()).thenReturn(referenceProvider);
		Mockito.when(registrationAgent.getReference()).thenReturn(referenceAgent);
		Mockito.when(registrationValidation.getReference()).thenReturn(referenceValidation);
		
		Mockito.when(context.getBundle()).thenReturn(bundle);
		Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
		Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);
		Mockito.when(bundle.getState()).thenReturn(Bundle.ACTIVE);
		
		mediator = new Mediator(context);

    	sensinact = new SensiNact(mediator);
        instance = new ModelInstanceBuilder(mediator, MyModelInstance.class, 
        	ModelConfiguration.class).withStartAtInitializationTime(true
        			).build("serviceProvider", null); 
        initialized=true;
    	
        callbackCount = 0;
        linkCallbackCount = 0;
        extraCallbackCount = 0;
        agentCallbackCount = 0;
    }

    public final SnaAgent getAgent()
    {
    	return this.agent;
    }
    
    public final SensiNact getSensiNact()
    {
    	return this.sensinact;
    }

    public final ModelInstance getModelInstance()
    {
    	return this.instance;
    }
    
    public final Mediator getMediator()
    {
    	return this.mediator;
    }
    
    public final SecuredAccess getSecuredAccess()
    {
    	return this.securedAccess;
    }

    public final int getCallbackCount()
    {
    	return this.callbackCount;
    }    

    public final int getExtraCallbackCount()
    {
    	return this.extraCallbackCount;
    }    

    public final int getLinkCallbackCount()
    {
    	return this.linkCallbackCount;
    }

    public final int getAgentCallbackCount()
    {
    	return this.agentCallbackCount;
    }
    
    private boolean initialized = false;
    
    final boolean initialized()
    {
    	return initialized;
    }

	public void stop()
	{
		sensinact.close();
	}
    
    private final void setAgent(SnaAgent agent)
    {
    	this.agent = agent;
    }
    
    final void callbackInc()
    {
    	this.callbackCount+=1;
    }    

    final void extraCallbackInc()
    {
    	this.extraCallbackCount+=1;
    }    

    final void linkCallbackInc()
    {
    	this.linkCallbackCount+=1;
    }

	final void agentCallbackInc()
	{
    	this.agentCallbackCount+=1;
	}

	public void agentCallbackCountReset()
	{
		this.agentCallbackCount=0;
	}
}
