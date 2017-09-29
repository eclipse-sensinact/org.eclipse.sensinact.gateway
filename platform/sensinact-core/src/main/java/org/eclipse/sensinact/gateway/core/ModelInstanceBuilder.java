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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.AccessNode;
import org.eclipse.sensinact.gateway.core.security.AccessNodeImpl;
import org.eclipse.sensinact.gateway.core.security.AccessProfile;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTree;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessNode;
import org.eclipse.sensinact.gateway.core.security.ImmutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.MutableAccessNode;
import org.eclipse.sensinact.gateway.core.security.MutableAccessTree;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Allows to build in a simple way a {@link ModelInstance}
 * 
 * @param <C> the extended {@link ModelConfiguration}
 * type in use
 * @param <I> the extended {@link ModelInstance} type in use
 */
public class ModelInstanceBuilder
{
	protected Mediator mediator;
	protected Class<? extends ModelInstance<? extends ModelConfiguration>> resourceModelType;
	protected Class<? extends ModelConfiguration> resourceModelConfigurationType;
	
	protected Class<? extends ResourceImpl> resourceType;
	protected Class<? extends ServiceProviderImpl> serviceProviderType;
	protected Class<? extends ServiceImpl> serviceType;

	private Class<? extends Resource> defaultResourceType;	
	private Class<?> defaultDataType;
	private Modifiable defaultModifiable;
	private Resource.UpdatePolicy defaultUpdatePolicy;
	
	protected boolean startAtInitializationTime = false;
	
	//default behavior : refer to the xml description to instantiate all 
	//pre-defined resources
	protected byte resourceBuildPolicy =  (byte)(
			BuildPolicy.BUILD_APPEARING_ON_DESCRIPTION.getPolicy()|
			BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy()|
			BuildPolicy.BUILD_NON_DESCRIBED.getPolicy());

	//default behavior : instantiate services whose name is returned 
	//by the initial ServiceEnumeration request only 
	protected byte serviceBuildPolicy =  (byte)(
			BuildPolicy.BUILD_APPEARING_ON_DESCRIPTION.getPolicy()|
			BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy()|
			BuildPolicy.BUILD_NON_DESCRIBED.getPolicy());
		
	protected AccessProfile accessProfile;
	
	protected Map<String,AccessLevel> users;
	protected ModelConfiguration modelConfiguration;
	protected ResourceConfigBuilder defaultResourceConfigBuilder;
	
	/**
	 * @param resourceModelType
	 * @param name
	 */
	public ModelInstanceBuilder(Mediator mediator)
	{
		this(mediator, ModelInstance.class, ModelConfiguration.class);
	}
	
	/**
	 * @param resourceModelType
	 * @param name
	 */
	public <C extends ModelConfiguration,I extends ModelInstance<C>> 
	ModelInstanceBuilder(Mediator mediator, 
		Class<? extends I> resourceModelType, 
		Class<? extends C> resourceModelConfigurationType)
	{
		this.mediator = mediator;
		this.resourceModelType = resourceModelType;
		this.resourceModelConfigurationType = resourceModelConfigurationType;
		this.users = new HashMap<String,AccessLevel>();
	}
	
	/**
	 * Defines the extended {@link ServiceProviderImpl} type to use
	 * 
	 * @param serviceProviderType
	 * 		the extended {@link ServiceProviderImpl} type to use
	 * @return
	 * 		this SnaProcessorConfiguration
	 */
	public ModelInstanceBuilder withProviderImplementationType(
			Class<? extends ServiceProviderImpl> serviceProviderType)
	{
		this.serviceProviderType = serviceProviderType;
		return this;
	}
	
	/**
	 * Defines the extended {@link ServiceImpl} type to use
	 * 
	 * @param serviceType
	 * 		the extended {@link ServiceImpl} type to use
	 */
	public ModelInstanceBuilder withServiceImplementationType(
			Class<? extends ServiceImpl> serviceType)
	{
		this.serviceType = serviceType;
		return this;
	}

