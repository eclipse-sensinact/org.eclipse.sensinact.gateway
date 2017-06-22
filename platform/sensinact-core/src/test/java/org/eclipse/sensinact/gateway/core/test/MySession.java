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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.ModelElementProxyBuildException;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.util.UriUtils;

@SuppressWarnings({"rawtypes","unchecked"})
class MySession implements Session
{
	private Mediator mediator;
	private Map<String, ServiceRegistration> instances;

	private final Session.Key key;
	

	/**
	 * @param mediator
	 */
	public MySession(Mediator mediator)
	{
		this.mediator = mediator;
		this.instances = new HashMap<String,ServiceRegistration>();
		this.key = new Session.Key();
		this.key.setUid(0);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Session#getServiceProviders()
	 */
	@Override
	public Set<ServiceProvider> getServiceProviders()
	{
		final Set<ServiceProvider> snaResourceModels = new HashSet<ServiceProvider>();
		ServiceReference[] references = null;
		try
		{
			references = this.mediator.getContext().getServiceReferences(
					SensiNactResourceModel.class.getCanonicalName(),
					null);
			
		} catch (InvalidSyntaxException e)
		{
			e.printStackTrace();
		}
		if (references == null || references.length == 0)
		{
			this.mediator.debug("No SnaObject registered");
			return Collections.<ServiceProvider>emptySet();
		}
		int index = 0;
		int length = references == null ? 0 : references.length;

		for (; index < length; index++)
		{
			final SensiNactResourceModel snaObject;

			if ((snaObject = (SensiNactResourceModel) 
					this.mediator.getContext().getService(
					references[index])) == null)
			{
				continue;
			}
			try 
			{
				snaResourceModels.add((ServiceProvider) 
					snaObject.getRootElement().getProxy(((Session.Key
							)this.getKey())));
				
			} catch (ModelElementProxyBuildException e) 
			{
				e.printStackTrace();
			}
		}
		return snaResourceModels;
	}

	private Key getKey() 
	{
		return this.key;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Session#getServiceProvider(java.lang.String)
	 */
	@Override
	public ServiceProvider getServiceProvider(String serviceProviderName)
	{
		ServiceProvider snaResourceModel = null;
		ServiceReference[] references = null;
		try
		{
			references = mediator.getContext().getServiceReferences(
					SensiNactResourceModel.class.getCanonicalName(),
					new StringBuilder().append("(uri=").append(
					UriUtils.PATH_SEPARATOR).append(serviceProviderName)
							.append(")").toString());
			
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		if (references == null || references.length != 1) {
			if (mediator.isDebugLoggable()) {
				mediator.debug("ServiceProvider does not exist");
			}
			return null;
		}
		final SensiNactResourceModel snaObject;

		if ((snaObject = (SensiNactResourceModel) mediator.getContext(
			).getService(references[0])) == null) 
		{
			if (mediator.isDebugLoggable())
			{
				mediator.debug("ServiceProvider no more avaialable");
			}
			return null;
		}
		try 
		{
			snaResourceModel = (ServiceProvider) snaObject.getRootElement(
				).getProxy(this.getKey());
			
		} catch (ModelElementProxyBuildException e) 
		{
			e.printStackTrace();
		}
		return snaResourceModel;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Session#getService(java.lang.String, java.lang.String)
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
	 * @see Session#getResource(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Resource getResource(String serviceProviderName,
	        String serviceName, String resourceName)
	{
		return getFromUri(UriUtils.getUri(new String[]{
				serviceProviderName, serviceName, resourceName}));
	}

	/**
	 * @inheritDoc
	 *
	 * @see Session#getFromUri(java.lang.String)
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

	/**
	 * @inheritDoc
	 *
	 * @see Session#
	 * register(SensiNactResourceModel)
	 */
	@Override
	public ServiceRegistration<SensiNactResourceModel> register(
			SensiNactResourceModel<?> modelInstance)
	{
		return null;
		
	}

	/**?
	 * @inheritDoc
	 *
	 * @see Session#
	 * unregister(SensiNactResourceModel)
	 */
	@Override
	public void unregister(ServiceRegistration<SensiNactResourceModel> registration) {
		
	}
}