/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.AttributeBuilder.Requirement;

/**
 * The default {@link ResourceConfigBuilder} is in charge of create the
 * previously non-described {@link ResourceConfig}s
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class DefaultResourceConfigBuilder implements ResourceConfigBuilder {
	private Class<? extends Resource> defaultResourceType;
	private Class<?> defaultDataType;
	private Modifiable defaultModifiable;
	private Resource.UpdatePolicy defaultUpdatePolicy;

	@Override
	public ResourceConfig getResourceConfig(ResourceDescriptor resourceDescriptor) {
		Class<? extends Resource> resourceType = (resourceDescriptor.resourceType() == null)
				? this.getDefaultResourceType()
				: resourceDescriptor.resourceType();

		Class<? extends ResourceConfig> resourceConfigType = resourceDescriptor.resourceConfigType();
		Class<? extends ResourceImpl> resourceImplementationType = resourceDescriptor.resourceImplementationType();

		ResourceConfig resourceConfig = null;
		try {
			resourceConfig = resourceConfigType.newInstance();
		} catch (Exception e) {
			return null;
		}
		TypeConfig typeConfig = new TypeConfig(resourceType);
		typeConfig.setImplementationClass(resourceImplementationType);
		
		resourceConfig.setTypeConfig(typeConfig);

		if (ActionResource.class.isAssignableFrom(resourceType)) {
			return resourceConfig;
		}
		if (resourceDescriptor.updatePolicy() != null) 
			resourceConfig.setUpdatePolicy(resourceDescriptor.updatePolicy());
		else 
			resourceConfig.setUpdatePolicy(this.getDefaultUpdatePolicy());
		
		RequirementBuilder requirementBuider  = new RequirementBuilder(Requirement.MODIFIABLE, resourceDescriptor.resourceName());
		if (resourceDescriptor.modifiable() == null) {
			requirementBuider.put(resourceDescriptor.serviceName(), this.getDefaultModifiable());
			resourceDescriptor.withModifiable(this.getDefaultModifiable());
		} else 
			requirementBuider.put(resourceDescriptor.serviceName(), resourceDescriptor.modifiable());		
		resourceConfig.addRequirementBuilder(requirementBuider);
		
		requirementBuider  = new RequirementBuilder(Requirement.TYPE, resourceDescriptor.resourceName());
		if (resourceDescriptor.dataType() == null) {
			requirementBuider.put(resourceDescriptor.serviceName(), this.getDefaultDataType());
			resourceDescriptor.withDataType(this.getDefaultDataType());
		} else
			requirementBuider.put(resourceDescriptor.serviceName(), resourceDescriptor.dataType());
		resourceConfig.addRequirementBuilder(requirementBuider);
		
		return resourceConfig;
	}

	/**
	 * Returns the default extended {@link Resource} Type to be used by this
	 * ResourceConfigBuilder
	 * 
	 * @return this ResourceConfigBuilder's default {@link Resource} Type
	 */
	private Class<? extends Resource> getDefaultResourceType() {
		if (this.defaultResourceType == null) {
			return RESOURCE_TYPE;
		}
		return defaultResourceType;
	}

	@Override
	public void setDefaultResourceType(Class<? extends Resource> defaultResourceType) {
		this.defaultResourceType = defaultResourceType;
	}

	/**
	 * Returns the default data Type to be used by this ResourceConfigBuilder
	 * 
	 * @return this ResourceConfigBuilder's default data Type
	 */
	private Class<?> getDefaultDataType() {
		if (this.defaultDataType == null) {
			return DATA_TYPE;
		}
		return defaultDataType;
	}

	@Override
	public void setDefaultDataType(Class<?> defaultDataType) {
		this.defaultDataType = defaultDataType;
	}

	/**
	 * Returns the default {@link Modifiable} to be used by this
	 * ResourceConfigBuilder
	 * 
	 * @return this ResourceConfigBuilder's default {@link Modifiable}
	 */
	private Modifiable getDefaultModifiable() {
		if (this.defaultModifiable == null) {
			return MODIFIABLE;
		}
		return defaultModifiable;
	}

	@Override
	public void setDefaultModifiable(Modifiable defaultModifiable) {
		this.defaultModifiable = defaultModifiable;
	}

	/**
	 * Returns the default {@link Resource.UpdatePolicy} to be used by this
	 * ResourceConfigBuilder
	 * 
	 * @return this ResourceConfigBuilder's default {@link Resource.UpdatePolicy}
	 */
	private Resource.UpdatePolicy getDefaultUpdatePolicy() {
		if (this.defaultUpdatePolicy == null)
			return UPDATE_POLICY;
		return defaultUpdatePolicy;
	}

	@Override
	public void setDefaultUpdatePolicy(Resource.UpdatePolicy defaultUpdatePolicy) {
		this.defaultUpdatePolicy = defaultUpdatePolicy;
	}
}