	/**
	 * Defines the {@link AccessProfile} applying to the
	 * {@link SensiNactResourceModel} to be created. If the
	 * resource model is referenced in the datastore
	 * of the sensiNact framework holding it this {@link 
	 * AccessProfile} is useless
	 * 
	 * @param accessProfile
	 * 		the {@link AccessProfile} applying
	 */
	public ModelInstanceBuilder withAccessProfile(
			AccessProfile accessProfile)
	{
		this.accessProfile  = accessProfile;
		return this;
	}

	/**
	 * Specifies a specific {@link AccessLevel} for the user
	 * whose public key is passed as parameter
	 * 
	 * @param userPublicKey
	 * 		the user's public key
	 * @param accessLevel
	 * 		the {@link AccessLevel} for the specified
	 * 		user
	 */
	public ModelInstanceBuilder withUser(
			String userPublicKey, AccessLevel accessLevel)
	{
		this.users.put(userPublicKey, accessLevel);
		return this;
	}
	
	/**
	 * Defines the extended {@link ResourceImpl} type to use
	 * 
	 * @param resourceType
	 * 		the extended {@link ResourceImpl} type to use
	 */
	public ModelInstanceBuilder withResourceImplementationType(
			Class<? extends ResourceImpl> resourceType)
	{
		this.resourceType  = resourceType;
		return this;
	}

	/**
	 * Sets the default extended {@link Resource} interface to be
	 * used by the {@link ResourceConfigBuilder} of the {@link 
	 * ModelConfiguration} to be created and/or configured by
	 * this ModelInstanceBuilder 
	 * 
	 * @param defaultResourceType the default extended {@link 
	 * Resource} interface to be used
	 * 
	 * @return this ModelInstanceBuilder
	 */
	public ModelInstanceBuilder withDefaultResourceType(
			Class<? extends Resource> defaultResourceType)
	{
		this.defaultResourceType = defaultResourceType;
		return this;
	}

	/**
	 * Sets the default data Type to be used by the {@link 
	 * ResourceConfigBuilder} of the {@link ModelConfiguration} 
	 * to be created and/or configured by this ModelInstanceBuilder 
	 * 
	 * @param defaultDataType the default data Type to be used
	 * 
	 * @return this ModelInstanceBuilder
	 */
	public ModelInstanceBuilder withDefaultDataType(Class<?> defaultDataType)
	{
		this.defaultDataType = defaultDataType;
		return this;			
	}

	/**
	 * Sets the default {@link Modifiable} to be used by the 
	 * {@link ResourceConfigBuilder} of the {@link 
	 * ModelConfiguration} to be created and/or configured by
	 * this ModelInstanceBuilder 
	 * 
	 * @param defaultModifiable the default {@link Modifiable} 
	 * to be used
	 * 
	 * @return this ModelInstanceBuilder
	 */
	public ModelInstanceBuilder withDefaultModifiable(
			Modifiable defaultModifiable)
	{
		this.defaultModifiable = defaultModifiable;
		return this;		
	}

	/**
	 * Sets the default extended {@link Resource.UpdatePolicy} to be
	 * used by the {@link ResourceConfigBuilder} of the {@link 
	 * ModelConfiguration} to be created and/or configured by
	 * this ModelInstanceBuilder 
	 * 
	 * @param defaultUpdatePolicy the default {@link Resource.UpdatePolicy}
	 * to be used
	 * 
	 * @return this ModelInstanceBuilder
	 */
	public ModelInstanceBuilder withDefaultUpdatePolicy(
			Resource.UpdatePolicy defaultUpdatePolicy)
	{
		this.defaultUpdatePolicy = defaultUpdatePolicy;
		return this;			
	}
	
	/**
	 * Defines the build policy applying on service instantiation
	 * 
	 * @param buildPolicy the byte representing the {@link BuildPolicy}(s) 
	 * applying on service instantiation
	 *            
	 */
	public ModelInstanceBuilder withServiceBuildPolicy(byte buildPolicy) 
	{
		this.serviceBuildPolicy = buildPolicy;
		return this;
	}

	/**
	 * Defines the build policy applying on resource instantiation
	 * 
	 * @param buildPolicy the byte representing the {@link BuildPolicy}(s) 
	 * applying on resource instantiation
	 *            
	 */
	public ModelInstanceBuilder withResourceBuildPolicy(byte buildPolicy) 
	{
		this.resourceBuildPolicy = buildPolicy;
		return this;
	}

