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

import java.security.InvalidKeyException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgentImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;

class MySecuredAccess implements SecuredAccess
{
	private Mediator mediator;
	private MySession session;	
	private Map<String, ServiceRegistration<SensiNactResourceModel>> registrations;
	private Map<String, ServiceRegistration> agents;
	
	/**
	 * @param mediator
	 */
	public MySecuredAccess(Mediator mediator)
	{
		this.mediator = mediator;
		this.session = new MySession(mediator);
		this.registrations = 
		new HashMap<String,ServiceRegistration<SensiNactResourceModel>>();
		this.agents = new HashMap<String,ServiceRegistration>();
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * getSession(java.lang.String, java.lang.String)
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
	 * @see SecuredAccess#
	 * getAnonymousSession()
	 */
	@Override
	public Session getAnonymousSession()
	{
		return this.session;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * validate(org.osgi.framework.Bundle)
	 */
	@Override
	public String validate(Bundle bundle) 
	{
		return "xxxxxxxxxxxxxx000000";
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#buildAccessNodesHierarchy(String, String, AccessTree)
	 */
	@Override
	public void buildAccessNodesHierarchy(String identifier, String name,
	        AccessTree accessTree) throws SecuredAccessException
	{
		accessTree.add(name);
		
		if(!"serviceProvider".equals(name))
		{			
			accessTree.add("serviceProvider");
		}
		accessTree.add("serviceProvider/testService");
		accessTree.add("serviceProvider/testService/location"
			).withAccessProfile(AccessProfileOption.DEFAULT.getAccessProfile());
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * getAccessTree(java.lang.String)
	 */
	@Override
	public AccessTree getAccessTree(String identifier)
	        throws SecuredAccessException
	{
		AccessTree tree = new AccessTree(mediator
			).withAccessProfile(AccessProfileOption.ALL_ANONYMOUS);
	
		return tree;
	}


	/**
	 * @inheritDoc
	 *
	 * @see Session#
	 * registerAgent(AbstractSnaAgentCallback,
	 * SnaFilter)
	 */
	@Override
	public String registerAgent(Mediator medaitor, SnaAgentCallback callback,
	        SnaFilter filter)
	{		
		BundleContext context = this.mediator.getContext();
		
		SnaAgentImpl agent = SnaAgentImpl.createAgent(mediator, 
			callback, filter, "xxxxxxxxxxxxxx000000");
		
		String identifier = new StringBuilder().append("agent_"
				).append(agent.hashCode()).toString();
		
		Hashtable props = new Hashtable();
		ServiceRegistration agentRegistration = context.registerService(
					SnaAgent.class,	agent, props);
		
		this.agents.put(identifier, agentRegistration);
		return identifier;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Session#
	 * unregisterAgent(java.lang.String)
	 */
	@Override
	public void unregisterAgent(String identifier)
	{
		this.agents.get(identifier).unregister();
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * register(SensiNactResourceModel)
	 */
	@Override
	public ServiceRegistration<SensiNactResourceModel> register(
			SensiNactResourceModel<?> modelInstance)
	{
		Dictionary<String, String> props = modelInstance.getProperties();
		
		ServiceRegistration<SensiNactResourceModel> registration = 
				this.mediator.getContext().registerService(
				SensiNactResourceModel.class, modelInstance, props);

		return registration;
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#
	 * update(SensiNactResourceModel)
	 */
	@Override
	public void update(SensiNactResourceModel<?> modelInstance,
			ServiceRegistration<SensiNactResourceModel> registration)
	{
		Dictionary<String,String> props = modelInstance.getProperties();		
		registration.setProperties(props);
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#createAuthorizationService()
	 */
	@Override
	public void createAuthorizationService()
	{
		//nothing to implement here
		//AuthorizationService already registered in the
		//moked BundleContext 
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#unregister(ServiceRegistration)
	 */
	@Override
	public void unregister(ServiceRegistration<SensiNactResourceModel> registration)
	        throws SecuredAccessException
	{
		if(registration != null)
		{
			try
			{
				registration.unregister();
				
			} catch(IllegalStateException e)
			{
				mediator.debug("model instance already unregistered");
			}
		}
		
	}

	/**
	 * @inheritDoc
	 *
	 * @see SecuredAccess#close()
	 */
	@Override
	public void close()
	{
		System.out.println("Closing the SecuredAccess");		
	}
}