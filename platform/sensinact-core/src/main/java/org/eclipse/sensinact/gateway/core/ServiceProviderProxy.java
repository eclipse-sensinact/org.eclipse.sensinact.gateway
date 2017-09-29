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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Localizable;
import org.eclipse.sensinact.gateway.common.primitive.Stateful;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * {@link ServiceProvider} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceProviderProxy extends ModelElementProxy<Service> 
implements ElementsProxy<Service>, ServiceCollection, Localizable, 
Stateful<ServiceProvider.LifecycleStatus>
{
	/**
	 * 
	 */
	private ServiceProviderDescription description;

	/**
	 * @param mediator
	 * @param description
	 * @param proxies
	 * @throws InvalidValueException 
	 */
    public ServiceProviderProxy(Mediator mediator,
            String name, List<Service> proxies) 
    {
    	super(mediator, ServiceProvider.class, 
    		UriUtils.getUri(new String[]{name}));
    	
    	super.elements.addAll(proxies);
    }

	/**
	 * @inheritDoc
	 *
	 * @see Describable#getDescription()
	 */
    @Override
    @SuppressWarnings("unchecked")
    public ServiceProviderDescription getDescription()
    {
    	if(this.description == null)
    	{
    		List<ServiceDescription> descriptions = 
    				new ArrayList<ServiceDescription>();
    		
    		Enumeration elements = this.elements();
    		
    		while(elements.hasMoreElements())
    		{
    			Service service = (Service) elements.nextElement();
    			ServiceDescription serviceDescription = 
    					service.getDescription();
    			
    			if(serviceDescription!=null)
    			{
    				descriptions.add(serviceDescription);
    			}
    		}
    		this.description = new ServiceProviderDescription(
    				super.mediator, super.uri, descriptions);
    	}
	    return this.description;
    }

	/** 
	 * @inheritDoc
	 * 
	 * @see Localizable#getLocation()
	 */
	@Override
	public String getLocation() 
	{
		String location = null;
		Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
		if(admin != null)
		{
			location = admin.get(LocationResource.LOCATION).getResponse(
					String.class, DataResource.VALUE);
		}
		return location;
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see Localizable#setLocation(java.lang.String)
	 */
	@Override
	public String setLocation(String location) 
			throws InvalidValueException 
	{
		String setLocation = null;
		Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
		if(admin != null)
		{
			setLocation = admin.set(LocationResource.LOCATION, 
					location).getResponse(String.class, DataResource.VALUE);
		}
		return setLocation;
	}
    
	/** 
	 * @inheritDoc
	 * 
	 * @see Stateful#getStatus()
	 */
	@Override
	public ServiceProvider.LifecycleStatus getStatus() 
	{
		ServiceProvider.LifecycleStatus status = null;
		Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
		if(admin != null)
		{
			status = admin.get(ServiceProvider.LIFECYCLE_STATUS).getResponse(
				ServiceProvider.LifecycleStatus.class, 
					DataResource.VALUE);
		}
		return status;
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see Stateful#setStatus(java.lang.Enum)
	 */
	@Override
	public ServiceProvider.LifecycleStatus setStatus(
		ServiceProvider.LifecycleStatus status) throws InvalidValueException 
	{
		ServiceProvider.LifecycleStatus setStatus = null;
		Service admin = getService(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
		if(admin != null)
		{
			setStatus = admin.set(ServiceProvider.LIFECYCLE_STATUS, 
				status).getResponse(ServiceProvider.LifecycleStatus.class, 
					DataResource.VALUE);
		}
		return setStatus;
	}

	/**
	 * @inheritDoc
	 * 
     * @see ServiceCollection#
     * getServices()
     */
    public List<Service> getServices()
    {
    	return Collections.<Service>unmodifiableList(super.elements);
    }

	/**
	 * @inheritDoc
	 * 
     * @see ServiceCollection#
     * getService(java.lang.String)
     */
    public Service getService(String serviceName)
    {
    	return super.element(serviceName);
    }

	/**
	 * @inheritDoc
	 *
     * @see SensiNactResourceModelElementProxy#
     * getAccessMethod(AccessMethod.Type)
     */
    @Override
	public AccessMethod getAccessMethod(String name)
    {
	    return null;
    }
}
