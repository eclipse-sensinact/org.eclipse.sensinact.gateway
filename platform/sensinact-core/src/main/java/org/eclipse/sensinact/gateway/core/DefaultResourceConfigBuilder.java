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

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;

/**
 * The default {@link ResourceConfigBuilder} is in charge of create
 * the previously non-described {@link ResourceConfig}s 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DefaultResourceConfigBuilder implements ResourceConfigBuilder
{
	private Class<? extends Resource> defaultResourceType;	
	private Class<?> defaultDataType;
	private Modifiable defaultModifiable;
	private Resource.UpdatePolicy defaultUpdatePolicy;
	
	/** 
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ResourceConfigBuilder#
	 * getResourceConfig(org.eclipse.sensinact.gateway.core.ResourceDescriptor)
	 */
	@Override
	public ResourceConfig getResourceConfig(ResourceDescriptor 
			resourceConfigDescriptor)
	{
		Class<? extends Resource> resourceType = 
			(resourceConfigDescriptor.resourceType() == null)
			?this.getDefaultResourceType()
			:resourceConfigDescriptor.resourceType();
		
		Class<? extends ResourceConfig> resourceConfigType = 
			resourceConfigDescriptor.resourceConfigType();

		Class<? extends ResourceImpl> resourceImplementationType = 
			resourceConfigDescriptor.resourceImplementationType();
		
		ResourceConfig resourceConfig = null;
		try
		{			
			resourceConfig = resourceConfigType.newInstance();
			
		} catch(Exception e)
		{
			return null;
		}
    	TypeConfig typeConfig = new TypeConfig(resourceType);
    	typeConfig.setImplementationClass(resourceImplementationType);
    	
    	resourceConfig.setTypeConfig(typeConfig); 

    	if(ActionResource.class.isAssignableFrom(resourceType))
    	{
    		return resourceConfig;
    	}
    	if(resourceConfigDescriptor.updatePolicy() != null)
    	{
    		resourceConfig.setUpdatePolicy(
    				resourceConfigDescriptor.updatePolicy());
    	} else
    	{
    		resourceConfig.setUpdatePolicy(
    				this.getDefaultUpdatePolicy());
    	}
    	if(resourceConfigDescriptor.modifiable() == null)
    	{
    		resourceConfigDescriptor.withModifiable(
    				this.getDefaultModifiable());
    	}
    	if(resourceConfigDescriptor.dataType() == null)
    	{
    		resourceConfigDescriptor.withDataType(
    				this.getDefaultDataType());
    	}
		return resourceConfig;
	}


	/**
	 * Returns the default extended {@link Resource} Type to be 
	 * used by this ResourceConfigBuilder
	 * 
	 * @return this ResourceConfigBuilder's default {@link 
	 * Resource} Type
	 */
	private Class<? extends Resource> getDefaultResourceType()
	{
		if(this.defaultResourceType == null)
		{
			return RESOURCE_TYPE;
		}
		return defaultResourceType;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ResourceConfigBuilder#
	 * setDefaultResourceType(java.lang.Class)
	 */
	public void setDefaultResourceType(Class<? extends Resource> defaultResourceType)
	{
		this.defaultResourceType = defaultResourceType;
	}

	/**
	 * Returns the default data Type to be used by this 
	 * ResourceConfigBuilder
	 * 
	 * @return this ResourceConfigBuilder's default data
	 * Type
	 */
	private Class<?> getDefaultDataType()
	{
		if(this.defaultDataType == null)
		{
			return DATA_TYPE;
		}
		return defaultDataType;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ResourceConfigBuilder#
	 * setDefaultDataType(java.lang.Class)
	 */
	public void setDefaultDataType(Class<?> defaultDataType)
	{
		this.defaultDataType = defaultDataType;
	}


	/**
	 * Returns the default {@link Modifiable} to be used by this 
	 * ResourceConfigBuilder
	 * 
	 * @return this ResourceConfigBuilder's default {@link 
	 * Modifiable}
	 */
	private Modifiable getDefaultModifiable()
	{
		if(this.defaultModifiable == null)
		{
			return MODIFIABLE;
		}
		return defaultModifiable;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ResourceConfigBuilder#setDefaultModifiable(Modifiable)
	 */
	public void setDefaultModifiable(Modifiable defaultModifiable)
	{
		this.defaultModifiable = defaultModifiable;
	}

	/**
	 * Returns the default {@link Resource.UpdatePolicy} to be used by this
	 * ResourceConfigBuilder
	 * 
	 * @return this ResourceConfigBuilder's default {@link 
	 * Resource.UpdatePolicy}
	 */
	private Resource.UpdatePolicy getDefaultUpdatePolicy()
	{
		if(this.defaultUpdatePolicy == null)
		{
			return UPDATE_POLICY;
		}
		return defaultUpdatePolicy;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ResourceConfigBuilder#
	 * setDefaultUpdatePolicy(Resource.UpdatePolicy)
	 */
	public void setDefaultUpdatePolicy(Resource.UpdatePolicy defaultUpdatePolicy)
	{
		this.defaultUpdatePolicy = defaultUpdatePolicy;
	}
}