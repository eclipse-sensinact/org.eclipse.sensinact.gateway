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
package org.eclipse.sensinact.gateway.core.security.impl;

import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.ModelAlreadyRegisteredException;
import org.eclipse.sensinact.gateway.core.ModelElementProxyBuildException;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.message.SnaAgentImpl;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.core.security.Sessions;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.UriUtils;

class SecuredAccessImpl implements SecuredAccess
{
	private Session initialValue()
	{	
		return new Session()
		{
			/**
			 * @inheritDoc
			 *
			 * @see Session#getServiceProviders()
			 */
			@Override
			public Set<ServiceProvider> getServiceProviders()
			{
				return SecuredAccessImpl.this.getServiceProviders(
						this.getSessionKey());
			}

			/**
			 * @inheritDoc
			 *
			 * @see Session#getServiceProvider(String)
			 */
			@Override
			public ServiceProvider getServiceProvider(String serviceProviderName)
			{
				return SecuredAccessImpl.this.getServiceProvider(
						this.getSessionKey(), serviceProviderName);
			}

			/**
			 * @inheritDoc
			 *
			 * @see Session#getService(String, String)
			 */
			@Override
			public Service getService(String serviceProviderName,
									  String serviceName)
			{
				return getFromUri(UriUtils.getUri(new String[]{
						serviceProviderName, serviceName}));
			}

			/**
			 * @inheritDoc
			 *
			 * @see Session#getResource(String, String, String)
			 */
			@Override
			public Resource getResource(String serviceProviderName,
										String serviceName, String resourceName)
			{
				return getFromUri(UriUtils.getUri(new String[]{
						serviceProviderName, serviceName, resourceName}));
			}

			/**
			 * @throws SecuredAccessException 
			 * @throws ModelAlreadyRegisteredException 
			 * @inheritDoc
			 *
			 * @see Session#register(SensiNactResourceModel)
			 */
			@Override
			public ServiceRegistration<SensiNactResourceModel> 
			register(SensiNactResourceModel modelInstance) 
					throws SecuredAccessException 
			{
				return SecuredAccessImpl.this.register(modelInstance);
			}

			/**
			 * @throws SecuredAccessException 
			 * @inheritDoc
			 *
			 * @see Session#unregister(ServiceRegistration)
			 */
			@Override
			public void unregister(
					ServiceRegistration<SensiNactResourceModel> registration) 
					throws SecuredAccessException
			{		
				SecuredAccessImpl.this.unregister(registration);
				
			}

			/**
			 * @inheritDoc
			 *
			 * @see Session#getFromUri(String)
			 */
			@Override
			public <S extends ElementsProxy<?>> S getFromUri(String uri)
			{
				String[] uriElements = UriUtils.getUriElements(uri);					
				ServiceProvider provider;
				Service service;
				Resource resource;
				if(uriElements.length > 0)
				{
					provider = this.getServiceProvider(uriElements[0]);
					if(uriElements.length > 1)
					{
						service = provider.getService(uriElements[1]);
						if(uriElements.length >2)
						{
							resource = service.getResource(uriElements[2]);
							return (S) resource;
						}
						return (S) service;
					}
					return (S) provider;
				}
				return null;
			}

			/**
			 * @inheritDoc
			 *
			 * @see Session#getSessionKey()
			 */
			@Override
			public Session.Key getSessionKey()
			{
				Session.Key key =  new Session.Key();
				key.setToken("FAKEKEY");
				key.setUid(0);
				return key;
			}
		};
	}
	
	private Mediator mediator;
	private Map<String, ServiceRegistration> agents;

	private ServiceRegistration<AuthorizationService> authorizationRegistration;
	