	/**
	 * Defines the default {@link ResourceConfigCatalog} providing the
	 *  available {@link ResourceConfig}s
	 * 
	 * @param resourceConfigCatalog the {@link ResourceConfigBuilder} to 
	 * be set
	 */
	public ModelInstanceBuilder withDefaultResourceConfigBuilder(
		ResourceConfigBuilder defaultResourceConfigBuilder) 
	{
		this.defaultResourceConfigBuilder = defaultResourceConfigBuilder;
		return this;
	}
	
    /**
   	 * Defines whether the resource model is build dynamically according
   	 * to the content of a parsed communication packet
     * 
     * @param buildDynamically
     * 		<ul>
     * 			<li>true if the resource model has to be build
     * 				dynamically according to the content
     * 				of a parsed communication packet</li>
     * 			<li>false otherwise</li>
     * 		</ul>
   	 */
     public ModelInstanceBuilder withStartAtInitializationTime(
       		boolean startAtInitializationTime)
     {
   	    this.startAtInitializationTime = startAtInitializationTime;
   	    return this;
     }

    /**
	 * Defines the {@link ModelConfiguration} which applies
     * on new created {@link ModelInstance}s
     * 
     * @param configuration
     *      the {@link ModelConfiguration} which applies
	 */
	public ModelInstanceBuilder withConfiguration(
			ModelConfiguration modelConfiguration)
	{
		this.modelConfiguration = modelConfiguration;
		return this;
	}
	
    /**
     * Configures the {@link ModelConfiguration} passed as parameter
     * 
     * @param configuration the {@link ModelConfiguration} to 
     * configure
     */
    protected  <C extends ModelConfiguration> void configure(C configuration)
    {
	   	 configuration.setServiceBuildPolicy(this.serviceBuildPolicy
	   		).setResourceBuildPolicy(this.resourceBuildPolicy
	   		).setStartAtInitializationTime(this.startAtInitializationTime);
	   	 
    	 if(this.resourceType != null)
    	 {
    		 configuration.setResourceImplementationType(this.resourceType);
    	 }
    	 if(this.serviceType != null)
    	 {
    		 configuration.setServiceImplmentationType(this.serviceType);
    	 }
    	 if(this.serviceProviderType != null)
    	 {
    		 configuration.setProviderImplementationType(this.serviceProviderType);
    	 }
    	 if(this.defaultResourceType!=null)
    	 {
    		 configuration.setDefaultResourceType(this.defaultResourceType);
    	 }
    	 if(this.defaultDataType!=null)
    	 {
    		 configuration.setDefaultDataType(this.defaultDataType);
    	 }
    	 if(this.defaultModifiable!=null)
    	 {
    		 configuration.setDefaultModifiable(this.defaultModifiable);
    	 }
    	 if(this.defaultUpdatePolicy!=null)
    	 {
    		 configuration.setDefaultUpdatePolicy(this.defaultUpdatePolicy);
    	 }
     }

    /**
     * Creates and returns the {@link RootNode} of the {@link AccessNodeImpl}s
     * hierarchy for the {@link SensiNactResourceModel}(s) to be built
     * by the intermediate of this builder
     * 
     * @return the {@link RootNode} of the {@link AccessNodeImpl}s hierarchy 
     * for the {@link SensiNactResourceModel}(s) to be built
     */
    protected AccessTree<?> buildAccessTree()
    {
    	return this.mediator.callService(
		SecuredAccess.class, new 
		Executable<SecuredAccess, AccessTree<?>>()
		{
			@Override
			public AccessTree<?> execute(
					SecuredAccess service)  throws Exception
			{
				AccessTree<? extends AccessNode> accessTree = null;
				
				String identifier = service.validate(
					ModelInstanceBuilder.this.mediator.getContext(
							).getBundle());

				if(identifier == null)
				{
					accessTree = new AccessTreeImpl(mediator
						).withAccessProfile(AccessProfileOption.DEFAULT);
					
				} else
				{
					accessTree =  service.getAccessTree(
							identifier);
				}
				return accessTree;
			}
		});
    }
    
