/**
 * #%L
 * sensiNact IoT Gateway - Core
 * %%
 * Copyright (C) 2015 CEA
 * %%
 * sensiNact - 2015
 * 
 * CEA - Commissariat a l'energie atomique et aux energies alternatives
 * 17 rue des Martyrs
 * 38054 Grenoble
 * France
 * 
 * Copyright(c) CEA
 * All Rights Reserved
 * #L%
 */
package org.eclipse.sensinact.gateway.core;

import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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
		 * Creates and registers an {@link SnaAgent} attached to this Session
		 * and that will be unregistered 
		 * 
		 * @param callback the {@link SnaAgentCallback} of the {@link SnaAgent}
		 * to be created
		 * @param filter the {@link SnaFilter} of the {@link SnaAgent}
		 * to be created
		 */
		public void registerSessionAgent(final SnaAgentCallback callback, 
				final SnaFilter filter)
		{			
			AccessController.<String>doPrivileged(new PrivilegedAction<String>()
			{
				@Override
				public String run()
				{
					SessionKey key = SensiNact.this.sessions.get(getId());
					if(key == null || key.getPublicKey()==null)
					{
						return null;
					}
					final String publicKey = key.getPublicKey();
					final int localId = key.localID();
					
					final SnaAgentImpl agent = SnaAgentImpl.createAgent(
						mediator, callback, filter, publicKey);
					
					final String identifier = new StringBuilder().append(
						"agent_").append(agent.hashCode()).toString();
					
					Dictionary<String,Object> props = new Hashtable<String,Object>();
					props.put("org.eclipse.sensinact.gateway.agent.id",identifier);
				    props.put("org.eclipse.sensinact.gateway.agent.local",(localId==0));
				    	
					agent.start(props);
					
					SensiNact.this.mediator.callService(RemoteCore.class,
							new Executable<RemoteCore,Void>()
				    {
						@Override
						public Void execute(RemoteCore remoteCore) 
								throws Exception
						{
							if(remoteCore.localID()!=localId)
							{
								remoteCore.endpoint().registerAgent(
									identifier, filter, publicKey);
							}
							return null;
						}
				    });
					return identifier;
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
	        }else
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
		        final String serviceId, final String resourceId, final String subscriptionId)
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
		 * @see org.eclipse.sensinact.gateway.core.Endpoint#jsonResources(java.lang.String, java.lang.String)
		 */
		@Override
		public JSONObject getResources(
				final String serviceProviderId, 
				final String serviceId)
		{     		 
			Service service = this.service(serviceProviderId, serviceId);
			if(service == null)
			{
    			 return AccessController.doPrivileged(new PrivilegedAction<JSONObject>()
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

	final Sessions sessions;	
	private final Session localAnonymousSession;
	
	Mediator mediator;
	private RegistryEndpoint registry;
	
	private int count = LOCAL_ID+1;
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
    public SensiNact(Mediator mediator) 
    		throws SecuredAccessException, BundleException
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
		mediator.register(securedAccess, SecuredAccess.class, null);
        
        this.defaultLocation = ModelInstance.defaultLocation(mediator);
        this.sessions = new Sessions();

		final AccessTree<? extends AccessNode> tree = mediator.callService(
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
		
		SessionKey localAnonymousSessionKey = new SessionKey(
			LOCAL_ID, SecuredAccess.ANONYMOUS_PKEY, tree);
		localAnonymousSessionKey.setUserKey(new UserKey(
				SecuredAccess.ANONYMOUS_PKEY));		
		this.localAnonymousSession = new SensiNactSession(mediator, 
				localAnonymousSessionKey.getToken());		
		this.sessions.put(localAnonymousSessionKey, 
				localAnonymousSession);
		
        this.mediator = mediator;
        this.registry = new RegistryEndpoint();
    }


	/**
	 * 
	 * @param authentication
	 * 
	 * @return
	 * 
	 * @throws InvalidKeyException
	 * @throws DataStoreException
	 */
	@Override
	public Session getSession(final Authentication<?> authentication)
			throws InvalidKeyException, DataStoreException 
	{
		Session session = null;
		if(Credentials.class.isAssignableFrom(authentication.getClass()))
		{
			final UserKey userKey = AccessController.<UserKey>doPrivileged(
			new PrivilegedAction<UserKey>()
			{
				@Override
				public UserKey run() 
				{
					return mediator.callService(AuthenticationService.class,			
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
				}
			});
			if(userKey == null)
			{
				session = getAnonymousSession();
				
			} else
			{
				session = this.sessions.getSessionFromPublicKey(
					userKey.getPublicKey());
			}
			if (session == null) 
			{				    
				AccessTree<? extends AccessNode> tree = mediator.callService(
				SecuredAccess.class, new Executable<SecuredAccess, 
				AccessTree<? extends AccessNode>>() 
				{
					@Override
					public AccessTree<? extends AccessNode> execute(
						SecuredAccess securedAccess) throws Exception
					{
						AccessTree<? extends AccessNode> tree =
							securedAccess.getUserAccessTree(
								userKey.getPublicKey());
						if(tree == null)
						{
							tree = securedAccess.getUserAccessTree(
									SecuredAccess.ANONYMOUS_PKEY);
						}
						return tree;
					}
				});
				SessionKey sessionKey = new SessionKey(LOCAL_ID,
						SensiNact.this.nextToken(), tree);						
				sessionKey.setUserKey(userKey);						
				session = new SensiNactSession(mediator, sessionKey.getToken());						
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
	 * @param login
	 * @param password
	 * @return
	 * @throws InvalidKeyException
	 * @throws DataStoreException
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
	 * @see SecuredAccess#getAnonymousSession()
	 */
	@Override
	public Session getAnonymousSession() 
	{
		return localAnonymousSession;
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
		return AccessController.<Session>doPrivileged(new PrivilegedAction<Session>()
		{
			@Override
			public Session run() 
			{
				return  mediator.callService(
				SecuredAccess.class, new Executable<SecuredAccess, Session>() 
				{
					@Override
					public Session execute(SecuredAccess securedAccess)
					        throws Exception
					{
						String publicKey = securedAccess.getApplicationPublicKey(privateKey);
						if(publicKey == null 
							|| SecuredAccess.ANONYMOUS_PKEY.equals(publicKey))
						{
							return SensiNact.this.getAnonymousSession();
						}
						AccessTree<? extends AccessNode> tree = 
								securedAccess.getApplicationAccessTree(publicKey);
						SessionKey sessionKey = new SessionKey(LOCAL_ID, 
								SensiNact.this.nextToken(), tree);
						sessionKey.setUserKey(new UserKey(publicKey));	
						Session session = new SensiNactSession(mediator, 
								sessionKey.getToken());							
						sessions.put(sessionKey, session);
						return session;
					}
				});		
			}
		});
	}
	/**
	 * @return
	 * @throws InvalidKeyException
	 */
	String nextToken() throws InvalidKeyException
	{	
		boolean exists = false;
		String token = null;
		do
		{
			token = CryptoUtils.createToken();

			synchronized(this.sessions)
			{	
				exists = this.sessions.get(
					new Sessions.KeyExtractor<Sessions.KeyExtractorType>(
						Sessions.KeyExtractorType.TOKEN, token))!=null;
			}
		} while(exists);		
		return token;		
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
		return AccessController.<String>doPrivileged(new PrivilegedAction<String>()
		{
			@Override
			public String run()
			{
				final Bundle bundle = mediator.getContext().getBundle();
		    	final String agentKey = mediator.callService(
		    		SecuredAccess.class, new Executable<SecuredAccess, 
		    		String>()
				    {
						@Override
						public String execute(SecuredAccess securedAccess)
								throws Exception
						{
							String bundleIdentifier = securedAccess.validate(
									bundle);
							try
							{
								return securedAccess.getAgentPublicKey(
										bundleIdentifier);
							}
							catch (Exception e)
							{
								SensiNact.this.mediator.error(
									"Unable to retrieve the agent key",e);
							}   
							return null;
						}
				    }
		    	);    	
				final SnaAgentImpl agent = SnaAgentImpl.createAgent(
					mediator, callback, filter, agentKey);
				
				final String identifier = new StringBuilder().append(
					"agent_").append(agent.hashCode()).toString();
				
				Dictionary<String,Object> props = new Hashtable<String,Object>();
				props.put("org.eclipse.sensinact.gateway.agent.id",identifier);
			    props.put("org.eclipse.sensinact.gateway.agent.local",true);
				agent.start(props);
				
				SensiNact.this.mediator.callService(RemoteCore.class,
						new Executable<RemoteCore,Void>()
			    {
					@Override
					public Void execute(RemoteCore remoteCore) 
							throws Exception
					{
						try
						{
							remoteCore.endpoint().registerAgent(
								identifier, filter, agentKey);
						}
						catch (Exception e)
						{
							SensiNact.this.mediator.error(e);
						}   
						return null;
					}
			    });
				return identifier;
			}
		});
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
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				Collection<ServiceReference<SnaAgent>> references = null;
				try
				{
					references = SensiNact.this.mediator.getContext(
					).getServiceReferences(SnaAgent.class, new StringBuilder(
					).append("(&(org.eclipse.sensinact.gateway.agent.id=").append(
					identifier).append(")(org.eclipse.sensinact.gateway.agent.local=true))"
							).toString());
				}
				catch (InvalidSyntaxException e)
				{
					mediator.error(e);
				}
				if(references == null || references.size() != 1)
				{
					return null;
				}
				SensiNact.this.mediator.getContext().getService(
						references.iterator().next()).stop();
				
				SensiNact.this.mediator.callService(RemoteCore.class,
						new Executable<RemoteCore,Void>()
			    {
					@Override
					public Void execute(RemoteCore remoteCore) 
							throws Exception
					{
						try
						{
							remoteCore.endpoint().unregisterAgent(
									identifier);
						}
						catch (Exception e)
						{
							SensiNact.this.mediator.error(e);
						}   
						return null;
					}
			    });
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
	public void createRemoteCore(AbstractRemoteEndpoint remoteEndpoint)
	{   
    	final RemoteSensiNact remoteCore = new RemoteSensiNact(
    		mediator, remoteEndpoint, new LocalEndpoint(count)
		{
			private Session createSession(final String publicKey)
			{
				return AccessController.<Session>doPrivileged(
						new PrivilegedAction<Session>()
				{
					@Override
					public Session run()
					{
		    			return SensiNact.this.mediator.callService(SecuredAccess.class, 
		    				new Executable<SecuredAccess, Session>() 
		    			{
		    				@Override
		    				public Session execute(SecuredAccess securedAccess) 
		    						throws Exception
		    				{ 
		    					AccessTree<? extends AccessNode> tree = 
		    						securedAccess.getUserAccessTree(publicKey);
		    					
		    					if(tree == null)
		    					{
		    						tree = securedAccess.getUserAccessTree(
		    								SecuredAccess.ANONYMOUS_PKEY);
		    					}
		    					SessionKey sessionKey = new SessionKey(localID(), 
		    						SensiNact.this.nextToken(), tree);
		    					sessionKey.setUserKey(new UserKey(publicKey));			
		    					Session session = new SensiNactSession(
		    						SensiNact.this.mediator, sessionKey.getToken());
		    					SensiNact.this.sessions.put(sessionKey, session);
		    					return session;
		    				}
		    			});	
					}
				});
			}
			
			public Session getSession(String publicKey)
			{
				String filteredKey = publicKey;
				if(SecuredAccess.ANONYMOUS_PKEY.equals(publicKey))
				{
					filteredKey = new StringBuilder().append("remote_").append(
						SecuredAccess.ANONYMOUS_PKEY).append("_").append(
								localID()).toString();
				}
				Session session = SensiNact.this.sessions.getSessionFromPublicKey(
						publicKey);
				
				if(session == null)
				{
					session = createSession(filteredKey);
				}
				return session;
			}
		});
    	
		count++;
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
    	
	}	

	/**
	 * @param namespace
	 */
	public void unregisterEndpoint(final String namespace)
	{
		if(namespace == null)
		{
			return;
		}		
		AccessController.<Void>doPrivileged(
				new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				SensiNact.this.mediator.callService(RemoteCore.class, 
					new StringBuilder().append("(namespace=").append(namespace
						).append(")").toString(), new Executable<RemoteCore,Void>()
					{
						@Override
						public Void execute(RemoteCore remoteCore)
						        throws Exception
						{
							remoteCore.close();
							return null;
						}
					});
				return null;
			}
		});
	}

	/**
	 * @param localID
	 * @param serviceProviderId
	 * @param executable
	 * @return
	 */
	private JSONObject connector(String serviceProviderId,
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
	 * @param publicKey
	 * @param filter
	 * @return
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
	 * @param publicKey
	 * @param serviceProviderId
	 * @return
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
	 * @param publicKey
	 * @param serviceProviderId
	 * @param serviceId
	 * @return
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
	 * @param publicKey
	 * @param serviceProviderId
	 * @param serviceId
	 * @param resourceId
	 * @return
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @param serviceId
	 * @param resourceId
	 * @return
	 */
	protected JSONObject getResource(String identifier, 
		final String serviceProviderId, final String serviceId, 
		final String resourceId)
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return connector(serviceProviderId, 
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @param serviceId
	 * @return
	 */
	protected JSONObject getResources(String identifier, 
			final String serviceProviderId, final String serviceId) 
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @param serviceId
	 * @return
	 */
	protected JSONObject getService(String identifier, 
			final String serviceProviderId, final String serviceId) 
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @return
	 */
	protected JSONObject getServices(String identifier, 
			final String serviceProviderId)
    {
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @return
	 */
	protected JSONObject getProvider(String identifier,  
			final String serviceProviderId)
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @param serviceId
	 * @param resourceId
	 * @param subscriptionId
	 * @return
	 */
	protected JSONObject unsubscribe(String identifier, 
		final String serviceProviderId, final String serviceId, 
		final String resourceId, final String subscriptionId) 
	{
		final SessionKey sessionKey = sessions.get(
			new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN,identifier));
		
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @param serviceId
	 * @param resourceId
	 * @param recipient
	 * @param conditions
	 * @return
	 */
	protected JSONObject subscribe(String identifier, 
		final String serviceProviderId, final String serviceId,
		    final String resourceId, final Recipient recipient, 
			    final JSONArray conditions) 
	{
		final SessionKey sessionKey = sessions.get(
				new KeyExtractor<KeyExtractorType>(
					KeyExtractorType.TOKEN,identifier));
			
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @param serviceId
	 * @param resourceId
	 * @param parameters
	 * @return
	 */
	protected JSONObject act(String identifier, 
		final String serviceProviderId, final String serviceId, 
		    final String resourceId, final Object[] parameters) 
	{
		final SessionKey sessionKey =  sessions.get(new KeyExtractor<KeyExtractorType>(KeyExtractorType.TOKEN,identifier));
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @param serviceId
	 * @param resourceId
	 * @param attributeId
	 * @param parameter
	 * @return
	 */
	protected JSONObject set(String identifier,  
		final String serviceProviderId, final String serviceId, 
		    final String resourceId,  final String attributeId, 
		        final Object parameter)
	{
		final SessionKey sessionKey =  sessions.get(
			new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN,identifier));
		
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param localID
	 * @param serviceProviderId
	 * @param serviceId
	 * @param resourceId
	 * @param attributeId
	 * @return
	 */
	protected JSONObject get(String identifier, 
		final String serviceProviderId, final String serviceId, 
		    final String resourceId, final String attributeId)
	{
		final SessionKey sessionKey = sessions.get(
			new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN,identifier));
		
		return connector(serviceProviderId, new Executable<RemoteCore,JSONObject>()
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
	 * @param publicKey
	 * @param resolveNamespace
	 * @param localID
	 * @return
	 */
	protected JSONObject getLocations(String identifier) 
	{		
		final SessionKey sessionKey =  sessions.get(
			new KeyExtractor<KeyExtractorType>(
				KeyExtractorType.TOKEN,identifier));
		
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
	 * @param publicKey
	 * @param resolveNamespace
	 * @param localID
	 * @return
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
	 * @param publicKey
	 * @param resolveNamespace
	 * @param localID
	 * @param filter
	 * @return
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
	 * 
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
}