	/**
	 * @param mediator
	 */
	public SecuredAccessImpl(Mediator mediator)
	{
		this.mediator = mediator;
		this.agents = new ConcurrentHashMap<String,ServiceRegistration>();
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#getSession(Authentication)
	 */
	@Override
	public Session getSession(Authentication<?> authentication)
	        throws InvalidKeyException, DataStoreException
	{
		return getAnonymousSession();
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#getAnonymousSession()
	 */
	@Override
	public Session getAnonymousSession()
	{
		Session session = Sessions.SESSIONS.get();
		if(session == null)
		{
			session  = this.initialValue();
			Sessions.SESSIONS.set(session);
		}
		return session;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * getAccessNode(java.lang.String, java.lang.String)
	 */
	@Override
	public AccessTree getAccessTree(String identifier) throws SecuredAccessException
	{
		AccessTree tree = new AccessTree(mediator
			).withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
		return tree;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * buildAccessNodesHierarchy(java.lang.String, java.lang.String, 
	 * org.eclipse.sensinact.gateway.core.security.RootNode)
	 */
	@Override
	public void buildAccessNodesHierarchy(String identifier, String name,
			AccessTree tree) throws SecuredAccessException
	{
		//do nothing
	}
	
	/**
	 * 
	 */
	private Set<ServiceProvider> getServiceProviders(final Session.Key key)
	{
		final String filter = "(lifecycle.status=ACTIVE)";
		
		Set<ServiceProvider> serviceProviders =
		AccessController.<Set<ServiceProvider>>doPrivileged(
		new PrivilegedAction<Set<ServiceProvider>>()
		{
			@Override
			public Set<ServiceProvider> run()
			{
				Collection<ServiceReference<SensiNactResourceModel>>
				references = null;
				try
				{
					references = SecuredAccessImpl.this.mediator.getContext(
						).getServiceReferences(SensiNactResourceModel.class,
								filter);

				}catch(InvalidSyntaxException e)
				{
					SecuredAccessImpl.this.mediator.error(e);					
				}
				if(references == null || references.isEmpty())
				{
					return Collections.<ServiceProvider>emptySet();
				}
				Set<ServiceProvider> providers = new HashSet<ServiceProvider>();
					
				Iterator<ServiceReference<SensiNactResourceModel>> 
					iterator = references.iterator();
				
				while(iterator.hasNext())
				{
					ServiceReference<SensiNactResourceModel> reference = 
							iterator.next();
					
					SensiNactResourceModel model = 
						SecuredAccessImpl.this.mediator.getContext(
							).getService(reference);
					
					ServiceProvider provider = null;
					try
					{
						provider = (ServiceProvider) model.getRootElement(
								).getProxy(key);
						
					}catch (ModelElementProxyBuildException e) 
					{
						SecuredAccessImpl.this.mediator.error(e);
					}
					if(provider != null)
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
	 * @inheritDoc
	 *
	 * @see Session#getServiceProvider(String)
	 */
	private ServiceProvider getServiceProvider(final Session.Key key, 
			final String serviceProviderName)
	{
		final String filter = new StringBuilder().append("(&(uri=").append(
			UriUtils.getUri(new String[]{serviceProviderName})).append(
						")(lifecycle.status=ACTIVE))").toString();
		
		ServiceProvider serviceProvider =
		AccessController.<ServiceProvider>doPrivileged(
		new PrivilegedAction<ServiceProvider>()
		{
			@Override
			public ServiceProvider run()
			{
				ServiceProvider provider = null;
				try
				{
					Collection<ServiceReference<SensiNactResourceModel>>
					references = SecuredAccessImpl.this.mediator.getContext(
						).getServiceReferences(SensiNactResourceModel.class,
								filter);
					
					if(references == null || references.size()!=1)
					{
						return provider;
					}
					ServiceReference<SensiNactResourceModel> reference =
							references.iterator().next();
					
					SensiNactResourceModel model = 
						SecuredAccessImpl.this.mediator.getContext().getService(
							reference);
				
					if(model != null)
					{
						provider = (ServiceProvider) model.getRootElement(
								).getProxy(key);
					}					
				}catch(InvalidSyntaxException e)
				{
					e.printStackTrace();
					SecuredAccessImpl.this.mediator.error(e);
					
				} catch (ModelElementProxyBuildException e) 
				{
					e.printStackTrace();
					SecuredAccessImpl.this.mediator.error(e);
				}
				return provider;
			}
		});
		return serviceProvider;
	}
	/**
	 * Registers the {@link SensiNactResourceModel}  as a system one,
	 * meaning it exists in the system's datastore
	 * 
	 * @param modelInstance the {@link SensiNactResourceModel} to
	 * register
	 * 
	 * @throws SecuredAccessException 
	 * @throws ModelAlreadyRegisteredException
	 */
	@Override
	public ServiceRegistration<SensiNactResourceModel> 
	register(final SensiNactResourceModel<?> modelInstance) 
			throws SecuredAccessException
	{
		ServiceRegistration<SensiNactResourceModel>  registration = 
		AccessController.<ServiceRegistration<SensiNactResourceModel>>
		doPrivileged(new PrivilegedAction<ServiceRegistration<SensiNactResourceModel>>()
		{
			@Override
			public ServiceRegistration<SensiNactResourceModel> run()
			{
				Dictionary<String,String> props = modelInstance.getProperties();
				
				return (ServiceRegistration<SensiNactResourceModel>)
				mediator.getContext().registerService(
				SensiNactResourceModel.class.getCanonicalName(), modelInstance, props);
			}
		});		
		if(registration == null)
		{
			throw new SecuredAccessException(
				String.format("The model instance '%s' is not registered",
					modelInstance.getName()));
		}
		return registration;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#unregister(ServiceRegistration)
	 */
	@Override
	public void unregister(
			final ServiceRegistration<SensiNactResourceModel> 
			registration) throws SecuredAccessException
	{
		if(registration != null)
		{
			AccessController.<Void>doPrivileged(
					new PrivilegedAction<Void>()
			{
				@Override
				public Void run()
				{
					try
					{
						registration.unregister();
						
					} catch(IllegalStateException e)
					{
						mediator.debug("model instance already unregistered");
					}
					return null;
				}
			});	
		}
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * registerAgent(AbstractSnaAgentCallback,
	 * SnaFilter)
	 */
	@Override
	public String registerAgent(Mediator mediator,
								SnaAgentCallback callback, SnaFilter filter)
	{			
		final SnaAgentImpl agent = SnaAgentImpl.createAgent(
			mediator, callback, filter, null);
		
		String identifier = new StringBuilder().append("agent_"
				).append(agent.hashCode()).toString();
			
		ServiceRegistration registration = 
			AccessController.<ServiceRegistration>doPrivileged(
				new PrivilegedAction<ServiceRegistration>()
		{
			@Override
			public ServiceRegistration run()
			{
				ServiceRegistration<?> registration = 
					SecuredAccessImpl.this.mediator.getContext(
					).registerService(SnaAgent.class,	agent, null);
				return registration;
			}
		});		
		if(registration == null)
		{
			mediator.error("The agent is not registered");
			return null;
		}
		SecuredAccessImpl.this.agents.put(identifier, registration);
		return identifier;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * unregisterAgent(java.lang.String)
	 */
	@Override
	public void unregisterAgent(String identifier)
	{
		final ServiceRegistration<SnaAgent> registration = 
				this.agents.get(identifier);
		
		if(registration != null)
		{
			AccessController.<Void>doPrivileged(
					new PrivilegedAction<Void>()
			{
				@Override
				public Void run()
				{
					SecuredAccessImpl.this.mediator.getContext(
						).getService(registration.getReference()
								).stop();
					try
					{						
						registration.unregister();
						
					} catch(IllegalStateException e)
					{
						SecuredAccessImpl.this.mediator.error(e);
					}
					return null;
				}
			});	
		}
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#update(SensiNactResourceModel, ServiceRegistration)
	 */
	@Override
	public void update(final SensiNactResourceModel<?> modelInstance,
			final ServiceRegistration<SensiNactResourceModel> registration)
	        throws SecuredAccessException
	{
		final Dictionary<String,String> props = modelInstance.getProperties();
		
		if(registration == null)
		{
			throw new SecuredAccessException(
				String.format("The model instance '%s' is not registered",
					modelInstance.getName()));
		}
		AccessController.<Void>doPrivileged(
				new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				registration.setProperties(props);
				return null;
			}
		});	
	}


	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#createAuthorizationService()
	 */
	@Override
	public void createAuthorizationService()
	{
		AuthorizationService authorization = new AuthorizationServiceImpl();
		
		this.authorizationRegistration = this.mediator.getContext(
			).registerService(AuthorizationService.class, 
					authorization, null);
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#close()
	 */
	@Override
	public void close()
	{
		if (mediator.isDebugLoggable()) 
		{
			mediator.debug("closing sensiNact secured access");
		}
		if(this.authorizationRegistration != null)
		{
			try
			{
				this.authorizationRegistration.unregister();
				
			} catch(IllegalStateException e)
			{
				try
				{
					mediator.debug(e.getMessage());					
				} catch(IllegalStateException ise)
				{
					//do nothing because it probably means 
					//that the OSGi environment is closing
				}
			}
		}
	}
}