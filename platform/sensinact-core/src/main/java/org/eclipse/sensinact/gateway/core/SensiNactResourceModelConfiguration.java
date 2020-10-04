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
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;

/**
 * Configuration of a sensiNact Resource Model instance
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SensiNactResourceModelConfiguration {
	/**
	 * Defines the policy applying on the build of resources according to their
	 * previously defined description
	 * 
	 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
	 */
	public enum BuildPolicy {
		// The existing description is used to build the resources
		// of a discovered service
		BUILD_ON_DESCRIPTION((byte) 4),
		// when a service is discovered
		// all associated described resources are created
		BUILD_COMPLETE_ON_DESCRIPTION((byte) 5),
		// when a service is discovered
		// only resources providing data and previously
		// described are created
		BUILD_APPEARING_ON_DESCRIPTION((byte) 6),
		// build non described resources using a dedicated policy
		BUILD_NON_DESCRIBED((byte) 8);

		private final byte policy;

		/**
		 * Constructor
		 * 
		 * @param policy
		 *            the byte representation of the policy to be instantiated
		 */
		private BuildPolicy(byte policy) {
			this.policy = policy;
		}

		/**
		 * Returns the byte representation of this BuildPolicy
		 * 
		 * @return this BuildPolicy byte representation
		 */
		public byte getPolicy() {
			return this.policy;
		}

		/**
		 * Returns the array of BuildPolicy
		 * 
		 * @param policy
		 * @return
		 */
		public static BuildPolicy[] valueOf(byte policy) {
			List<BuildPolicy> policies = new ArrayList<BuildPolicy>();
			BuildPolicy[] values = BuildPolicy.values();
			int index = 0;
			int length = values == null ? 0 : values.length;
			for (; index < length; index++) {
				if (isBuildPolicy(policy, values[index])) {
					policies.add(values[index]);
				}
			}
			return policies.toArray(new BuildPolicy[0]);
		}

		/**
		 * Returns true if the byte representation of the BuildPolicy passed as
		 * parameter includes the one specified; returns false otherwise
		 * 
		 * @param policy
		 *            the byte representation of the BuildPolicy
		 * @param buildPolicy
		 *            the BuildPolicy to check whether it is included in the byte
		 *            representation or not
		 * 
		 * @return true if the byte representation of the BuildPolicy includes the one
		 *         specified; false otherwise
		 */
		public static boolean isBuildPolicy(byte policy, BuildPolicy buildPolicy) {
			return (buildPolicy.getPolicy() & policy) == buildPolicy.getPolicy();
		}
	}

	/**
	 * Returns the extended {@link ServiceProviderImpl} type in use
	 * 
	 * @return the extended {@link ServiceProviderImpl} type in use
	 */
	Class<? extends ServiceProviderImpl> getProviderImplementationType();

	/**
	 * Returns the extended {@link ServiceImpl} type in use
	 * 
	 * @return the extended {@link ServiceImpl} type in use
	 */
	Class<? extends ServiceImpl> getServiceImplementationType();

	/**
	 * Returns the extended {@link ResourceImpl} type in use
	 * 
	 * @return the extended {@link ResourceImpl} type in use
	 */
	Class<? extends ResourceImpl> getResourceImplementationType();
	
	/**
	 * Returns the byte representing the {@link BuildPolicy}(s) that apply on non
	 * described resource instantiation
	 * 
	 * @return the byte representing the {@link BuildPolicy}(s) applying on
	 *         resources
	 */
	byte getResourceBuildPolicy();

	/**
	 * Returns the byte representing the {@link BuildPolicy}(s) that apply on non
	 * described service instantiation
	 * 
	 * @return the byte representing the {@link BuildPolicy}(s) applying on services
	 */
	byte getServiceBuildPolicy();

	/**
	 * Returns true if the root element of the SensiNactResourceModel configured by
	 * this SensiNactResourceModelConfiguration is started when created or not
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if the root element of the resource model is started
	 *         automatically when created</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean getStartAtInitializationTime();

	/**
	 * Defines the extended {@link ServiceProviderImpl} type to use
	 * 
	 * @param serviceProviderType the extended {@link ServiceProviderImpl} type to use
	 * @return this SensiNactResourceModelConfiguration
	 */
	SensiNactResourceModelConfiguration setProviderImplementationType(Class<? extends ServiceProviderImpl> serviceProviderType);

	/**
	 * Defines the extended {@link ServiceImpl} type to use
	 * 
	 * @param serviceType
	 *            the extended {@link ServiceImpl} type to use
	 */
	SensiNactResourceModelConfiguration setServiceImplmentationType(Class<? extends ServiceImpl> serviceType);

	/**
	 * Defines the extended {@link ResourceImpl} type to use
	 * 
	 * @param resourceType
	 *            the extended {@link ResourceImpl} type to use
	 */
	SensiNactResourceModelConfiguration setResourceImplementationType(Class<? extends ResourceImpl> resourceType);

	/**
	 * Set the default extended {@link Resource} interface to be used by the
	 * {@link ResourceConfigBuilder} attached to this SensiNactModelConfiguration
	 * 
	 * @param defaultResourceType
	 *            the default {@link Resource} interface to be used
	 * 
	 * @return this SensiNactResourceModelConfiguration
	 */
	SensiNactResourceModelConfiguration setDefaultResourceType(Class<? extends Resource> defaultResourceType);

	/**
	 * Set the default data Type to be used by the {@link ResourceConfigBuilder}
	 * attached to this SensiNactModelConfiguration
	 * 
	 * @param defaultDataType
	 *            the default data Type to be used
	 * 
	 * @return this SensiNactResourceModelConfiguration
	 */
	SensiNactResourceModelConfiguration setDefaultDataType(Class<?> defaultDataType);

	/**
	 * Set the default {@link Modifiable} to be used by the
	 * {@link ResourceConfigBuilder} attached to this SensiNactModelConfiguration
	 * 
	 * @param defaultModifiable
	 *            the default {@link Modifiable} to be used
	 * 
	 * @return this SensiNactResourceModelConfiguration
	 */
	SensiNactResourceModelConfiguration setDefaultModifiable(Modifiable defaultModifiable);

	/**
	 * Set the default {@link Resource.UpdatePolicy} to be used by the
	 * {@link ResourceConfigBuilder} attached to this SensiNactModelConfiguration
	 * 
	 * @param defaultUpdatePolicy
	 *            the default {@link Resource.UpdatePolicy} to be used
	 * 
	 * @return this SensiNactResourceModelConfiguration
	 */
	SensiNactResourceModelConfiguration setDefaultUpdatePolicy(Resource.UpdatePolicy defaultUpdatePolicy);

	/**
	 * Defines the byte representing the {@link BuildPolicy}(s) to be applied on non
	 * described resources instantiation
	 * 
	 * @param buildPolicy
	 *            the byte representing the {@link BuildPolicy}(s) applying on
	 *            resources
	 */
	SensiNactResourceModelConfiguration setResourceBuildPolicy(byte buildPolicy);

	/**
	 * Defines the byte representing the {@link BuildPolicy}(s) to be applied on non
	 * described services instantiation
	 * 
	 * @param buildPolicy
	 *            the byte representing the {@link BuildPolicy}(s) applying on
	 *            services
	 */
	SensiNactResourceModelConfiguration setServiceBuildPolicy(byte buildPolicy);

	/**
	 * Defines whether an newly instantiated resource is automatically started or
	 * not
	 * 
	 * @param startAtInitializationTime
	 *            <ul>
	 *            <li>true if the resource is automatically started</li>
	 *            <li>false otherwise</li>
	 *            </ul>
	 */
	SensiNactResourceModelConfiguration setStartAtInitializationTime(boolean startAtInitializationTime);

	/**
	 * Returns an array of Strig names of the accessible {@link AccessMethod.Type}
	 * for the {@link AccessLevel} of the {@link AccessLevelOption} passed as
	 * parameter and for the targeted resource whose path is also passed as
	 * parameter
	 * 
	 * @param path
	 *            the string path of the targeted resource
	 * @param accessLevelOption
	 *            the {@link AccessLevelOption}
	 * 
	 * @return the array of accessible {@link AccessMethod.Type} for the specified
	 *         access level option and resource
	 */
	List<MethodAccessibility> getAccessibleMethods(String path, AccessLevelOption accessLevelOption);

	/**
	 * Returns the {@link AccessLevelOption} for the agent, the application or the
	 * user whose public key is passed as parameter, and for the targeted resource
	 * whose path is also passed as parameter
	 * 
	 * @param path
	 *            the string path of the targeted resource
	 * @param publicKey
	 *            the agent, the application or the user's public string key
	 * 
	 * @return the {@link AccessLevelOption} for the specified the agent, the
	 *         application or the user, and resource
	 */
	AccessLevelOption getAuthenticatedAccessLevelOption(String path, String publicKey);

}
