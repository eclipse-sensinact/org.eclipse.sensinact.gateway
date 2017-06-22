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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.util.UriUtils;


/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
abstract class AbstractSession implements Session
{
	/**
	 * Searches and Returns the {@link ServiceProvider} registered in
	 * the OSGi host environment whose name is passed as parameter
	 * 
	 * @param name
	 * 		the name of the searched {@link ServiceProvider}  
	 * @return
	 * 		the {@link ServiceProvider} with the specified
	 * 		name
	 */
	protected abstract ServiceProvider getServiceProviderFromOsgiRegistry(String name);
	
	protected final Mediator mediator;
	
	//protected final List<ServiceProvider> providers;
	
    private final long alive;
    
	private Key sessionKey;
           
    /**
     * Constructor
     * 
     * @param token
     * @param userId
     */
    public AbstractSession(Mediator mediator, Key sessionKey)
    {
        this.alive = System.currentTimeMillis();
        this.mediator = mediator;
    	//this.providers = new ArrayList<ServiceProvider>();
    	
    	this.sessionKey = sessionKey;
    }
    
    /**
     * @inheritDoc
     * 
     * @see Session#getSessionKey()
     */
    public Key getSessionKey()
    {
    	return this.sessionKey;
    }

    /**
     * @inheritDoc
     *
     * @see Session#getFromUri(java.lang.String)
     */
    public final <S extends ElementsProxy<?>> S getFromUri(String uri)
    {
    	return this.getFromUri(UriUtils.getUriElements(uri));
    }

    /**
     * @inheritDoc
     *
     * @see Session#getFromUri(java.lang.String)
     */
    private final <S extends ElementsProxy<?>> S getFromUri(String[] uriElements)
    {	
    	String serviceProviderName = uriElements[0];
    	
    	ServiceProvider provider = null;    	
    	int index = -1;
//    	
//    	if((index = this.providers.indexOf(
//    		new Name<ServiceProvider>(serviceProviderName))) == -1)
//    	{    		
    		provider = this.getServiceProviderFromOsgiRegistry(
    				serviceProviderName);
    		
//    		if(provider !=null)
//    		{
//    			this.providers.add(provider);
//    		}
//    	} else
//    	{
//    		provider = this.providers.get(index);
//    	}
    	if(provider == null)
    	{
    		if(this.mediator.isDebugLoggable())
    		{
    			this.mediator.debug(new StringBuilder().append(
    					"the sensiNact service provider '"
    					).append(serviceProviderName).append(
    					"' does not exist").toString());
    		}
    		return null;
    	}
    	ElementsProxy<?> element = provider;
    	index = 1;
    	int length = (uriElements.length>3)?3:uriElements.length;
    	
    	for(;index < length && element!=null; index++)
    	{
    		element = (ElementsProxy<?>) element.element(
    				uriElements[index]);
    	}
    	if(element == null)
    	{
    		this.mediator.debug(
    			"the sensiNact resouce model element '%s' does not exist",
    				UriUtils.getUri(uriElements));
    	}
    	return (S) element;
    }

	/**
	 * @inheritDoc
	 *
	 * @see Session#
	 * getServiceProvider(java.lang.String)
	 */
    @Override
    public ServiceProvider getServiceProvider(String serviceProviderName)
    {
    	ServiceProvider provider = (ServiceProvider) this.getFromUri(
    			new String[]{serviceProviderName});
    	return provider;
    }

	/**
	 * @inheritDoc
	 *
	 * @see Session#
	 * getService(java.lang.String, java.lang.String)
	 */
    @Override
    public Service getService(String serviceProviderName, String serviceName)
    {
	    Service service = (Service) this.getFromUri(
    			new String[]{serviceProviderName, serviceName});
	    return service;
    }

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.s ecurity.Session#
	 * getResource(java.lang.String, java.lang.String, java.lang.String)
	 */
    @Override
    public Resource getResource(String serviceProviderName, String serviceName,
            String resourceName)
    {
	    Resource resource = (Resource) this.getFromUri(
    			new String[]{serviceProviderName, serviceName, resourceName});
	    return resource;
    }
    
    /** 
     * @inheritDoc
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object)
    {
        if(object == null)
        {
        	return false;
        }         
        if(object.getClass() == AbstractSession.class)
        {
        	this.sessionKey.equals(((AbstractSession)object
        			).getSessionKey());
        }
        return this.sessionKey.equals(object);
    }
}
