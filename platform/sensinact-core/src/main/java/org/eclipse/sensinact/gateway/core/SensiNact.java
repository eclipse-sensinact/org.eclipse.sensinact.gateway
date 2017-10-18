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
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgentImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.ActResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessFactory;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.core.security.SessionKey;
import org.eclipse.sensinact.gateway.core.security.Sessions;
import org.eclipse.sensinact.gateway.core.security.Sessions.KeyExtractor;
import org.eclipse.sensinact.gateway.core.security.Sessions.KeyExtractorType;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * {@link Core} service implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SensiNact implements Core
{	
	//********************************************************************//
	//						NESTED DECLARATIONS		    				  //
	//********************************************************************//
	
	/**
	 * {@link Session} service implementation
	 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
	 */
	final class SensiNactSession implements Session
	{	    	
		private Mediator mediator;
		private final String identifier;

		/**
		 * Constructor
		 * 
		 * @param mediator the {@link Mediator} allowing the Session 
		 * to be instantiated to interact with the OSGi host environment
		 * @param identifier the String identifier of the Session
		 * to be instantiated
		 */
		public SensiNactSession(Mediator mediator, String identifier)
		{
			this.mediator = mediator;
			this.identifier = identifier;
		}

		/**
		 * return String identifier of this Session
		 * 
		 * @return this Session's identifier
		 */
		public String getId()
		{
			return this.identifier;
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.security.Session#
		 * registerSessionAgent(org.eclipse.sensinact.gateway.core.message.SnaAgentCallback, org.eclipse.sensinact.gateway.core.message.SnaFilter)
		 */
		public String registerSessionAgent(final SnaAgentCallback callback, 
				final SnaFilter filter)
		{			
			return AccessController.<String>doPrivileged(
			new PrivilegedAction<String>()
			{
				@Override
				public String run()
				{
					SessionKey key = SensiNact.this.sessions.get(getId());
					if(key == null || key.getPublicKey()==null)
					{
						return null;
					}
					return key.registerAgent(callback, filter);
				}
			});
		}
		
		/** 
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.security.Session#
		 * getServiceProviders()
		 */
		@Override
	    public Set<ServiceProvider> serviceProviders()
	    {
		    return this.serviceProviders(null);
	    }

		/** 
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.security.Session#
		 * getServiceProviders()
		 */
		@Override
	    public Set<ServiceProvider> serviceProviders(final String filter)
	    {
			return AccessController.doPrivileged(
			 new PrivilegedAction<Set<ServiceProvider>>()
			 {
				@Override
	            public Set<ServiceProvider> run()
	            {
			    	return SensiNact.this.serviceProviders(
			    		SensiNactSession.this.getId(),
			    		filter);
	            }
			 });
	    }
	    
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.security.Session#
		 * getServiceProvider(java.lang.String)
		 */
	    @Override
	    public ServiceProvider serviceProvider(
	    		final String serviceProviderName)
	    {
	    	ServiceProvider provider = AccessController.doPrivileged(
	    				new PrivilegedAction<ServiceProvider>()
			{
				@Override
                public ServiceProvider run()
                {
					return SensiNact.this.serviceProvider(
			    			SensiNactSession.this.getId(),
							serviceProviderName);
                }
			});
	    	return provider;        	    	
	    }

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.security.Session#
		 * getService(java.lang.String, java.lang.String)
		 */
	    @Override
	    public Service service(
	    		final String serviceProviderName, 
	    		final String serviceName)
	    {
	    	Service service = null;
	    	ServiceProvider provider = this.serviceProvider(serviceProviderName);
	    	if(provider != null)
	    	{
	    		service = provider.getService(serviceName);
	    	}
	    	return service;
	    }

		/**
		 * @inheritDoc
		 * 
		 * @see org.eclipse.sensinact.gateway.core.security.Session#
		 * getResource(java.lang.String, java.lang.String, java.lang.String)
		 */
	    @Override
	    public Resource resource(
	    		final String serviceProviderName, 
	    		final String serviceName,
	            final String resourceName)
	    {
	    	Resource resource = null;
	    	Service service = null;
	    	if((service = this.service(serviceProviderName, serviceName))!= null)
	    	{
	    		resource = service.getResource(resourceName);
	    	}
	    	return resource;
	    }
		
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonAll()
		 */
		@Override
	    public JSONObject getAll()
	    {
			return this.getAll(null);
	    }
		
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonAll(java.lang.String)
		 */
		@Override
	    public JSONObject getAll(final String filter)
	    {
			 return AccessController.doPrivileged(
					 new PrivilegedAction<JSONObject>()
			 {
				@Override
	            public JSONObject run()
	            {
			    	return SensiNact.this.getAll(
			    		SensiNactSession.this.getId(), filter);
	            }
			 });
	    }

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonLocations()
		 */
		@Override
		public JSONObject getLocations()
		{
			 return AccessController.doPrivileged(
					 new PrivilegedAction<JSONObject>()
			 {
				@Override
	            public JSONObject run()
	            {
			    	return SensiNact.this.getLocations(
			    			SensiNactSession.this.getId());
	            }
			 });
		}
		
	    /**
	     * @inheritDoc
	     *
	     * @see org.eclipse.sensinact.gateway.core.Endpoint#
	     * jsonGet(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	     */
	    public JSONObject get(final String serviceProviderId, 
	    	final String serviceId, final String resourceId,
	    	    final String attributeId)
	    {	
	        Resource resource = this.resource(serviceProviderId, serviceId, 
	        		resourceId);
	        
	        if(resource == null)
	        {
	        	return AccessController.doPrivileged(
	        			new PrivilegedAction<JSONObject>()
				 {
					@Override
		            public JSONObject run()
		            {
				    	return SensiNact.this.get( 
				    		SensiNactSession.this.getId(),
				    		serviceProviderId, 
				    		serviceId, 
				    		resourceId, 
				    		attributeId);
		            }
				 });
	        }
	        GetResponse response = null;        	        
	        if(attributeId==null)
	        {
	        	if(!resource.getType().equals(Resource.Type.ACTION))
				{
					response = ((DataResource) resource).get();
				}else
				{
					String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
                		).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
                			).append(serviceId).append(UriUtils.PATH_SEPARATOR
                				).append(resourceId).toString();
					
					response = (GetResponse) AccessMethodResponse.error(
						mediator, uri, AccessMethod.Type.valueOf(AccessMethod.GET), 
						    420, "Unknown method", null);
				}
	        } else
	        {
	        	response = resource.get(attributeId);
	        }
	        JSONObject object= new JSONObject(response.getJSON());
    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).append(UriUtils.PATH_SEPARATOR
        						).append(resourceId).toString();
        		object.remove("uri");
        		object.put("uri", uri);
        	}
        	return object;
	    }

	    /**
	     * @inheritDoc
	     *
	     * @see org.eclipse.sensinact.gateway.core.Endpoint#
	     * jsonSet(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	     */
	    public JSONObject set(final String serviceProviderId, final String serviceId,
        final String resourceId, final String attributeId, final Object parameter)
		{	 
			 Resource resource = this.resource(serviceProviderId, 
					 serviceId, resourceId);
			 if(resource == null)
			 {
				 return AccessController.doPrivileged(new PrivilegedAction<JSONObject>()
				 {
					@Override
		            public JSONObject run()
		            {
				    	return SensiNact.this.set(
				    		SensiNactSession.this.getId(),
				    		serviceProviderId, 
				    		serviceId,
				    		resourceId, 
				    		attributeId, 
				    		parameter);
		            }
				 });
			}					 
	        SetResponse response = null;
	        if(attributeId==null)
	        {
	        	if(!resource.getType().equals(Resource.Type.ACTION))
				{
					response = ((DataResource) resource).set(parameter);
					
				}else
				{
					String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
                		).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
                			).append(serviceId).append(UriUtils.PATH_SEPARATOR
                				).append(resourceId).toString();
					
					response = (SetResponse) AccessMethodResponse.error(
						mediator, uri,  AccessMethod.Type.valueOf(AccessMethod.SET), 
							420, "Unknown method", null);
				}
	        } else
	        {
	        	response = resource.set(attributeId,parameter);
	        }        
	        JSONObject object= new JSONObject(response.getJSON());

    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).append(UriUtils.PATH_SEPARATOR
        						).append(resourceId).toString();
        		object.remove("uri");
        		object.put("uri", uri);
        	}
        	return object;
	    }

	    /**
	     * @inheritDoc
	     *
	     * @see org.eclipse.sensinact.gateway.core.Endpoint#
	     * jsonAct(java.lang.String, java.lang.String, java.lang.String, java.lang.Object[])
	     */
	    public JSONObject act(final String serviceProviderId, final String serviceId,
		        final String resourceId, final Object[] parameters)
		{ 
			 Resource resource = this.resource(serviceProviderId, serviceId, resourceId);
			 if(resource == null)
			 {
				 return AccessController.doPrivileged(new PrivilegedAction<JSONObject>()
				 {
					@Override
		            public JSONObject run()
		            {
				    	return SensiNact.this.act(
				    		SensiNactSession.this.getId(),
				    		serviceProviderId,
				    		serviceId, 
				    		resourceId,
				    		parameters);
		            }
				 });
			 }
			 ActResponse response = null;
			 if(!resource.getType().equals(Resource.Type.ACTION))
			 {
				 String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
                		).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
                			).append(serviceId).append(UriUtils.PATH_SEPARATOR
                				).append(resourceId).toString();
					
				 response = (ActResponse) AccessMethodResponse.error(
					mediator, uri,  AccessMethod.Type.valueOf(AccessMethod.ACT), 
					    420, "Unknown method", null);
			} else
			{
				response = ((ActionResource) resource).act(parameters);
			}
	        JSONObject object= new JSONObject(response.getJSON());

    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).append(UriUtils.PATH_SEPARATOR
        						).append(resourceId).toString();
        		object.remove("uri");
        		object.put("uri", uri);
        	}
        	return object;
	    }

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#
		 * jsonSubscribe(java.lang.String, java.lang.String, java.lang.String, org.eclipse.sensinact.gateway.core.message.Recipient, org.json.JSONArray)
		 */
		public JSONObject subscribe(final String serviceProviderId,
		    final String serviceId, final String resourceId, 
		    final Recipient recipient, final JSONArray conditions)
		{  		
			Resource resource = this.resource(serviceProviderId,serviceId, resourceId);
			if(resource == null)
			{
				 return AccessController.doPrivileged(
						 new PrivilegedAction<JSONObject>()
				 {
					@Override
		            public JSONObject run()
		            {
				    	return SensiNact.this.subscribe(
				    		SensiNactSession.this.getId(),
				    		serviceProviderId,
				    		serviceId, 
				    		resourceId, 
				    		recipient, 
				    		conditions);
		            }
				 });
			}
			SubscribeResponse response = null;
        	if(!resource.getType().equals(Resource.Type.ACTION))
			{
    	        Constraint constraint = null;
    	        if(conditions!=null && conditions.length()>0)
    	        {
    		        try 
    		        {
    					constraint = ConstraintFactory.Loader.load(
    							mediator.getClassLoader(), conditions);
    					
    				} catch (InvalidConstraintDefinitionException e)
    		        {
    					mediator.error(e.getMessage(),e);	
    				}
    	        }
				response = ((DataResource) resource
	        	        	).subscribe(recipient, (constraint==null
	        	        	?Collections.<Constraint>emptySet()
	        	        	:Collections.<Constraint>singleton(constraint)));						
			} else
			{
				String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
            		).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
            			).append(serviceId).append(UriUtils.PATH_SEPARATOR
            				).append(resourceId).toString();
				
				response = (SubscribeResponse) AccessMethodResponse.error(
				    mediator, uri,  AccessMethod.Type.valueOf(AccessMethod.SUBSCRIBE), 
				        420, "Unknown method", null);
			}
	        JSONObject object= new JSONObject(response.getJSON());

    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).append(UriUtils.PATH_SEPARATOR
        						).append(resourceId).toString();
        		object.remove("uri");
        		object.put("uri", uri);
        	}
        	return object;
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonUnsubscribe(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		public JSONObject unsubscribe(final String serviceProviderId,
		        final String serviceId, final String resourceId, 
		        final String subscriptionId)
		{     		
			Resource resource = this.resource(serviceProviderId, serviceId, resourceId);
			if(resource == null)
			{
				return AccessController.doPrivileged(new PrivilegedAction<JSONObject>()
				{
					@Override
		            public JSONObject run()
		            {
				    	return SensiNact.this.unsubscribe(
				    		SensiNactSession.this.getId(),
				    		serviceProviderId,
				    		serviceId, 
				    		resourceId, 
				    		subscriptionId);
		            }
				});
			}
			UnsubscribeResponse response =null;
        	if(!resource.getType().equals(Resource.Type.ACTION))
			{ 	        
				response =  ((DataResource) resource
        	        	).unsubscribe(subscriptionId);						
			} else
			{
				String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
            		).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
            			).append(serviceId).append(UriUtils.PATH_SEPARATOR
            				).append(resourceId).toString();
				
				response = (UnsubscribeResponse) AccessMethodResponse.error(
					mediator, uri,  AccessMethod.Type.valueOf(AccessMethod.UNSUBSCRIBE), 
					    420, "Unknown method", null);
			}
	        JSONObject object= new JSONObject(response.getJSON());

    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		String uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).append(UriUtils.PATH_SEPARATOR
        						).append(resourceId).toString();
        		object.remove("uri");
        		object.put("uri", uri);
        	}
        	return object;
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonProviders()
		 */
		@Override
		public JSONObject getProviders()
		{
      		 
			 return AccessController.doPrivileged(
					 new PrivilegedAction<JSONObject>()
			 {
				@Override
	            public JSONObject run()
	            {
			    	return SensiNact.this.getProviders(
			    		SensiNactSession.this.getId());
	            }
			 });
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonProvider(java.lang.String)
		 */
		@Override
		public JSONObject getProvider(final String serviceProviderId)
		{	 
			ServiceProvider serviceProvider= this.serviceProvider(
					serviceProviderId);

	        if (serviceProvider == null)
	        {
       			 return AccessController.doPrivileged(
       					 new PrivilegedAction<JSONObject>()
       			 {
       				@Override
       	            public JSONObject run()
       	            {
       			    	return SensiNact.this.getProvider(
    			    		SensiNactSession.this.getId(),
       			    		serviceProviderId);
       	            }
       			 });
	        }
	        String uri = null;
    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		 uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).toString();
        	} else
        	{
        		uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(serviceProviderId).toString();
        	}
        	JSONObject jsonDevice = new JSONObject();
        	jsonDevice.put("type", "DESCRIBE_RESPONSE");
        	jsonDevice.put("uri", uri );
        	jsonDevice.put("statusCode", 200);
        	jsonDevice.put("response", new JSONObject(
        			serviceProvider.getDescription().getJSON()));
            return jsonDevice;       	        
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonServices(java.lang.String)
		 */
		@Override
		public JSONObject getServices(final String serviceProviderId)
		{	
			ServiceProvider provider = this.serviceProvider(serviceProviderId);
			if(provider == null)
			{
    			 return AccessController.doPrivileged(new PrivilegedAction<JSONObject>()
    			 {
    				@Override
    	            public JSONObject run()
    	            {
    			    	return SensiNact.this.getServices(
    			    		SensiNactSession.this.getId(),
    			    		serviceProviderId);
    	            }
    			 });
			}
	        String uri = null;
    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		 uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).toString();
        	} else
        	{
        		uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(serviceProviderId).toString();
        	}
			List<Service> services = provider.getServices();
 	        JSONArray servicesJson = new JSONArray();
            for (Service service : services)
            {
                servicesJson.put(service.getName());
            }
            JSONObject jsonDevice = new JSONObject();
        	jsonDevice.put("type", "SERVICES_LIST");
        	jsonDevice.put("uri", uri);
        	jsonDevice.put("statusCode", 200);
        	jsonDevice.put("services", servicesJson);        	
            return jsonDevice;
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonService(java.lang.String, java.lang.String)
		 */
		@Override
		public JSONObject getService(final String serviceProviderId, final String serviceId)
		{     		
			Service service = this.service(serviceProviderId, serviceId);
			if(service == null)
			{
    			 return AccessController.doPrivileged(new PrivilegedAction<JSONObject>()
    			 {
    				@Override
    	            public JSONObject run()
    	            {
    			    	return SensiNact.this.getService(
    			    		SensiNactSession.this.getId(),
    			    		serviceProviderId, 
    			    		serviceId);
    	            }
    			 });
			}
	        String uri = null;
    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		 uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).toString();
        	} else
        	{
        		uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).toString();
        	}
        	JSONObject jsonService = new JSONObject();
        	jsonService.put("type", "DESCRIBE_RESPONSE");
        	jsonService.put("uri",uri);        	
        	jsonService.put("statusCode", 200);
        	jsonService.put("response", new JSONObject(
        			service.getDescription().getJSON()));

            return jsonService;
		}
		
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#
		 * jsonResources(java.lang.String, java.lang.String)
		 */
		@Override
		public JSONObject getResources(
				final String serviceProviderId, 
				final String serviceId)
		{     		 
			Service service = this.service(serviceProviderId, serviceId);
			if(service == null)
			{
    			 return AccessController.doPrivileged(
    			 new PrivilegedAction<JSONObject>()
    			 {
    				@Override
    	            public JSONObject run()
    	            {
    			    	return SensiNact.this.getResources(
    			    		SensiNactSession.this.getId(),
    			    		serviceProviderId, 
    			    		serviceId);
    	            }
    			 });
			}
	        String uri = null;
    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		 uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).toString();
        	} else
        	{
        		uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).toString();
        	}
			JSONArray resourcesJson = new JSONArray();
	        List<Resource> resources = service.getResources();

            for (Resource resource : resources)
            {
                resourcesJson.put(resource.getName());
            }
            JSONObject jsonResources = new JSONObject();
        	jsonResources.put("type", "RESOURCES_LIST");
        	jsonResources.put("uri",uri);        	
        	jsonResources.put("statusCode", 200);
        	jsonResources.put("resources", resourcesJson);
	        	
	        return jsonResources;
		}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonResource(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public JSONObject getResource(
				final String serviceProviderId,
				final String serviceId,
		        final String resourceId)
		{      		 
			Resource resource = this.resource(serviceProviderId,serviceId,resourceId);
			if(resource == null)
			{
    			 return AccessController.doPrivileged(new PrivilegedAction<JSONObject>()
    			 {
    				@Override
    	            public JSONObject run()
    	            {
    			    	return SensiNact.this.getResource(
    			    		SensiNactSession.this.getId(),
    			    		serviceProviderId, 
    			    		serviceId,
    			    		resourceId);
    	            }
    			 });
			}
	        String uri = null;
    		SessionKey sessionKey = SensiNact.this.sessions.get(
    				new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN, 
    						this.getId()));
        	if(sessionKey.localID()!=0)
        	{
        		 uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(SensiNact.this.namespace()).append(":"
        				).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).append(UriUtils.PATH_SEPARATOR
        							).append(resourceId).toString();
        	} else
        	{
        		uri = new StringBuilder().append(UriUtils.PATH_SEPARATOR
        			).append(serviceProviderId).append(UriUtils.PATH_SEPARATOR
        					).append(serviceId).append(UriUtils.PATH_SEPARATOR
        							).append(resourceId).toString();
        	}
        	JSONObject jsonResource = new JSONObject();
        	jsonResource.put("type", "DESCRIBE_RESPONSE");
        	jsonResource.put("uri", uri);        	
        	jsonResource.put("statusCode", 200);
        	jsonResource.put("response", new JSONObject(
        		resource.getDescription().getDescription()));
            return jsonResource;
		}
	};
	
	/**
	 * endpoint of the local OSGi host environment
	 * 
	 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
	 */
	final class RegistryEndpoint 
	{		
		/**
		 * @param publicKey
		 * @param filter
		 * @return
		 */
		private Collection<ServiceReference<SensiNactResourceModel>> 
		getReferences(SessionKey sessionKey, String filter)
		{
			AccessTree<? extends AccessNode> tree = sessionKey.getAccessTree();
			AccessMethod.Type describe = AccessMethod.Type.valueOf(
					AccessMethod.DESCRIBE);
			
			Collection<ServiceReference<SensiNactResourceModel>> result = 
					new ArrayList<ServiceReference<SensiNactResourceModel>>();
			
			Collection<ServiceReference<SensiNactResourceModel>> references = 
					null;  
			try
	        {
	            references = SensiNact.this.mediator.getContext(
	                ).getServiceReferences(SensiNactResourceModel.class,
	            	    filter);
	            Iterator<ServiceReference<SensiNactResourceModel>> iterator = 
						references.iterator();
	            
				while(iterator.hasNext())
				{
					ServiceReference<SensiNactResourceModel> reference = iterator.next();					
					String name = (String) reference.getProperty("name");
					Integer level = (Integer) reference.getProperty(name.concat(".DESCRIBE"));
					if(level == null)
					{
						level = new Integer(
							AccessLevelOption.OWNER.getAccessLevel().getLevel());
					}
					AccessNode node = sessionKey.getAccessTree().getRoot().get(
						UriUtils.getUri(new String[] {name}));
					
					if(node == null)
					{
						node = tree.getRoot();
					}
					if(node.getAccessLevelOption(describe
						).getAccessLevel().getLevel() >= level.intValue())
					{
						result.add(reference);
					}
				}
	        }
	        catch (InvalidSyntaxException e)
	        {
	            mediator.error(e.getMessage(),e);
	        }  
			return result;
		}
		
		/**
		 * @param publicKey
		 * @param filter
		 * @return
		 */
		private Set<ServiceProvider> serviceProviders(
				final SessionKey sessionKey, 
				String filter)
		{
			String activeFilter = "(lifecycle.status=ACTIVE)";
			String providersFilter = null;
			
			if(filter == null)
			{
				providersFilter = activeFilter;
				
			} else
			{
				StringBuilder filterBuilder = 
						new StringBuilder().append("(&");
				if(!filter.startsWith("("))
				{
					filterBuilder.append("(");					
				}
				filterBuilder.append(filter);
				if(!filter.endsWith(")"))
				{
					filterBuilder.append(")");					
				}
				filterBuilder.append(activeFilter);
				filterBuilder.append(")");
				providersFilter = filterBuilder.toString();
			}
			final String fltr = providersFilter;
						
			Set<ServiceProvider> serviceProviders =
			AccessController.<Set<ServiceProvider>>doPrivileged(
			new PrivilegedAction<Set<ServiceProvider>>()
			{
				@Override
				public Set<ServiceProvider> run()
				{									
					Collection<ServiceReference<SensiNactResourceModel>> 
					references = RegistryEndpoint.this.getReferences(sessionKey, 
							fltr);
					
					Iterator<ServiceReference<SensiNactResourceModel>> iterator= 
						references.iterator();
					
					Set<ServiceProvider> providers = new HashSet<ServiceProvider>();
					
					while(iterator.hasNext())
					{					
						ServiceReference<SensiNactResourceModel> ref = iterator.next();						
						SensiNactResourceModel model = SensiNact.this.mediator.getContext(
								).getService(ref);					
						ServiceProvider provider = null;
						try
						{
							provider = (ServiceProvider) model.getRootElement().getProxy(
									sessionKey.getPublicKey());
							
						}catch (ModelElementProxyBuildException e) 
						{
							SensiNact.this.mediator.error(e);
						}
						if(provider != null && provider.isAccessible())
						{
							providers.add(provider);
						}
					}			
					return providers;
				}
			});
			return serviceProviders;
		}

		/**
		 * @param publicKey
		 * @param serviceProviderName
		 * @return
		 */
		private ServiceProvider serviceProvider(SessionKey sessionKey, 
				final String serviceProviderName)
		{
			ServiceProvider provider = null;
			
			Set<ServiceProvider> providers = this.serviceProviders(
			    sessionKey, new StringBuilder().append("(name="
			    		).append(serviceProviderName).append(
			    				")").toString());

			if(providers == null || providers.size()!=1)
			{
				return provider;
			}
			provider = providers.iterator().next();
			return provider;
		}

		
	    /**
	     * @param publicKey
	     * @param serviceProviderName
	     * @param serviceName
	     * @return
	     */
	    private Service service(SessionKey sessionKey,  
	    	String serviceProviderName, String serviceName)
	    {
	    	ServiceProvider serviceProvider = serviceProvider(sessionKey, 
	    			serviceProviderName); 
	    	Service service = null;
	    	if(serviceProvider != null)
		    {
	    		service = serviceProvider.getService(serviceName);
		    }
		    return service;
	    }

	    /**
	     * @param publicKey
	     * @param serviceProviderName
	     * @param serviceName
	     * @param resourceName
	     * @return
	     */
	    private Resource resource(SessionKey sessionKey,  
	    	String serviceProviderName, String serviceName, 
	    	    String resourceName)
	    {
	    	Service service = this.service(sessionKey, serviceProviderName, 
	    			serviceName);
	    	Resource resource = null ;
	    	if(service != null)
	    	{
	    		resource = service.getResource(resourceName);
	    	}
		    return resource;
	    }

		/**
		 * @param publicKey
		 * @param resolveNamespace
		 * @return
		 */
		private JSONObject getLocations(SessionKey sessionKey,  
				boolean resolveNamespace)
		{
			StringBuilder builder = new StringBuilder();
			builder.append('{');
			builder.append("\"type\": \"GET_RESPONSE\"");
			builder.append(",\"statusCode\": 200");
			builder.append(",\"uri\": \"/dev/var\"");
			builder.append(",\"response\":");
			builder.append('{');
			builder.append("\"timestamp\": 0L");
			builder.append(",\"name\": \"No\"");
			builder.append(",\"value\":");
			builder.append('[');

	        String prefix = resolveNamespace?new StringBuilder().append(
	        	SensiNact.this.namespace()).append(":").toString():"";
	        	
        	int index=-1;
        	Collection<ServiceReference<SensiNactResourceModel>> references = 
					RegistryEndpoint.this.getReferences(sessionKey, null);
        	
			Iterator<ServiceReference<SensiNactResourceModel>> iterator = 
					references.iterator();

			AccessTree<? extends AccessNode> tree = sessionKey.getAccessTree();
			AccessMethod.Type get = AccessMethod.Type.valueOf(AccessMethod.GET);
			
			while(iterator.hasNext())
			{
	        	index++;
	        	ServiceReference<SensiNactResourceModel> reference = iterator.next();
	        	String name = (String) reference.getProperty("name");
				Integer level = (Integer) reference.getProperty(name.concat(
						".admin.location.GET"));
				if(level == null)
				{
					level = new Integer(
						AccessLevelOption.OWNER.getAccessLevel().getLevel());
				}
				String uri = UriUtils.getUri(new String[] {name});
				AccessNode node = sessionKey.getAccessTree().getRoot().get(
					uri);
				
				if(node == null)
				{
					node = tree.getRoot();
				}
				if(node.getAccessLevelOption(get
					).getAccessLevel().getLevel() < level.intValue())
				{
					continue;
				}				
	        	String provider = new StringBuilder().append(prefix
	        		).append(uri.substring(1)).toString();	        	
	        	String location =(String) reference.getProperty(
	        			LocationResource.LOCATION);

	        	location = (location==null||location.length()==0)
        		    ?defaultLocation:location;
	        	builder.append(index>0?',':"");
				builder.append('{');
				builder.append("\"provider\":");
				builder.append('"');
				builder.append(provider);
				builder.append('"');
				builder.append(",\"location\":");
				builder.append('"');
				builder.append(location);
				builder.append('"');			
				builder.append('}');
	        }
			builder.append(']');			
			builder.append('}');			
			builder.append('}');
			JSONObject object = new JSONObject(builder.toString());
			return object;
		}
		
		/**
		 * @param publicKey
		 * @param resolveNamespace
		 * @param filter
		 * @return
		 */
		private JSONObject getAll(SessionKey sessionKey, 
			boolean resolveNamespace, String filter)
	    {
			StringBuilder builder = new StringBuilder();
			builder.append('{');
			builder.append("\"providers\":");
			builder.append('[');

	        String prefix = resolveNamespace?new StringBuilder().append(
	        	SensiNact.this.namespace()).append(":").toString():"";
	        	
	        int index = -1;

        	Collection<ServiceReference<SensiNactResourceModel>> references = 
					RegistryEndpoint.this.getReferences(sessionKey, filter);	
			Iterator<ServiceReference<SensiNactResourceModel>> iterator = 
					references.iterator();

			AccessTree<? extends AccessNode> tree = sessionKey.getAccessTree();
			AccessMethod.Type describe = AccessMethod.Type.valueOf(AccessMethod.DESCRIBE);
			
			while(iterator.hasNext())
			{
	        	index++;
	        	ServiceReference<SensiNactResourceModel> reference = iterator.next();
	        	String name = (String) reference.getProperty("name");
				
	        	String provider = new StringBuilder().append(prefix).append(name).toString();
	        	String location =(String)reference.getProperty(LocationResource.LOCATION);	        	
	        	location = (location==null||location.length()==0)?defaultLocation:location;	        	
	            List<String> serviceList = (List<String>)reference.getProperty("services");

				builder.append(index>0?',':"");
				builder.append('{');
				builder.append("\"name\":");
				builder.append('"');
				builder.append(provider);
				builder.append('"');
				builder.append(",\"services\":");
				builder.append('[');

	            int sindex = 0;
	            int slength = serviceList==null?0:serviceList.size();
	            for(;sindex < slength; sindex++)
	            {
	            	String service = serviceList.get(sindex);
	            	String resolvedService = new StringBuilder().append(name).append("."
	            			).append(service).toString();
	            	String serviceUri = UriUtils.getUri(new String[] {name,service});
	            	Integer serviceLevel = (Integer) reference.getProperty(
	            			resolvedService.concat(".DESCRIBE"));
					if(serviceLevel == null)
					{
						serviceLevel = new Integer(
							AccessLevelOption.OWNER.getAccessLevel().getLevel());
					}
					AccessNode node = sessionKey.getAccessTree(
							).getRoot().get(serviceUri);					
					if(node == null)
					{
						node = tree.getRoot();
					}
					if(node.getAccessLevelOption(describe
						).getAccessLevel().getLevel() < serviceLevel.intValue())
					{
						continue;
					}
	            	List<String> resourceList = (List<String>) 
	            		reference.getProperty(service.concat(".resources"));
	            	
					builder.append(sindex>0?',':"");
					builder.append('{');
					builder.append("\"name\":");
					builder.append('"');
					builder.append(service);
					builder.append('"');
					builder.append(",\"location\":");
					builder.append('"');
					builder.append(location);
					builder.append('"');
					builder.append(",\"resources\":");
					builder.append('[');
	            	
	                int rindex = 0;
	                int rlength = resourceList==null?0:resourceList.size();
	                for(;rindex < rlength; rindex++)
	                {
	                	String resource = resourceList.get(rindex);	 
		            	String resolvedResource = new StringBuilder().append(resolvedService
		            			).append(".").append(resource).toString();
		            	String resourceUri = UriUtils.getUri(new String[] {name, service, resource});
		            	Integer resourceLevel = (Integer) reference.getProperty(
		            			resolvedResource.concat(".DESCRIBE"));		            	
						if(resourceLevel == null)
						{
							resourceLevel = new Integer(
								AccessLevelOption.OWNER.getAccessLevel().getLevel());
						}
						node = sessionKey.getAccessTree().getRoot(
								).get(resourceUri);					
						if(node == null)
						{
							node = tree.getRoot();
						}
						if(node.getAccessLevelOption(describe
							).getAccessLevel().getLevel() < resourceLevel.intValue())
						{
							continue;
						}
	                	String type =(String) reference.getProperty(
	                			resolvedResource.concat(".type"));
						builder.append(rindex>0?',':"");
						builder.append('{');
						builder.append("\"name\":");
						builder.append('"');
						builder.append(resource);
						builder.append('"');
						builder.append(",\"type\":");
						builder.append('"');
						builder.append(type);
						builder.append('"');
						builder.append(",\"path\":");
						builder.append('"');
						builder.append(resourceUri);
						builder.append('"');
						builder.append(",\"fromService\":");
						builder.append('"');
						builder.append(service);
						builder.append('"');
						builder.append(",\"fromProvider\":");
						builder.append('"');
						builder.append(provider);
						builder.append('"');						
						builder.append('}');
	                }      
					builder.append(']');
					builder.append('}');      	
	            }
				builder.append(']');
				builder.append('}');
	        }
			builder.append(']');
			builder.append('}');
			JSONObject object = new JSONObject(builder.toString());
			return object;
	    }
		
	    /**
	     * @param publicKey
	     * @param resolveNamespace
	     * @param filter
	     * @return
	     */
	    private JSONObject getProviders(SessionKey sessionKey, 
	    		boolean resolveNamespace, String filter)
	    {
			JSONObject object = new JSONObject();			
			JSONArray jproviders = new JSONArray();
			object.put("providers", jproviders);

	        String prefix = resolveNamespace?new StringBuilder().append(
	        	SensiNact.this.namespace()).append(":").toString():"";

        	Collection<ServiceReference<SensiNactResourceModel>> references = 
					this.getReferences(sessionKey, filter);	
			Iterator<ServiceReference<SensiNactResourceModel>> iterator = 
					references.iterator();
			
			while(iterator.hasNext())
			{
				ServiceReference<SensiNactResourceModel> reference = iterator.next();
	        	String name = (String)reference.getProperty("name");	        	
	        	String provider = new StringBuilder().append(prefix
	        		).append(name).toString();	        	
	            jproviders.put(provider);
	        }
	        object.put("type", "PROVIDERS_LIST");
	    	object.put("uri", UriUtils.PATH_SEPARATOR);
	    	object.put("statusCode", 200);	    	
	        return object;
	    }
	}; 
	
	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
	
	//********************************************************************//
	//						STATIC DECLARATIONS		      				  //
	//********************************************************************//
	
	/**
	 * Retrieves or creates a namespace for the instance of sensiNact whose 
	 * {@link Mediator} is passed as parameter
	 * 
	 * @param mediator the {@link Mediator} allowing to retrieve the 
	 * namespace if it exists
	 * 
	 * @return the namespace for the instance of sensiNact with the 
	 * specified {@link Mediator}
	 * 
	 */
	private static final String namespace(Mediator mediator)
	{
		String prop = (String) mediator.getProperty(Core.NAMESPACE_PROP);
		if(prop == null)
		{
			prop = new StringBuilder().append("sNa").append(Math.round(
				(float)(System.currentTimeMillis()/100000L))+ 
					mediator.hashCode()).toString();
		}
		return prop;
	}
	
	protected static final int LOCAL_ID = 0;

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	final AccessTree<? extends AccessNode> anonymousTree;
	final Sessions sessions;
	
	Mediator mediator;
	private RegistryEndpoint registry;
	
	private volatile int count = LOCAL_ID+1;
	private final String namespace;
	private final String defaultLocation;
	
    /**
     * Constructor
     * 
     * @param mediator the extended {@link Mediator} allowing the 
     * {@link Core} to be instantiated to interact with the OSGi host 
     * environment
     * 
     * @throws SecuredAccessException 
     * @throws BundleException 
     */
    public SensiNact(final Mediator mediator) throws SecuredAccessException, BundleException
    {
    	this.namespace = SensiNact.namespace(mediator);
    	SecuredAccess securedAccess = null;
    	
		ServiceLoader<SecuredAccessFactory> serviceLoader =
			ServiceLoader.load(SecuredAccessFactory.class, 
					mediator.getClassLoader());

		Iterator<SecuredAccessFactory> iterator = serviceLoader.iterator();

		if (iterator.hasNext())
		{
			SecuredAccessFactory factory = iterator.next();
			if (factory != null)
			{
				securedAccess = factory.newInstance(mediator);
			}
		}
		if (securedAccess == null)
		{
			throw new BundleException("A SecuredAccess service was excepted");
		}		
		securedAccess.createAuthorizationService();
		final SecuredAccess sa = securedAccess;
		
		AccessController.doPrivileged(new PrivilegedAction<Void>() 
		{
			@Override
			public Void run() 
			{
				mediator.register(sa, SecuredAccess.class, null);
				return null;
			}
		});
		
        this.defaultLocation = ModelInstance.defaultLocation(mediator);
        this.sessions = new Sessions();

		this.anonymousTree = mediator.callService(
		SecuredAccess.class, new Executable<SecuredAccess, 
		AccessTree<? extends AccessNode>>() 
		{
			@Override
			public AccessTree<? extends AccessNode> execute(
					SecuredAccess securedAccess)
			        throws Exception
			{
				return securedAccess.getUserAccessTree(
						SecuredAccess.ANONYMOUS_PKEY);
			}
		});
        this.mediator = mediator;
        this.registry = new RegistryEndpoint();
    }
    
    private final <R,P> R doPrivilegedService(
    		final Class<P> p,
    		final String f,
    		final Executable<P,R> e)
    {
    	R r= AccessController.<R>doPrivileged(new PrivilegedAction<R>()
		{
			@Override
			public R run()
			{
				return mediator.callService(p, f, e);				
			}
		});
    	return r;
    }

    private final <P> Void doPrivilegedVoidServices(
    		final Class<P> p,
    		final String f,
    		final Executable<P,Void> e)
    {
    	return AccessController.<Void>doPrivileged(
    			new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				mediator.<P>callServices(p, f, e);	
				return null;
			}
		});
    }

    private final AccessTree<?> getAnonymousTree()
    {
    	AccessTree<?> tree = null;
		if(MutableAccessTree.class.isAssignableFrom(
				this.anonymousTree.getClass()))
		{
			tree = ((MutableAccessTree<?>)
				this.anonymousTree).clone();
		} else
		{
			tree = this.anonymousTree;
		}
		return tree;
    }
    
    private final AccessTree<?> getUserAccessTree(final String publicKey)
    {
    	AccessTree<? extends AccessNode> tree = null;
		if(publicKey != null && !publicKey.startsWith(SecuredAccess.ANONYMOUS_PKEY))
		{
			tree =  doPrivilegedService(SecuredAccess.class, null,
			new Executable<SecuredAccess, AccessTree<? extends AccessNode>>() 
			{
				@Override
				public AccessTree<? extends AccessNode> execute(
					SecuredAccess securedAccess) throws Exception
				{
					AccessTree<? extends AccessNode> tree =
						securedAccess.getUserAccessTree(publicKey);					
					return tree;
				}
			});				
		}
		if(tree == null)
		{
			tree = getAnonymousTree();
		}
		return tree;
    }
    
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 * getSession(org.eclipse.sensinact.gateway.core.security.Authentication)
	 */
	@Override
	public Session getSession(final Authentication<?> authentication)
			throws InvalidKeyException, DataStoreException 
	{
		Session session = null;
		if(Credentials.class.isAssignableFrom(authentication.getClass()))
		{
			final UserKey userKey = this.doPrivilegedService(
			AuthenticationService.class, null, 
		    new Executable<AuthenticationService, UserKey>() 
			{
				@Override
				public UserKey execute(AuthenticationService service) 
						throws Exception 
				{
					UserKey key = service.buildKey((Credentials)
							authentication);
					return key;
				}
			});
			String pkey = userKey==null
					?null:userKey.getPublicKey();
			
			session = this.sessions.getSessionFromPublicKey(
					pkey);
			
			if (session == null) 
			{				    
				AccessTree<? extends AccessNode> tree = 
					this.getUserAccessTree(
					pkey);
				
				SessionKey sessionKey = new SessionKey(mediator,
					LOCAL_ID, SensiNact.this.nextToken(), tree);
				
				UserKey ukey = userKey;
				if(ukey == null)
				{
					count++;
					ukey = new UserKey(new StringBuilder().append(
						SecuredAccess.ANONYMOUS_PKEY).append("_"
							).append(count).toString());
				}
				sessionKey.setUserKey(ukey);				
				session = new SensiNactSession(mediator, 
						sessionKey.getToken());						
				sessions.put(sessionKey, session);	
			}
		} else if(AuthenticationToken.class.isAssignableFrom(
				authentication.getClass()))
		{
			session = this.getSession(((AuthenticationToken)authentication
					).getAuthenticationMaterial());
		}
		return session;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#getSession(java.lang.String)
	 */
	@Override
	public Session getSession(final String token)
	{
		Session session = this.sessions.getSessionFromToken(token);
		return session;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#getAnonymousSession()
	 */
	@Override
	public Session getAnonymousSession() 
	{
		AccessTree<?> tree = this.getUserAccessTree(null);
				
		count++;
		String pkey = new StringBuilder().append(
			SecuredAccess.ANONYMOUS_PKEY).append("_"
				).append(count).toString();

		SessionKey sessionKey = new SessionKey(mediator, 
			LOCAL_ID, this.nextToken(), tree);
		
		sessionKey.setUserKey(new UserKey(pkey));
		
		Session session = new SensiNactSession(mediator, 
				sessionKey.getToken());
		
		this.sessions.put(sessionKey,session);
		return session;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 * getApplicationSession(java.lang.String)
	 */
	@Override
	public Session getApplicationSession(final Mediator mediator, 
			final String privateKey)
	{
		SessionKey skey = this.doPrivilegedService(
		SecuredAccess.class, null, new Executable<SecuredAccess, SessionKey>() 
		{
			@Override
			public SessionKey execute(SecuredAccess securedAccess)
			        throws Exception
			{
			    String publicKey = securedAccess.getApplicationPublicKey(
						privateKey);
			    AccessTree<? extends AccessNode> tree = null;
			    
				if(publicKey == null)
				{
					count++;
					publicKey = new StringBuilder().append(
						SecuredAccess.ANONYMOUS_PKEY).append("_"
							).append(count).toString();

					tree = SensiNact.this.getAnonymousTree();
					
				} else
				{
					tree = securedAccess.getApplicationAccessTree(
							publicKey);
				}
				SessionKey sessionKey = new SessionKey(mediator, 
					LOCAL_ID, SensiNact.this.nextToken(), tree);
				
				sessionKey.setUserKey(new UserKey(publicKey));
				return sessionKey;
			}
		});		
		Session session = new SensiNactSession(mediator, 
				skey.getToken());
		
		sessions.put(skey, session);
		return session;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#namespace()
	 */
	@Override
	public String namespace()
	{
		return this.namespace;
	}
    
	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * registerAgent(AbstractSnaAgentCallback,
	 * SnaFilter)
	 */
	@Override
	public String registerAgent(final Mediator mediator, 
			final SnaAgentCallback callback, final SnaFilter filter)
	{			
		final Bundle bundle = mediator.getContext().getBundle();

		final String bundleIdentifier = this.doPrivilegedService(
		BundleValidation.class, null, new Executable<BundleValidation,String>()
	    {
			@Override
			public String execute(BundleValidation bundleValidation)
					throws Exception
			{
				return bundleValidation.check(bundle);
			}
	    });    	
    	final String agentKey = this.doPrivilegedService(
		SecuredAccess.class, null, new Executable<SecuredAccess, String>()
	    {
			@Override
			public String execute(SecuredAccess securedAccess)
					throws Exception
			{
				return securedAccess.getAgentPublicKey(bundleIdentifier);
			}
	    });    	
		final SnaAgentImpl agent = SnaAgentImpl.createAgent(
			mediator, callback, filter, agentKey);
		
		String identifier = new StringBuilder().append(
			"agent_").append(agent.hashCode()).toString();
		
		final Dictionary<String,Object> props = new Hashtable<String,Object>();
		props.put("org.eclipse.sensinact.gateway.agent.id", identifier);
	    props.put("org.eclipse.sensinact.gateway.agent.local", true);
	    
	    AccessController.<Void>doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				agent.start(props);
				return null;
			}
		});
		return identifier;
	}
	
	/**
	 * Unregisters the {@link SnaAgent} whose identifier
	 * is passed as parameter
	 * 
	 * @param identifier
	 * 		the identifier of the {@link SnaAgent} to 
	 * 		register
	 */
	public void unregisterAgent(final String identifier)
	{
		doPrivilegedService(SnaAgent.class,new StringBuilder(
		).append("(&(org.eclipse.sensinact.gateway.agent.id=").append(
		identifier).append(")(org.eclipse.sensinact.gateway.agent.local=true))"
		).toString(), new Executable<SnaAgent,Void>()
		{
			@Override
			public Void execute(SnaAgent agent) throws Exception 
			{
				agent.stop();
				return null;
			}			
		});

		doPrivilegedVoidServices(RemoteCore.class, null, 
		new Executable<RemoteCore,Void>()
		{
			@Override
			public Void execute(RemoteCore remoteCore) throws Exception 
			{
				remoteCore.endpoint().unregisterAgent(identifier);
				return null;
			}			
		});
	} 

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Core#
	 * createRemoteCore(org.eclipse.sensinact.gateway.core.AbstractRemoteEndpoint)
	 */
	@Override
	public void createRemoteCore(final AbstractRemoteEndpoint remoteEndpoint)
	{   
		count++;
    	final RemoteSensiNact remoteCore = new RemoteSensiNact(
    		mediator, remoteEndpoint, new LocalEndpoint(count)
		{
    		private Map<String, Session> remoteSessions = 
    				new HashMap<String, Session>();
    		
			private Session createSession(final String publicKey)
			{
				AccessTree<? extends AccessNode> tree = 
					SensiNact.this.getUserAccessTree(publicKey);
					
				SessionKey sessionKey = new SessionKey(mediator, localID(), 
					SensiNact.this.nextToken(), tree);
				
				sessionKey.setUserKey(new UserKey(publicKey));			
				
				Session session = new SensiNactSession(
					SensiNact.this.mediator, sessionKey.getToken());
				
				SensiNact.this.sessions.put(sessionKey, session, 
					remoteEndpoint);
				
				this.remoteSessions.put(publicKey, session);
				return session;
			}
			
			@Override
			public Session getSession(String publicKey)
			{
				String filteredKey = publicKey;
				if(SecuredAccess.ANONYMOUS_PKEY.equals(publicKey))
				{
					filteredKey = new StringBuilder().append(
					SecuredAccess.ANONYMOUS_PKEY).append("_R").append(
								localID()).toString();
				}
				Session session = this.remoteSessions.get(filteredKey);				
				if(session == null)
				{
					session = createSession(filteredKey);
				}
				return session;
			}
			
			@Override
			void closeSession(String publicKey)
			{
				String filteredKey = publicKey;
				if(SecuredAccess.ANONYMOUS_PKEY.equals(publicKey))
				{
					filteredKey = new StringBuilder().append("remote_").append(
						SecuredAccess.ANONYMOUS_PKEY).append("_").append(
								localID()).toString();
				}
				this.remoteSessions.remove(filteredKey);
			}

			@Override
			void close()
			{
				this.remoteSessions.clear();
			}
		});  
    	
    	remoteCore.endpoint().onConnected(new Executable<String,Void>()
		{
			@Override
			public Void execute(String namespace) throws Exception
			{
		    	remoteCore.open(namespace);
				return null;
			}
		});  
    	
    	remoteCore.endpoint().onDisconnected(new Executable<String,Void>()
		{
			@Override
			public Void execute(String namespace) throws Exception
			{
		    	remoteCore.close();
				return null;
			}
		});  
    	
    	Collection<ServiceReference<SnaAgent>> serviceReferences = null;
		try
		{
			serviceReferences = this.mediator.getContext(
			).getServiceReferences(SnaAgent.class, 
			"(org.eclipse.sensinact.gateway.agent.local=true)");
		}
		catch (InvalidSyntaxException e)
		{
			this.mediator.error(e);
		}
    	if(serviceReferences== null || serviceReferences.isEmpty())
    	{
    		return;
    	}
		Iterator<ServiceReference<SnaAgent>> iterator = 
				serviceReferences.iterator();
		SnaAgent agent = null;
		
		while(iterator.hasNext())
		{
			ServiceReference<SnaAgent> serviceReference = iterator.next();
			if((agent =  this.mediator.getContext().getService(
					serviceReference))!=null)
	    	{
	    		try
	    		{
	    			String identifierProp = (String)
	    				serviceReference.getProperty(
	    				"org.eclipse.sensinact.gateway.agent.id");
	    			
	    			((SnaAgentImpl)agent).registerRemote(
	    				remoteCore, identifierProp);
	    			
	    		}catch(Exception e)
	    		{
	    			continue;
	    			
	    		} finally
	    		{
	    			if(agent != null)
	    			{
	    				agent = null;
	    				this.mediator.getContext().ungetService(
	    						serviceReference);
	    			}
	    		}
	    	}
		}
	}	

	/**
	 * Unregisters the {@link RemoteCore} whose String namespace
	 * is passed as parameter
	 * 
	 * @param namespace the String namespace of the {@link RemoteCore}
	 * to be unregistered
	 */
	protected void unregisterEndpoint(final String namespace)
	{
		if(namespace == null)
		{
			return;
		}		
		this.doPrivilegedService(RemoteCore.class,
		String.format("(namespace=%s)",namespace), 
		new Executable<RemoteCore,Void>()
		{
			@Override
			public Void execute(RemoteCore remoteCore) 
				throws Exception
			{					
				remoteCore.close();
				return null;
			}			
		});
	}

    /**
     * Returns the Set of available {@link ServiceProvider}s compliant 
     * to the LDAP formated filter passed as parameter
     * 
     * @param identifier the String identifier of the {@link Session} 
     * requiring the list of available service providers
     * @param filter the String LDAP formated filter 
     * 
     * @return the Set of available {@link ServiceProvider}s compliant to
     * the specified filter and for the specified {@link Session}
     */
	protected Set<ServiceProvider> serviceProviders(String identifier, String filter)
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		Set<ServiceProvider> set = new HashSet<ServiceProvider>();
		set.addAll(this.registry.serviceProviders(sessionKey,
				filter));
		return set;
	}
		
	/**
     * Returns the {@link ServiceProvider} whose String identifier is 
     * passed as parameter
     * 
     * @param identifier the String identifier of the {@link Session} 
     * requiring the service provider
     * @param serviceProviderId the String identifier of the service provider
     * 
     * @return the {@link ServiceProvider}
     */
	protected ServiceProvider serviceProvider(String identifier, 
			String serviceProviderId)
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return this.registry.serviceProvider(sessionKey,
				serviceProviderId);
	}

	/**
     * Returns the {@link Service} whose String identifier is passed as 
     * parameter, held by the specified service provider
     * 
     * @param identifier the String identifier of the {@link Session} 
     * requiring the service
     * @param serviceProviderId the String identifier of the service provider
     * holding the service
     * @param servideId the String identifier of the service
     * 
     * @return the {@link Service}
     */
	protected Service service(String identifier, String serviceProviderId,
	        String serviceId)
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return this.registry.service(sessionKey,
				serviceProviderId, serviceId);
	}

	/**
     * Returns the {@link Resource} whose String identifier is passed as 
     * parameter, held by the specified service provider and service
     * 
     * @param identifier the String identifier of the {@link Session} 
     * requiring the resource
     * @param serviceProviderId the String identifier of the service provider
     * holding the service providing the resource
     * @param servideId the String identifier of the service providing the 
     * resource
     * @param resourceId the String identifier of the resource
     * 
     * @return the {@link Resource}
     */
	protected Resource resource(String identifier, String serviceProviderId,
	        String serviceId, String resourceId)
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return this.registry.resource(sessionKey,
				serviceProviderId, serviceId, resourceId);
	}
	
	/**
	 * Executes the {@link Executable} passed as parameter which 
	 * expects a {@link RemoteCore} parameter, and returns its JSON 
	 * formated execution result
	 * 
	 * @param serviceProviderId the String identifier of the 
	 * service provider whose prefix allows to identified the
	 * targeted  {@link RemoteCore}
	 * @param executable the {@link Executable} to be executed
	 * 
	 * @return the JSON formated result of the {@link Executable}
	 * execution
	 */
	private JSONObject remoteCoreInvocation(String serviceProviderId,
			Executable<RemoteCore,JSONObject> executable)
	{
		String[] serviceProviderIdElements = serviceProviderId.split(":");
		String domain = serviceProviderIdElements[0];
		JSONObject object = null;
		
		if(serviceProviderIdElements.length==1 || domain.length()==0)
		{
			return object;
		}	
		object = mediator.callService(RemoteCore.class, new StringBuilder(
			).append("(namespace=").append(domain).append(")").toString(), 
				executable);
		return object;
	}
	
	 /**
     * Returns the JSON formated description of the resource whose String
     * identifier is passed as parameter, and held by the service 
     * provider and service whose String identifiers are also passed as 
     * parameter
     * 
     * @param identifier the String identifier of the {@link Session} requiring 
     * the resource description 
     * @param serviceProviderId the String identifier of the 
     * service provider holding the service, providing the resource
     * @param serviceId the String identifier of the service providing
     * the resource
     * @param resourceId the String identifier  of the resource 
     * to return the description of
     * 
     * @return the JSON formated description of the specified resource
     */
	protected JSONObject getResource(String identifier, 
		final String serviceProviderId, final String serviceId, 
		final String resourceId)
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return remoteCoreInvocation(serviceProviderId, 
				new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().getResource(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1),
					serviceId, 
					resourceId);
			}
		});
	}

	/**
     * Returns the JSON formated list of available resources, for the service 
     * and service provider whose String identifiers are passed as parameter
     * 
     * @param identifier the String identifier of the {@link Session} requiring 
     * the description 
     * @param serviceProviderId the String identifier of the 
     * service provider holding the service
     * @param serviceId the String identifier of the service providing 
     * the resources
     * 
     * @return the JSON formated list of available resources for the 
     * specified service and service provider
     */
	protected JSONObject getResources(String identifier, 
			final String serviceProviderId, final String serviceId) 
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return remoteCoreInvocation(serviceProviderId, 
				new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().getResources(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1),
					serviceId);
			}
		});
	}

	/**
     * Returns the JSON formated description of the service whose String
     * identifier is passed as parameter, and held by the specified service 
     * provider
     * 
     * @param identifier the String identifier of the {@link Session} requiring 
     * the service description 
     * @param serviceProviderId the String identifier of the  service provider 
     * holding the service
     * @param serviceId the String identifier of the service to return the 
     * description of
     * 
     * @return the JSON formated description of the specified service
     */
	protected JSONObject getService(String identifier, 
			final String serviceProviderId, final String serviceId) 
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().getService(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1),
					serviceId);
			}
		});
	}

	/**
     * Returns the JSON formated list of available services for the service 
     * provider whose String identifier is passed as parameter
     * 
     * @param identifier the String identifier of the {@link Session} requiring 
     * the list of available services
     * @param serviceProviderId the String identifier of the  service provider 
     * holding the services
     * 
     * @return the JSON formated list of available services for the 
     * specified service provider
     */
	protected JSONObject getServices(String identifier, 
			final String serviceProviderId)
    {
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().getServices(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1));
			}
		});
	}

	/**
     * Returns the JSON formated description of the service provider whose
     * String identifier is passed as parameter
     * 
     * @param identifier the String identifier of the {@link Session} 
     * requiring the service provider description
     * @param serviceProviderId the String identifier of the service provider
     * to return the description of
     * 
     * @return the JSON formated description of the specified service provider
     */
	protected JSONObject getProvider(String identifier,  
			final String serviceProviderId)
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().getProvider(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1));
			}
		});
	}

	/** 
     * Invokes the UNSUBSCRIBE access method on the resource whose String 
     * identifier is passed as parameter, held by the specified service 
     * provider and service
     * 
     * @param identifier the String identifier of the {@link Session} invoking
     * the access method 
     * @param serviceProviderId the String identifier of the 
     * service provider holding the service providing the resource
     * on which applies the access method call
     * @param serviceId the String identifier of the service providing
     * the resource on which applies the access method call
     * @param resourceId the String identifier  of the resource 
     * on which applies the access method call
     * @param subscriptionId the String identifier of the subscription
     * to be deleted
     * 
     * @return the JSON formated response of the UNSUBSCRIBE access method 
     * invocation
     */
	protected JSONObject unsubscribe(String identifier, 
		final String serviceProviderId, final String serviceId, 
		final String resourceId, final String subscriptionId) 
	{
		final SessionKey sessionKey = sessions.get(
			new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN,identifier));
		
		return remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().unsubscribe(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1),
					serviceId, 
					resourceId,
					subscriptionId);
			}
		});
	}

	 /** 
     * Invokes the SUBSCRIBE access method on the resource whose String 
     * identifier is passed as parameter, held by the specified service 
     * provider and service
     * 
     * @param identifier the String identifier of the {@link Session} invoking
     * the access method 
     * @param serviceProviderId the String identifier of the 
     * service provider holding the service providing the resource
     * on which applies the access method call
     * @param serviceId the String identifier of the service providing
     * the resource on which applies the access method call
     * @param resourceId the String identifier  of the resource 
     * on which applies the access method call
     * @param recipient the {@link Recipient} to which the update events
     * generated by the subscription will be transmitted
     * @param conditions the JSON formated set of constraints applying
     * on the subscription to be created
     * 
     * @return the JSON formated response of the SUBSCRIBE access method 
     * invocation
     */
	protected JSONObject subscribe(String identifier, 
		final String serviceProviderId, final String serviceId,
		    final String resourceId, final Recipient recipient, 
			    final JSONArray conditions) 
	{
		final SessionKey sessionKey = sessions.get(new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().subscribe(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1),
					serviceId, 
					resourceId,
					recipient,
					conditions);
			}
		});
	}

	 /** 
     * Invokes the ACT access method on the resource whose String identifier
     * is passed as parameter, held by the specified service provider and 
     * service
     * 
     * @param identifier the String identifier of the {@link Session} invoking
     * the access method 
     * @param serviceProviderId the String identifier of the 
     * service provider holding the service providing the resource
     * on which applies the access method call
     * @param serviceId the String identifier of the service providing
     * the resource on which applies the access method call
     * @param resourceId the String identifier  of the resource 
     * on which applies the access method call
     * @param parameters the Objects array parameterizing the 
     * call 
     * 
     * @return the JSON formated response of the ACT access method 
     * invocation
     */
	protected JSONObject act(String identifier, 
		final String serviceProviderId, final String serviceId, 
		    final String resourceId, final Object[] parameters) 
	{
		final SessionKey sessionKey =  sessions.get(new KeyExtractor<KeyExtractorType>(
		KeyExtractorType.TOKEN,identifier));
		
		return remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().act(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1),
					serviceId, 
					resourceId,
					parameters);
			}
		});
	}

	/** 
     * Invokes the SET access method on the resource whose String identifier
     * is passed as parameter, held by the specified service provider and 
     * service
     * 
     * @param identifier the String identifier of the {@link Session} invoking
     * the access method 
     * @param serviceProviderId the String identifier of the 
     * service provider holding the service providing the resource
     * on which applies the access method call
     * @param serviceId the String identifier of the service providing
     * the resource on which applies the access method call
     * @param resourceId the String identifier  of the resource 
     * on which applies the access method call
     * @param attributeId the String identifier of the resource's attribute 
     * targeted by the access method call 
     * @param parameter the value object to be set
     * 
     * @return the JSON formated response of the SET access method 
     * invocation
     */
	protected JSONObject set(String identifier,  
		final String serviceProviderId, final String serviceId, 
		    final String resourceId,  final String attributeId, 
		        final Object parameter)
	{
		final SessionKey sessionKey =  sessions.get(
			new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN,identifier));
		
		return remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().set(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1),
					serviceId, 
					resourceId,
					attributeId,
					parameter);
			}
		});
	}

	/**
     * Invokes the GET access method on the resource whose String identifier
     * is passed as parameter, held by the specified service provider and 
     * service
     *  
     * @param identifier the String identifier of the {@link Session} invoking
     * the access method 
     * @param serviceProviderId the String identifier of the 
     * service provider holding the service providing the resource
     * on which applies the access method call
     * @param serviceId the String identifier of the service providing
     * the resource on which applies the access method call
     * @param resourceId the String identifier  of the resource 
     * on which applies the access method call
     * @param attributeId the String identifier of the resource's attribute 
     * targeted by the access method call 
     * 
     * @return the JSON formated response of the GET access method invocation
     */
	protected JSONObject get(String identifier, 
		final String serviceProviderId, final String serviceId, 
		    final String resourceId, final String attributeId)
	{
		final SessionKey sessionKey = sessions.get(
			new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN,identifier));
		
		return remoteCoreInvocation(serviceProviderId, new Executable<RemoteCore,JSONObject>()
		{
			@Override
			public JSONObject execute(RemoteCore connector)
					throws Exception 
			{
				if(connector == null)
				{
					return null;
				}
				return connector.endpoint().get(
					sessionKey.getPublicKey(),
					serviceProviderId.substring(serviceProviderId.indexOf(':')+1),
					serviceId, 
					resourceId,
					attributeId);
			}
		});
	}

	/**
  	 * Returns the JSON formated list of locations of all registered resource model 
  	 * instances, accessible by the {@link Session} whose String identifier is passed 
  	 * as parameter
  	 * 
  	 * @param identifier the String identifier of the {@link Session} for which to 
  	 * retrieve the list of accessible resource model instances
  	 * 
  	 * @return the JSON formated list of the location of the resource model 
  	 * instances for the specified {@link Session}.
  	 */
	protected JSONObject getLocations(String identifier) 
	{		
		final SessionKey sessionKey =  sessions.get(
			new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN, identifier));
		
		JSONObject object = this.registry.getLocations(sessionKey,
				sessionKey.localID()!=0);
		
		if(object == null)
		{
			object=new JSONObject();
		    object.put("type","GET_RESPONSE");
		    object.put("statusCode",AccessMethodResponse.SUCCESS_CODE);
		    object.put("uri","/dev/var");
		        			
			JSONArray jproviders = new JSONArray();
			
			JSONObject responseContent= new JSONObject();
			responseContent.put("timestamp",0L);
		    responseContent.put("name","No");
		    responseContent.put("value",jproviders);
		    responseContent.put("type","GET_RESPONSE");			
		    object.put("response", responseContent);
		}
		//propagate only if local instance call
		if(sessionKey.localID() == 0)
		{
			final JSONArray array = object.optJSONObject("response"
				).optJSONArray("value");
		
			mediator.callServices(RemoteCore.class, 
					new Executable<RemoteCore,Void>()
			{
				@Override
				public Void execute(RemoteCore core) throws Exception
				{
					JSONObject o = core.endpoint().getLocations(
							sessionKey.getPublicKey());
					if(!JSONObject.NULL.equals(o))
					{
						JSONArray a = o.optJSONArray("providers");
						int index = 0;
						int length = a==null?0:a.length();
						for(;index < length; index++)
						{
							array.put(a.get(index));
						}
					}
					return null;
				}	
			});
		}
		return object;
	}
	
	/**
     * Returns the JSON formated list of available service providers for
     * the {@link Session} whose String  identifier is passed as parameter
     * 
     * @param identifier the String  identifier of the {@link Session} 
     * requiring the list of available service providers
     * 
     * @return the JSON formated list of available service providers
     */
	protected JSONObject getProviders(String identifier) 
	{		
		final SessionKey sessionKey =  sessions.get(
			new KeyExtractor<KeyExtractorType>(
			KeyExtractorType.TOKEN, identifier));
		
		JSONObject object = this.registry.getProviders(sessionKey,
				sessionKey.localID()!=0, null);
		
		if(object == null)
		{
	        object = new JSONObject();
	    	object.put("type", "PROVIDERS_LIST");
	    	object.put("uri", UriUtils.PATH_SEPARATOR);
	    	object.put("statusCode", 200);
	    	object.put("providers", new JSONArray());
		}
		JSONArray jsonArray = object.optJSONArray("providers");
		if(jsonArray == null)
		{
			jsonArray = new JSONArray();
			object.put("providers", jsonArray);
		}
		//propagate only if local instance call
		if(sessionKey.localID() == 0)
		{
			final JSONArray array = jsonArray;
			mediator.callServices(RemoteCore.class, new Executable<RemoteCore,Void>()
			{
				@Override
				public Void execute(RemoteCore core) throws Exception
				{
					JSONObject o = core.endpoint().getLocations(
							sessionKey.getPublicKey());
					if(!JSONObject.NULL.equals(o))
					{
						JSONArray a = o.optJSONArray("providers");
						int index = 0;
						int length = a==null?0:a.length();
						for(;index < length; index++)
						{
							array.put(a.get(index));
						}
					}
					return null;
				}	
			});
		}
		return object;
	}

	/**
  	 * Returns the JSON formated list of all registered resource 
  	 * model instances, accessible by the {@link Session} whose 
  	 * String identifier is passed as parameter and compliant to  
  	 * the specified String LDAP formated filter.
  	 * 
  	 * @param identifier the String identifier of the {@link Session} for 
  	 * which to retrieve the list of accessible resource model instances
  	 * @param filter the String LDAP formated filter 
  	 * 
  	 * @return the JSON formated list of the resource model instances for 
  	 * the specified {@link Session} and compliant to the specified filter.
  	 */
	protected JSONObject getAll(String identifier, String filter)
	{
		final SessionKey sessionKey =  sessions.get(
				new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN, identifier));
		
		JSONObject object = this.registry.getAll(sessionKey,
				sessionKey.localID()!=0,filter);
		
		if(object == null)
		{
	        object = new JSONObject();
	    	object.put("providers", new JSONArray());
		}
		JSONArray array = object.optJSONArray("providers");
		if(array == null)
		{
			array = new JSONArray();
			object.put("providers", array);
		}		
		//propagate only if local instance call
		if(sessionKey.localID() == 0)
		{
			Collection<ServiceReference<RemoteCore>> references = null;
			try 
			{
				references = mediator.getContext(
						).getServiceReferences(RemoteCore.class,null);
				
			} catch (InvalidSyntaxException e) 
			{
				mediator.error(e.getMessage(), e);
			}
			if(references != null)
			{
				Iterator<ServiceReference<RemoteCore>> iterator = 
						references.iterator();
				
				while(iterator.hasNext())
				{
					RemoteCore connector = null;
					ServiceReference<RemoteCore> ref = iterator.next();
					if(ref == null ||
						(connector=mediator.getContext().getService(ref))==null)
					{
						continue;
					}
					JSONObject o = connector.endpoint().getAll(
							sessionKey.getPublicKey(), filter);
					mediator.getContext().ungetService(ref);
					if(o == null)
					{
						continue;
					}
					JSONArray a = o.optJSONArray("providers");
					int index = 0;
					int length = a==null?0:a.length();
					for(;index < length; index++)
					{
						array.put(a.get(index));
					}
				}
			}
		}
		return object;
	}

    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.core.Core#close()
     */
    public void close()
    {
        mediator.debug("closing sensiNact core");
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				SensiNact.this.mediator.callServices(SensiNactResourceModel.class, 
					new Executable<SensiNactResourceModel, Void>()
					{
						@Override
						public Void execute(SensiNactResourceModel instance)
						        throws Exception
						{
							instance.unregister();
							return null;
						}
					});
				SensiNact.this.mediator.callServices(RemoteCore.class, 
					new Executable<RemoteCore, Void>()
					{
						@Override
						public Void execute(RemoteCore instance)
						        throws Exception
						{
							instance.endpoint().disconnect();
							return null;
						}
					});
				SensiNact.this.mediator.callServices(SnaAgent.class, 
					new Executable<SnaAgent, Void>()
					{
						@Override
						public Void execute(SnaAgent instance)
						        throws Exception
						{
							instance.stop();
							return null;
						}
					});
				return null;
			}
		});    	
    }

	/**
	 */
	String nextToken() 
	{	
		boolean exists = false;
		String token = null;
		do
		{
			try
			{
				token = CryptoUtils.createToken();
			}
			catch (InvalidKeyException e)
			{
				token = Long.toHexString(
						System.currentTimeMillis());
			}
			synchronized(this.sessions)
			{	
				exists = this.sessions.get(
					new Sessions.KeyExtractor<Sessions.KeyExtractorType>(
						Sessions.KeyExtractorType.TOKEN, token))!=null;
			}
		} while(exists);		
		return token;		
	}
	
}
