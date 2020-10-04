/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.AttributeBuilder.Requirement;
import org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy;

/**
 * Resource configuration
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceConfig implements Nameable {
	public static final String ALL_TARGETS = "#ANY_TARGET#";
	public static final String ALL_PROFILES = "#ANY_PROFILE#";

	protected List<String> profiles;
	protected List<String> targets;

	protected TypeConfig typeConfig;
	protected List<RequirementBuilder> requirementBuilders;
	protected UpdatePolicy updatePolicy;

	/**
	 * Constructor
	 */
	public ResourceConfig() {
		this.requirementBuilders = new ArrayList<RequirementBuilder>();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.getName(ResourceConfig.ALL_TARGETS);
	}

	/**
	 * Returns the registered name for the specified service
	 */
	public String getName(String service) {
		if (service == null || service.length() == 0) {
			return this.getName(ResourceConfig.ALL_TARGETS);
		}
		String name = null;
		int index = -1;
		if ((index = this.requirementBuilders.indexOf(new Name<RequirementBuilder>(Resource.NAME))) > -1) {
			name = (String) this.requirementBuilders.get(index).get(service);
		}
		return name;
	}

	/**
	 * Configures the name of the resource to build
	 * 
	 * @param name
	 *            the name of the resource to build
	 */
	public void configureName(String service, String name) {
		int index = -1;
		if ((index = this.requirementBuilders.indexOf(new Name<RequirementBuilder>(Resource.NAME))) > -1) {
			this.requirementBuilders.get(index).put(service, name);

		} else {
			RequirementBuilder builder = new RequirementBuilder(Requirement.VALUE, Resource.NAME);
			builder.put(service, name);
			this.requirementBuilders.add(builder);
		}
	}

	/**
	 * Returns the {@link TypeConfig} which applies on {@link ResourceImpl}
	 * instances based on this ResourceConfig
	 * 
	 * @return the {@link TypeConfig} which applies
	 */
	public TypeConfig getTypeConfig() {
		return this.typeConfig;
	}

	/**
	 * Defines the {@link TypeConfig} which applies on {@link ResourceImpl}
	 * instances based on this ResourceConfig
	 * 
	 * @param policy
	 *            the {@link TypeConfig} which applies
	 */
	public void setTypeConfig(TypeConfig typeConfig) {
		this.typeConfig = typeConfig;
	}

	/**
	 * Defines the {@link UpdatePolicy} which applies on {@link ResourceImpl}
	 * instances based on this ResourceConfig
	 * 
	 * @param updatePolicy
	 *            the {@link UpdatePolicy} which applies
	 */
	public void setUpdatePolicy(UpdatePolicy updatePolicy) {
		this.updatePolicy = updatePolicy;
	}

	/**
	 * Returns the {@link UpdatePolicy} which applies on {@link ResourceImpl}
	 * instances based on this ResourceConfig
	 * 
	 * @return the {@link UpdatePolicy} which applies
	 */
	public UpdatePolicy getUpdatePolicy() {
		if (this.updatePolicy == null) {
			return UpdatePolicy.NONE;
		}
		return this.updatePolicy;
	}

	/**
	 * Returns true if the service whose identifier is passed as parameter is
	 * targeted by this ResourceConfig; otherwise returns false
	 * 
	 * @return true if the service whose identifier is passed as parameter is
	 *         defined as targeted; <br/>
	 *         false otherwise
	 */
	public boolean isTargeted(String serviceId) {
		if (serviceId == null || serviceId.length() == 0) {
			return this.isTargeted(ResourceConfig.ALL_TARGETS);
		}
		if (this.targets == null || this.targets.size() == 0) {
			return !serviceId.equals(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
		}
		return targets.indexOf(serviceId) > -1 || (targets.indexOf(ResourceConfig.ALL_TARGETS) > -1 
				&& !ServiceProvider.ADMINISTRATION_SERVICE_NAME.equals(serviceId));
	}

	/**
	 * Defines specific {@link Service} targets for this XmlResourceConfig
	 * 
	 * @param target
	 *            specific {@link Service} targets for this ResourceConfig
	 */
	public void setTarget(String target) {
		if(target==null){
			return;
		}
		this.targets = Arrays.asList(target.split(","));
	}

	/**
	 * Returns true if the profile whose identifier is passed as parameter is one
	 * for which this ResourceConfig; otherwise returns false
	 * 
	 * @return true if the service whose identifier is passed as parameter is
	 *         defined as targeted; <br/>
	 *         false otherwise
	 */
	public boolean isProfiled(String profileId) {
		if (profileId == null) {
			return this.isProfiled(ResourceConfig.ALL_PROFILES);
		}
		if (this.profiles == null) {
			return ResourceConfig.ALL_PROFILES.equals(profileId);
		}
		return this.profiles.indexOf(profileId) > -1 || this.profiles.indexOf(ResourceConfig.ALL_PROFILES) > -1;
	}

	/**
	 * Defines specific profile for this XmlResourceConfig
	 * 
	 * @param policy
	 *            specific profile for this XmlResourceConfig
	 */
	public void setProfile(String profile) {
		if(profile == null) {
			return;
		}
		this.profiles = Arrays.asList(profile.split(","));
	}

	/**
	 * Adds the {@link RequirementBuilder} passed as parameter, that will be applied
	 * on the set of {@link AttributeBuilder}s used to create the set of
	 * {@link Attribute}s of the {@link ResourceImpl} instances based on this
	 * ResourceConfig
	 * 
	 * @param requirementBuilder
	 *            the {@link RequirementBuilder} to add
	 */
	public void addRequirementBuilder(RequirementBuilder requirementBuilder) {
		if (requirementBuilder == null) {
			return;
		}
		int index = -1;
		RequirementBuilder builder = null;

		if ((index = this.requirementBuilders.indexOf(requirementBuilder)) > -1) {
			builder = this.requirementBuilders.get(index);
			Iterator<Map.Entry<String, Object>> iterator = requirementBuilder.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = iterator.next();
				builder.put(entry.getKey(), entry.getValue());
			}

		} else {
			this.requirementBuilders.add(requirementBuilder);
		}
	}

	/**
	 * Returns the set of {@link AttributeBuilder}s for the configured
	 * {@link Resource} type
	 * 
	 * @return the set of {@link AttributeBuilder}s for the configured
	 *         {@link Resource} type
	 */
	public List<AttributeBuilder> getAttributeBuilders(String service) {
		List<AttributeBuilder> builders = this.typeConfig.getAttributeBuilders();

		Iterator<RequirementBuilder> iterator = this.requirementBuilders.iterator();

		while (iterator.hasNext()) {
			iterator.next().apply(service, builders);
		}
		return builders;
	}
	
	/**
	 * Returns the List of observed Attributes of the Resources based on this ResourceConfig,
	 * and for the service whose String name is passed as parameter 
	 * 
	 * @param service the String name of the service for which Attributes may be observed
	 * @return the List of observed Attributes of the Resources based on this ResourceConfig
	 * for the specified service
	 */
	public List<String> getObserveds(String service){
		return Collections.emptyList();
	}
}
