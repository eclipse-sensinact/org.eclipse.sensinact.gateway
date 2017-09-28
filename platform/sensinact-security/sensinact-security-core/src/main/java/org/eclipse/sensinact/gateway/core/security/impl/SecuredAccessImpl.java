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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.*;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgentImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationService;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.core.security.Session.Key;
import org.eclipse.sensinact.gateway.core.security.Sessions;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.dao.AgentDAO;
import org.eclipse.sensinact.gateway.core.security.dao.BundleDAO;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.dao.ObjectDAO;
import org.eclipse.sensinact.gateway.core.security.dao.ObjectProfileAccessDAO;
import org.eclipse.sensinact.gateway.core.security.entity.AgentEntity;
import org.eclipse.sensinact.gateway.core.security.entity.BundleEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * Secured access service implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("rawtypes")
public class SecuredAccessImpl implements SecuredAccess 
{

	// ********************************************************************//
	// 						NESTED DECLARATIONS 						   //
	// ********************************************************************//

	// ********************************************************************//
	// 						ABSTRACT DECLARATIONS 						   //
	// ********************************************************************//

	// ********************************************************************//
	// 						STATIC DECLARATIONS 							//
	// ********************************************************************//

	public static final long ANONYMOUS_ID = 0;
	public static final String ANONYMOUS_PKEY = "anonymous";

	// ********************************************************************//
	// 						INSTANCE DECLARATIONS 							//
	// ********************************************************************//

	private final Mediator mediator;

	private BundleDAO bundleDAO;
	private AgentDAO agentDAO;
	private ObjectDAO objectDAO;
	private ObjectProfileAccessDAO objectProfileAccessDAO;
	
	private AccessProfileOption rootObjectProfileOption;	
	private Map<String, ServiceRegistration> agents;