    /**
     * Creates the {@link AccessNodeImpl} for the {@link SensiNactResourceModel}
     * to be built, and add it to the {@link RootNode} passed as parameter
     * 
     * @param root the {@link RootNode} to which attach the new created
     * {@link AccessNodeImpl}
     */
    protected void buildAccessNode(
    		final MutableAccessTree<? extends MutableAccessNode> accessTree,
    		final String name)
    {
    	final AccessProfile accessProfile = this.accessProfile;
    	
		this.mediator.callService(SecuredAccess.class, 
			new Executable<SecuredAccess, Void>()
		{
			@Override
			public Void execute(SecuredAccess service) 
			throws Exception
			{
				String identifier  = service.validate(
					ModelInstanceBuilder.this.mediator.getContext(
							).getBundle());

				if(identifier == null)
				{	
					accessTree.add(UriUtils.getUri(
						new String[]{name})).withAccessProfile(accessProfile==null
						?AccessProfileOption.ALL_ANONYMOUS.getAccessProfile()
								:accessProfile);

				} else
				{
					service.buildAccessNodesHierarchy(identifier , 
						name, accessTree);
				}	
				return null;
			}
		});
    }

	/**
	 * Creates and returns a {@link ModelConfiguration} 
	 * instance with the specified properties.
	 * 
	 * @return the new created {@link ModelConfiguration} 
	 */
	public <C extends ModelConfiguration>
	C buildConfiguration(Object...parameters)
	{	
		C configuration  =  null;
		AccessTree<?> accessTree = this.buildAccessTree();
		
		int parametersLength = (parameters==null?0:parameters.length);
		int offset = (this.defaultResourceConfigBuilder != null)?3:2;
		Object[] arguments =new Object[parametersLength+offset];
		if(parametersLength > 0)
		{
			System.arraycopy(parameters, 0, arguments, 
					offset, parametersLength);
		}
		arguments[0] = mediator;
		arguments[1] = accessTree;

	    if(this.defaultResourceConfigBuilder != null)
	   	{
	   		arguments[2] = defaultResourceConfigBuilder;
	   	}
		configuration =  ReflectUtils.<ModelConfiguration,C>getInstance(
			ModelConfiguration.class, (Class<C>)
			this.resourceModelConfigurationType, arguments);
		
		if(configuration != null)
		{
			this.configure(configuration);
			this.withConfiguration(configuration);
		}
		return configuration;
	}
	
	/**
	 * Creates and return a {@link SensiNactResourceModel} 
	 * instance with the specified properties. Optional arguments
	 * apply to the {@link SensiNactResourceModelConfiguration} 
	 * initialization
	 * 
	 * @return the new created {@link SensiNactResourceModel} 
	 */
	public <C extends ModelConfiguration,I extends ModelInstance<C>>
	I build(final String name, String profileId, Object...parameters)
	{	
		I instance = null;
		if(this.modelConfiguration == null)
		{
			this.buildConfiguration(parameters);
		}					
		if(this.modelConfiguration != null)
		{ 			
			boolean exists= AccessController.<Boolean>doPrivileged(
						new PrivilegedAction<Boolean>()
			{
				@Override
				public Boolean run()
				{				
					Collection<ServiceReference<SensiNactResourceModel>> 
					references = null;
					try
					{
						references = ModelInstanceBuilder.this.mediator.getContext(
						).getServiceReferences(SensiNactResourceModel.class, 
							new StringBuilder().append("(name=").append(name
									).append(")").toString());
					}
					catch (InvalidSyntaxException e)
					{
						ModelInstanceBuilder.this.mediator.error(e);
					}
					return (references!=null && references.size()>0);
				}
			});    	
			if(!exists)
			{
				this.buildAccessNode(this.modelConfiguration.getAccessTree(),
						name);
				
				instance = (I) ReflectUtils.<ModelInstance,I>getInstance(
					ModelInstance.class, (Class<I>) this.resourceModelType,
					this.mediator, this.modelConfiguration, name, 
					profileId);
	
				this.register(instance);				
			}else
			{
				mediator.error(
				"Unable to register the model instance '%s', it already exists", 
				 name);
			}
		}			
		return instance;
	}

	/**
	 * @param instance
	 */
	protected final <C extends ModelConfiguration,I extends ModelInstance<C>>
	void register(final I instance)
	{
		if(instance == null)
		{
			return;
		}
		instance.register();
	}
}