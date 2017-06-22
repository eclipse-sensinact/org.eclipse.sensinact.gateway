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
import org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy;

/**
 * {@link ResourceConfig} descriptor
 */
public class ResourceDescriptor
{
	protected Class<? extends ResourceConfig> resourceConfigType;
	
	protected Class<? extends Resource> resourceType;
	protected Class<? extends ResourceImpl> resourceImplementationType;
	protected String profile;
	protected String serviceName;
	protected String resourceName;
	protected Class<?> dataType;
	protected Object value;
	protected UpdatePolicy updatePolicy;
	protected Modifiable modifiable;
	protected Boolean hidden;


//	private ResourceDescriptor ()
//	{}
	
	public ResourceDescriptor withProfile(String profile)
	{
		this.profile = profile;
		return this;
	}

	public String profile()
	{
		return this.profile;
	}
	
	public ResourceDescriptor withServiceName(String serviceName)
	{
		this.serviceName = serviceName;
		return this;
	}

	public String serviceName()
	{
		return this.serviceName;
	}

	public ResourceDescriptor withResourceName(
			String resourceName)
	{
		this.resourceName = resourceName;
		return this;
	}

	public String resourceName()
	{
		return this.resourceName;
	}
	
	public ResourceDescriptor withResourceType(
			Class<? extends Resource> resourceType)
	{
		this.resourceType = resourceType;
		return this;
	}

	public Class<? extends Resource> resourceType()
	{
		return this.resourceType;
	}
	
	public ResourceDescriptor withDataType(
			Class<?> dataType)
	{
		this.dataType = dataType;
		return this;
	}

	public Class<?> dataType()
	{
		return this.dataType;
	}

	public ResourceDescriptor withDataValue(
			Object value)
	{
		this.value = value;
		return this;
	}

	public Object dataValue()
	{
		return this.value;
	}
	
	public ResourceDescriptor withUpdatePolicy(
			UpdatePolicy updatePolicy)
	{
		this.updatePolicy = updatePolicy;
		return this;
	}

	public UpdatePolicy updatePolicy()
	{
		return this.updatePolicy;
	}

	public ResourceDescriptor withModifiable(
			Modifiable modifiable)
	{
		this.modifiable = modifiable;
		return this;
	}

	public Modifiable modifiable()
	{
		return this.modifiable;
	}

	public ResourceDescriptor withHidden(
			boolean hidden)
	{
		this.hidden = hidden;
		return this;
	}
	
	public Boolean hidden()
	{
		return this.hidden;
	}

	public ResourceDescriptor withResourceConfigType(
			Class<? extends ResourceConfig> resourceConfigType)
	{
		this.resourceConfigType = resourceConfigType;
		return this;
	}
	
	public Class<? extends ResourceConfig> resourceConfigType()
	{
		return this.resourceConfigType;
	}

	public ResourceDescriptor withResourceImplementationType(
			Class<? extends ResourceImpl> resourceImplementationType)
	{
		this.resourceImplementationType = resourceImplementationType;
		return this;
	}
	
	public Class<? extends ResourceImpl> resourceImplementationType() {
		
		return this.resourceImplementationType;
	}

}