	private ServiceRegistration<AuthenticationService> authenticationRegistration;
	private ServiceRegistration<AuthorizationService> authorizationRegistration;
	private ServiceRegistration<Sessions> sessionsRegistration;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the extended {@link Mediator} associated to the
	 *            SecuredAccessImpl to instantiate
	 */
	public SecuredAccessImpl(Mediator mediator) 
			throws SecuredAccessException
	{
		this.mediator = mediator;
		this.agents = new HashMap<String, ServiceRegistration>();
		
		this.sessionsRegistration = this.mediator.getContext(
			).registerService(Sessions.class, new SessionsImpl(), null);
		try
		{
			this.agentDAO = new AgentDAO(mediator);
			this.objectDAO = new ObjectDAO(mediator);
			this.bundleDAO = new BundleDAO(mediator);
			this.objectProfileAccessDAO = new ObjectProfileAccessDAO(mediator);
			
			ObjectEntity object = this.objectDAO.select(
				new HashMap<String,Object>()
					{{
						this.put("OID", 0l);
						
					}}).get(0);
			
			rootObjectProfileOption = 
				this.objectProfileAccessDAO.getAccessProfileOption(
					object.getObjectProfileEntity());			
		}
		catch (DAOException e)
		{
			throw new SecuredAccessException(e);
		}
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
	public Session getSession(final Authentication<?> authentication)
			throws InvalidKeyException, DataStoreException 
	{
		Session session = null;
		if(Credentials.class.isAssignableFrom(authentication.getClass()))
		{
			final UserKey key = mediator.callService(AuthenticationService.class,			
			new Executable<AuthenticationService, UserKey>() 
			{
				@Override
				public UserKey execute(AuthenticationService service) 
						throws Exception 
				{
					return service.buildKey((Credentials)authentication);
				}
			});				
			session = this.getSession(key.getUid());
			
			if (session == null) 
			{		
				session = mediator.callService(Sessions.class, 
				new Executable<Sessions, Session>() 
				{
					@Override
					public Session execute(final Sessions sessions) 
							throws Exception 
					{
						Key sessionKey = new Session.Key();
						sessionKey.setToken(sessions.nextToken());
						sessionKey.setPublicKey(key.getPublicKey());
						sessionKey.setUid(key.getUid());
						
						Session session = 
								SecuredAccessImpl.this.createSession(
								sessionKey);
						
						sessions.register(session);
						return session;
					}
				});
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
	 * @param userId
	 * @return
	 * @throws InvalidKeyException
	 * @throws DataStoreException
	 */
	private Session getSession(final Long userId)
	{
		Session session = mediator.callService(Sessions.class, 
		new Executable<Sessions, Session>() 
		{
			@Override
			public Session execute(Sessions sessions) 
					throws Exception 
			{
				return sessions.get(userId);
			}
		});
		return session;
	}
	
	/**
	 * @param token
	 * @return
	 * @throws InvalidKeyException
	 * @throws DataStoreException
	 */
	public Session getSession(final String token)
	{
		Session session = mediator.callService(Sessions.class, 
		new Executable<Sessions, Session>() 
		{
			@Override
			public Session execute(Sessions sessions) 
					throws Exception 
			{
				return sessions.get(token);
			}
		});
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
		Session anonymous = null;
		try 
		{
			anonymous = this.getSession(ANONYMOUS_ID);	
			if (anonymous == null) 
			{
				anonymous = this.getSession(new Credentials(
						"ANONYMOUS",  "anonymous"));
			}
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (DataStoreException e) {
			e.printStackTrace();
		}
		return anonymous;
	}

//	/**
//	 * Creates and registers the {@link SnaAgent} whose callback is passed as
//	 * parameter
//	 * 
//	 * @param agent
//	 *            the {@link AbstractSnaAgentCallback} that will be called by
//	 *            the SnaAgent to create and to register
//	 * @param uid
//	 *            the integer identifier of the calling user
//	 */
//	private String registerAgent(AbstractSnaAgentCallback callback,
//			SnaFilter filter, Session.Key key)
//	{
//		SnaAgentImpl agent = SnaAgentImpl.createAgent(mediator, 
//				callback, filter, key);
//		
//		String identifier = new StringBuilder().append("agent_"
//				).append(agent.hashCode()).toString();
//
//		ServiceRegistration agentRegistration = mediator.getContext(
//			).registerService(SnaAgent.class.getCanonicalName(),
//				agent, null);
//
//		this.agents.put(identifier, agentRegistration);
//		return identifier;
//	}
//
//	/**
//	 * Unregisters and stops the {@link SnaAgent} whose identifier is passed as
//	 * parameter
//	 * 
//	 * @param identifier
//	 *            the string identifier of the {@link SnaAgent} to stop and to
//	 *            unregister
//	 * @param uid
//	 *            the integer identifier of the calling user
//	 */
//	private void unregisterAgent(String identifier, long uid)
//	{
//		ServiceRegistration<SnaAgent> registration = 
//				this.agents.remove(identifier);
//		try 
//		{
//			if (registration != null)
//			{
//				SnaAgent agent = (SnaAgent) mediator.getContext(
//					).getService(registration.getReference());
//
//				if (agent != null) 
//				{
//					agent.stop();
//				}
//				registration.unregister();
//			}
//		} catch (Exception e)
//		{
//			this.mediator.error(e);
//		}
//	}

	private final Session createSession(final Session.Key sessionKey) 
	{
		Session session = new AbstractSession(mediator, sessionKey) 
		{
			/**
			 * @inheritDoc
			 * 
			 * @see AbstractSession#
			 *      getServiceProviderFromOsgiRegistry(java.lang.String)
			 */
			@Override
			public ServiceProvider getServiceProviderFromOsgiRegistry(
					final String name) 
			{
				return AccessController.doPrivileged(
				new PrivilegedAction<ServiceProvider>()
				{
					@Override
					public ServiceProvider run() 
					{
						return SecuredAccessImpl.this.getServiceProvider(
								name, sessionKey);
					}
				});
			}

			/**
			 * @return 
			 * @inheritDoc
			 * 
			 * @see Session#
			 *      registered(org.eclipse.sensinact.gateway.core.AbstractModelElement)
			 */
			@Override
			public ServiceRegistration<SensiNactResourceModel> register(
					final SensiNactResourceModel resourceModel) 
			{
				return AccessController.doPrivileged(					
				new PrivilegedAction<ServiceRegistration<SensiNactResourceModel>>() 
				{
					@Override
					public ServiceRegistration<SensiNactResourceModel> run() {
						try
						{
							return SecuredAccessImpl.this.register(resourceModel);
							
						} catch (ModelAlreadyRegisteredException e) 
						{
							SecuredAccessImpl.this.mediator.error(e);
							
						} catch (SecuredAccessException e) {
							
							SecuredAccessImpl.this.mediator.error(e);
						}
						return null;
					}
				});
			}

			/**
			 * @inheritDoc
			 * 
			 * @see Session#
			 *      getServiceProviders()
			 */
			@Override
			public Set<ServiceProvider> getServiceProviders()
			{
				return AccessController.doPrivileged(
				new PrivilegedAction<Set<ServiceProvider>>() 
				{
					@Override
					public Set<ServiceProvider> run()
					{
						return SecuredAccessImpl.this.getServiceProviders(
								sessionKey);
					}
				});
			}

			@Override
			public void unregister(
			     final ServiceRegistration<SensiNactResourceModel> registration)
			     throws SecuredAccessException
			{
				AccessController.doPrivileged(new PrivilegedAction<Void>() 
				{
					@Override
					public Void run() 
					{
						try
						{
							SecuredAccessImpl.this.unregister(registration);
						}
						catch (SecuredAccessException e)
						{
							e.printStackTrace();
						}
						return null;
					}
				});
				
			}
		};
		Sessions.SESSIONS.set(session);
		return session;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#close()
	 */
	public void close()
	{
		if (mediator.isDebugLoggable()) 
		{
			mediator.debug("closing sensiNact secured access");
		}
		if(this.sessionsRegistration != null)
		{
			try
			{
				this.sessionsRegistration.unregister();
				
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
		if(this.authenticationRegistration != null)
		{
			try
			{
				this.authenticationRegistration.unregister();
				
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

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#buildAccessNodesHierarchy(String, String, AccessTree)
	 */
	@Override
	public void buildAccessNodesHierarchy(String signature, 
			String name, AccessTree tree) 
			throws SecuredAccessException
	{
		try 
		{
			if(name == null)
			{
				throw new NullPointerException(
						"The sensiNact resource model's name is missing");
			}
			if(!checkIdentifier(signature, name))
			{
				if(signature == null)
				{
					throw new SecuredAccessException(String.format(
					"A '%s' sensiNact resource model exists in the data store",
							name));
				} else
				{
					throw new SecuredAccessException("Invalid bundle identifier");
				}				
			} else if(signature != null)
			{
				buildNode(tree, this.objectDAO.find(
						UriUtils.getUri(new String[]{name})));
			}	
		} catch (DAOException e)
		{
			throw new SecuredAccessException(e);
			
		} catch (Exception e)
		{
			throw new SecuredAccessException(e);
		}
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#getAccessTree(String)
	 */
	@Override
	public AccessTree getAccessTree(String signature) 
			throws SecuredAccessException
	{
//		System.out.println("BUILDING ACCESS TREE  FOR "+signature);
		AccessTree tree = null;	
		BundleEntity object = null;
		AccessProfileOption option = null;		
		try 
		{
			if(signature == null || (object = this.bundleDAO.find(
					signature)) == null)
			{
				option = rootObjectProfileOption;
				
			} else
			{
				option = this.objectProfileAccessDAO.getAccessProfileOption(
					object.getObjectProfileEntity());
			}
			tree = new AccessTree(mediator).withAccessProfile(option);
			
		} catch (Exception e)
		{
			//e.printStackTrace();
			throw new SecuredAccessException(e);
		}
		//System.out.println(tree);
		return tree;
	}

//	/**
//	 * @inheritDoc
//	 *
//	 * @see SecuredAccess#
//	 * getRootNode(java.lang.String)
//	 */
//	public AccessNode getCompositionNode(
//			String signature, String[] paths) 
//			throws SecuredAccessException
//	{
//		AccessNode node = null;
//		try
//		{
//			int length = paths==null?0:paths.length;
//			int index = 0;
//			
//			for(;index < length; index++)
//			{
//				String path = paths[index];
//				
//				if(path == null)
//				{
//					throw new SecuredAccessException(
//						"Invalid resource model element's path : null");
//				}
//				//Does the ObjectEntity exist in the datastore
//				ObjectEntity object = this.objectDAO.find(path);
//				
//				AccessProfileOption option = null;
//				//if it is not the case, search for reified resource models
//				//in the current system
//				if(object == null)
//				{
//					
//					
//				} else
//				{
//					option = this.objectProfileAccessDAO.getAccessProfileOption(
//						object.getObjectProfileEntity());
//				}
//				//Set<MethodAccess> methodAccesses = accessProfile.getMethodAccesses();
//			}			
//		} catch (Exception e)
//		{
//			throw new SecuredAccessException(e);
//		}
//		return node;
//	}
	
	/**
	 * @param signature
	 * @param name
	 * @return
	 * @throws DAOException 
	 */
	private boolean checkIdentifier(String signature, String name)
			throws DAOException
	{
		if(name==null)
		{
			return false;
		}
		ObjectEntity entity = this.objectDAO.find(
			UriUtils.getUri(new String[]{name}), true);
		
		if(signature == null)
		{
			return (entity == null);
		}
		BundleEntity bundle = null;
		
		return (entity == null || ((bundle = this.bundleDAO.find(signature
			))!= null) && (bundle.getIdentifier()== entity.getBundleEntity()));
	}

	/**
	 * @param tree
	 * @param object
	 * @throws Exception
	 */
	private void buildNode(AccessTree tree, ObjectEntity object)
			throws Exception 
	{

//		System.out.println("\t====> BUILDING NODE SUITE "+ object );
		if (object == null || /*it means that the root has been reached*/
				object.getPath()==null) 
		{
			return;
		}
		try
		{
			AccessProfileOption option = 
				this.objectProfileAccessDAO.getAccessProfileOption(
				    object.getObjectProfileEntity());
//			System.out.println("\t\t====> ADDING NODE "+ object.getPath());
//			System.out.println("\t\t\t "+ option.name());
			tree.add(object.getPath()).withAccessProfile(option);
			
		} catch (Exception e)
		{
			this.mediator.error(e);
			throw e;
		}
		List<ObjectEntity> children = this.objectDAO.findChildren(
				object.getIdentifier());

		Iterator<ObjectEntity> iterator = children.iterator();
		
		while (iterator.hasNext()) 
		{
			ObjectEntity entity = iterator.next();
			buildNode(tree, entity);
		}
	}


	/**
	 * @throws SecuredAccessException 
	 * @inheritDoc
	 *
	 * @see SecuredAccess#createAuthorizationService()
	 */
	@Override
	public void createAuthorizationService() throws SecuredAccessException
	{
		try
		{
			this.authorizationRegistration = this.mediator.getContext(
			).registerService(AuthorizationService.class, 
				new AuthorizationServiceImpl(mediator), null);
			
			this.authenticationRegistration = this.mediator.getContext(
			).registerService(AuthenticationService.class, 
				new AuthenticationServiceImpl(mediator), null);
		}
		catch (DAOException e)
		{
			throw new SecuredAccessException(e);
		}
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see Session#getServiceProviders()
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
						provider = (ServiceProvider) 
							model.getRootElement().getProxy(key);
						
					}catch (ModelElementProxyBuildException e) 
					{
						SecuredAccessImpl.this.mediator.error(e);
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
	 * @inheritDoc
	 *
	 * @see Session#getServiceProvider(String)
	 */
	private ServiceProvider getServiceProvider(
			final String serviceProviderName,
			final Session.Key key)
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
						SecuredAccessImpl.this.mediator.getContext(
							).getService(reference);
				
					if(model != null)
					{
						provider = (ServiceProvider) model.getRootElement(
								).getProxy(key);
					}					
				}catch(InvalidSyntaxException e)
				{
					SecuredAccessImpl.this.mediator.error(e);
					
				} catch (ModelElementProxyBuildException e) 
				{
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
	 * @param modelInstance the {@link SensiNactResourceModel} to register
	 *
	 * @return 
	 * 
	 * @throws SecuredAccessException 
	 * @throws ModelAlreadyRegisteredException
	 */
	@Override
	public ServiceRegistration<SensiNactResourceModel> register(
			final SensiNactResourceModel modelInstance) 
			throws SecuredAccessException
	{
		ServiceRegistration registration = 
		AccessController.<ServiceRegistration>doPrivileged(
			new PrivilegedAction<ServiceRegistration>()
		{
			@Override
			public ServiceRegistration run()
			{
				Dictionary<String,String> props = modelInstance.getProperties();		
				ServiceRegistration registration = mediator.getContext(
					).registerService(SensiNactResourceModel.class.getCanonicalName(),
								modelInstance, props);
				return registration;
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
	 * @see SecuredAccess#
	 * unregister(SensiNactResourceModel)
	 */
	@Override
	public void unregister(
		final ServiceRegistration<SensiNactResourceModel> 
		registration)  throws SecuredAccessException
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
	public String registerAgent(Mediator mediator, SnaAgentCallback callback,
	        SnaFilter filter)
	{			
		final Bundle bundle = mediator.getContext(
				).getBundle();

        String bundleIdentifier = this.mediator.callService(BundleValidation.class,
                new Executable<BundleValidation, String>() {
                    @Override
                    public String execute(BundleValidation service) throws Exception {
                        return service.check(bundle);
                    }
                });

        String agentKey = null;
		try
		{
			agentKey = this.getAgentPublicKey(bundleIdentifier);
		}
		catch (DAOException e)
		{
			mediator.error("Unable to retrieve the agent key");
		}    	
		final SnaAgentImpl agent = SnaAgentImpl.createAgent(
			mediator, callback, filter, agentKey);
		
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
	 * @param signature
	 * @return
	 * @throws DAOException 
	 */
	private String getAgentPublicKey(String signature) throws DAOException
	{
		String agentKey = null;
		AgentEntity entity = this.agentDAO.findFromBundle(signature);
		if(entity != null)
		{
			agentKey = entity.getPublicKey();
		}
		return agentKey;
		
	}
	
	/**
	 * Unregisters the {@link SnaAgent} whose identifier
	 * is passed as parameter
	 * 
	 * @param identifier
	 * 		the identifier of the {@link SnaAgent} to 
	 * 		register
	 */
	public  void unregisterAgent(String identifier)
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
	 * @see SecuredAccess#
	 * update(SensiNactResourceModel,
	 * org.osgi.framework.ServiceRegistration)
	 */
	@Override
	public void update(
			final SensiNactResourceModel<?> modelInstance,
			final ServiceRegistration<SensiNactResourceModel> registration)
			throws SecuredAccessException
	{
		if(registration == null)
		{
			throw new SecuredAccessException(
				String.format("The model instance '%s' is not registered",
					modelInstance.getName()));
		}
		AccessController.<Void>doPrivileged(new PrivilegedAction<Void>()
		{
			@Override
			public Void run()
			{
				registration.setProperties(
						modelInstance.getProperties());
				return null;
			}
		});	
	}
}
