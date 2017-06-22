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

import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResult;
import org.eclipse.sensinact.gateway.core.method.ActMethod;
import org.eclipse.sensinact.gateway.core.method.InvalidTriggerException;
import org.eclipse.sensinact.gateway.core.method.LinkedActMethod;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * Service implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceImpl extends ModelElement<ModelInstance<?>,
ServiceProcessableData, ResourceImpl, Resource>
{	
	 /**
     * the number of subscriptions made on {@link Resource}s
     * which belong to this service
     */
    protected int subscriptionsCount;


	/**
	 * Constructor
	 * 
	 * @param modelInstance the {@link ModelInstance} of the service
	 * to be instantiated
	 * @param name the name of the service to be instantiated
	 * @param serviceProvider the {@link ServiceProviderImpl} parent
	 * of the service to be instantiated
	 * 
	 * @throws InvalidServiceException 
	 * 		if an error occurred while instantiating the service
	 */
	protected ServiceImpl(ModelInstance<?> modelInstance,
			String name, ServiceProviderImpl serviceProvider) 
			throws InvalidServiceException
	{	
		super(modelInstance,  serviceProvider, UriUtils.getUri(
			new String[]{serviceProvider.getPath(),name }));
		
		this.subscriptionsCount = 0;
	}

    /**
     * @inheritDoc
     *
     * @see ModelElement#
     * process(ProcessableData)
     */
    public void process(ServiceProcessableData data)
    {
    	if(data == null)
    	{
    		return;
    	}     	
    	String resourceId = data.getResourceId();
    	if(resourceId == null)
    	{
    		super.modelInstance.mediator().warn(
    				"Resource identifier not found");
    		return;
    	}
    	ResourceImpl resource = this.getResource(resourceId); 
    	if(resource == null)
    	{  	
    		ResourceDescriptor descriptor = super.getModelInstance(
    		).configuration().getResourceDescriptor(
    	    ).withServiceName(super.getName()
    		).withResourceName(resourceId);
    		
    		ResourceBuilder builder = super.getModelInstance().getResourceBuilder(
    			descriptor, this.modelInstance.configuration(
    					).getResourceBuildPolicy());
    		
    		if(builder != null)
    		{            
	        	try
	            {
	                resource = this.addResource(builder);
	            }
	            catch (Exception e)
	            {
	                super.modelInstance.mediator().error(e);
	            }
    		}
    	}   
        if(resource == null)
        {
            super.modelInstance.mediator().warn(
            	"Resource '%s' not found for '%s' service",
            	resourceId,super.getName());
            return;
        }
        resource.process(data);
    }
    
    /**
	 * Creates and returns a new {@link DataResource} associated
	 * to this ServiceImpl
	 * 
	 * @param resourceClass
	 *      extended {@link DataResource} type to instantiate
	 * @param name
	 *      the name of the {@link DataResource} to instantiate
	 * @param type
	 *      the type of the 'value' {@link Attribute} of the 
	 *      {@link DataResource} to instantiate
	 * @param value
	 *      the value of the 'value' {@link Attribute} of the 
	 *      {@link DataResource} to instantiate
	 * @param attributes
	 * 		extended set of attributes to add to the resource to 
	 * 		create
	 * @return 
	 * 		a new created {@link DataResource} instance
	 * 
	 * @throws InvalidResourceException
	 * 		if an error occurred while instantiating the new 
	 * 		{@link DataResource}
	 */
    public <D extends DataResource> ResourceImpl addDataResource(
    		Class<D> resourceClass, String name, Class<?> type, 
    		Object value) throws InvalidResourceException
	{	
   		ResourceDescriptor descriptor = super.getModelInstance(
   	    	).configuration().getResourceDescriptor(
   			).withServiceName(this.getName()
		    ).withResourceName(name
			).withResourceType(resourceClass
			).withDataType(type
			).withDataValue(value);
   		
		ResourceBuilder builder = super.getModelInstance().getResourceBuilder(
			descriptor, this.modelInstance.configuration().getResourceBuildPolicy());
		
		return this.addResource(builder);
	}

	/**
	 * Creates and adds a new {@link ActionResource} to this 
	 * service
	 * 
	 * @param name
	 *      the name of the {@link ActionResource} to instantiate
	 * @param resourceClass
	 *      extended {@link ActionResource} type to instantiate
	 * @param attributes
	 * 		extended set of attributes to add to the resource to 
	 * 		create
	 * @return 
	 * 		a new created {@link ActionResource} instance
	 * 
	 * @throws InvalidResourceException
	 */
	public  <D extends ActionResource> ResourceImpl addActionResource(
		String name, Class<D> resourceClass) throws InvalidResourceException
	{
		ResourceDescriptor descriptor = super.getModelInstance(
	    		).configuration().getResourceDescriptor(
	   		   	).withServiceName(this.getName()).withResourceName(name
				).withResourceType(resourceClass);	
		
		ResourceBuilder builder = super.getModelInstance().getResourceBuilder(
			descriptor, this.modelInstance.configuration().getResourceBuildPolicy());
		
		return this.addResource(builder);
	}
	
	/**
	 * Creates and adds a {@link LinkedResourceImpl} linked 
	 * to the {@link ResourceImpl} passed as parameter to 
	 * this service
	 * 
	 * @param resource
	 *       the {@link ResourceImpl} to which to link the 
	 *       {@link LinkedResourceImpl} to instantiate
	 * @param name
	 *        the name of the {@link LinkedResourceImpl} to 
	 *        instantiate
	 * @return 
	 * 		a new created {@link LinkedResourceImpl} instance
	 * 
	 * @throws InvalidResourceException
	 */
	public ResourceImpl addLinkedResource(
			String link, ResourceImpl targetedResource)
	        throws InvalidResourceException
	{
		if(targetedResource == null ||this.getResource(link)!=null
				|| LinkedResourceImpl.class.isAssignableFrom(
						targetedResource.getClass()))
		{
    		super.modelInstance.mediator().debug(
    			"Unable to create the linked resource : %s",
    			link);
			return null;
		}
		if(targetedResource.getType() == Resource.Type.ACTION)
		{
			return addLinkedActionResource(link, targetedResource, true);
		}
		ResourceBuilder builder = super.getModelInstance().createResourceBuilder(
				super.getModelInstance().configuration().getResourceDescriptor(
			   	).withResourceName(link).withResourceType(
				targetedResource.getResourceType()));
		
		ResourceImpl linkedResource = builder.buildLinkedResource(this, 
				targetedResource);

		if(this.addResource(linkedResource))
		{
			targetedResource.registerLink(linkedResource.getPath());
		}
		return linkedResource;		
	}
	
	/**
	 * Creates and adds a {@link LinkedResourceImpl} linked 
	 * to the {@link Resource.Type.ACTION} typed {@link ResourceImpl}
	 * passed as parameter to this service
	 * 
	 * @param resource
	 *       the {@link Resource.Type.ACTION} typed
	 *       {@link ResourceImpl} to which to link the 
	 *       {@link LinkedResourceImpl} to instantiate
	 * @param name
	 *        the name of the {@link LinkedResourceImpl} to 
	 *        instantiate       
	 * @param copyActMethod
	 * 		true if the {@link LinkedActMethod} of the created 
	 * 		{@link LinkedResourceImpl} is a simple 
	 * 		copy of the {@link ActMethod} of the targeted
	 * 		{@link ActionResource} ; if false an empty 
	 * 		{@link LinkedActMethod} (containing no signature)
	 * 		is registered into the created {@link LinkedResourceImpl} 
	 * @return 
	 * 		a new created {@link LinkedResourceImpl} instance
	 * 
	 * @throws InvalidResourceException
	 */
	public ResourceImpl addLinkedActionResource(
			String link, ResourceImpl targetedResource, 
			boolean copyActMethod) throws InvalidResourceException
	{
		if(targetedResource == null 
				|| targetedResource.getType()!= Resource.Type.ACTION
				|| this.getResource(link)!=null)
		{
    		super.modelInstance.mediator().debug(new StringBuilder().append(
    			"Unable to create the resource - Invalid Name :"
    				).append(link).toString());
			return null;
		}		
		ResourceBuilder builder = super.getModelInstance().createResourceBuilder(
				super.getModelInstance().configuration().getResourceDescriptor(
			   	).withResourceName(link).withResourceType(
				targetedResource.getResourceType()));
		
		ResourceImpl linkedResource = builder.buildLinkedActionResource(
				this, targetedResource, copyActMethod);
		
		if(this.addResource(linkedResource))
		{
			targetedResource.registerLink(linkedResource.getPath());
			return linkedResource;	
		}	
		return null;
	}
	
    /**
	 * Creates and returns a new {@link ResourceImpl} and 
	 * updates existing {@link ServiceProxy}s its {@link 
	 * ResourceProxy}
	 * 
	 * @param builder
	 *      the {@link ResourceBuilder} containing the 
	 *      definition of the resource to create
	 * @return 
	 * 		a new created {@link Resource} instance
	 * 
	 * @throws InvalidResourceException
	 * 		if an error occurred while instantiating the new 
	 * 		{@link Resource}
	 */
    public ResourceImpl addResource(ResourceBuilder builder) 
    		throws InvalidResourceException
	{		
    	String resourceName = builder.getConfiguredName();
    	ResourceImpl resource = null;
    	
    	if(resourceName == null || (resource = 
    			this.getResource(resourceName))!=null)
    	{
    		super.modelInstance.mediator().debug(
    			"The resource '%s' already exists",resourceName);
    		return null;
    	}
		if((resource = builder.build(super.modelInstance, this)) != null 
				&& this.addResource(resource))
		{
			return resource;
		}
		return null;
	}
    
	/**
	 * @param resource
	 * @return
	 */
	private boolean addResource(ResourceImpl resource)
	{
		if(resource == null || !super.addElement(resource))
		{
			 return false;
		}			
		if(super.modelInstance.isRegistered())
		{
			resource.start();
		}
		return true;
	}
	 
	/**
	 * @param resource
	 * @return
	 */
	public ResourceImpl removeResource(String resource)
	{
		return super.removeElement(resource);
	}

	/**
	 * Registers an {@link AccessMethodTrigger} and links its 
	 * execution to the call of an action resource to define 
	 * the value of a state variable resource
	 * 
	 * @param listen
	 * 		the name of the action resource
	 * @param target
	 * 		the name of the state variable resource to 
	 * 		link to the action resource
	 * @param signature
	 * 		the signature to which attach the specified
	 * 		{@link AccessMethodTrigger}
	 * @param trigger
	 * 		the {@link AccessMethodTrigger} to call when the 
	 * 		specified action resource is executed
	 * @throws InvalidValueException 
	 */
	public <P> void addActionTrigger(String listen, final String target, 
			Signature signature, final AccessMethodTrigger<P> trigger,
			AccessMethodExecutor.ExecutionPolicy policy) 
					throws InvalidValueException
	{
		if(signature.getName().intern()!= AccessMethod.Type.ACT.name().intern())
		{
			throw new InvalidTriggerException("ACT method expected");
		}
		ResourceImpl actuator = this.getResource(listen);
		final ResourceImpl variable = this.getResource(target);
		
		//ensures that the listened resource exists and is an action one
		//and that the targeted one exists and  is a variable
		if(actuator==null || !ActionResource.class.isAssignableFrom(
				actuator.getResourceType()) || 
		   variable==null || !StateVariableResource.class.isAssignableFrom(
				variable.getResourceType()))
		{
			throw new InvalidTriggerException(
				"An ActionResource and a StateVariableResource were expected");
		}
		final Attribute attribute = variable.getAttribute(variable.getDefault());
		if(Modifiable.FIXED.equals(attribute.getModifiable()))
		{
			throw new InvalidValueException("Trigger cannot modify the resource value");
		}
		attribute.lock();
		
		//registers the executor which calls the trigger
		actuator.registerExecutor(signature, new AccessMethodExecutor()
		{
			/** 
			 * @inheritDoc
			 * 
			 * @see Executable#execute(java.lang.Object)
			 */
			@SuppressWarnings("unchecked")
			@Override
			public Void execute(AccessMethodResult parameter)
					throws Exception 
			{ 
//					if(!parameter.hasError())
//					{
					Object result = null;
					switch(trigger.getParameters())
					{
						case EMPTY:
							result = trigger.execute((P)null);	
							break;
						case PARAMETERS:
							result = trigger.execute((P)parameter.getParameters());	
							break;
						case RESPONSE:
							result = trigger.execute((P)parameter.createSnaResponse());
							break;
						case INTERMEDIATE:
							result = trigger.execute((P)parameter);
							break;
						default:
							break;						
					}
					if(trigger.passOn())
					{
						ServiceImpl.this.passOn(
							AccessMethod.Type.SET, target,
							new Object[]{DataResource.VALUE, result});
					}						
					attribute.setValue(result);	
					parameter.push(new JSONObject(attribute.getDescription(
							).getJSON()));
//					}
				return null;
			}
		}, policy);
	}
	
	/**
	 * Returns an {@link Executable} whose execution returns the 
	 * value of the {@link ResourceImpl} of this ServiceImpl, 
	 * and whose name is passed as parameter. The value of the 
	 * targeted {@link ResourceImpl} is the one of its default
	 * {@link Attribute} if it has been defined
	 * 
	 * @param resourceName
	 * 		the name of the {@link ResourceImpl} for which
	 * 		to create a value extractor
	 * @return 
	 * 		an {@link Executable} value extractor for the specified
	 * 		{@link ResourceImpl}
	 */
	public Executable<Void,Object> getResourceValueExtractor(String resourceName) 
	{
		final Attribute attribute;
		String defaultAttributeName = null;
		ResourceImpl resource = this.getResource(resourceName);
		
		if(resource != null &&
				(defaultAttributeName = resource.getDefault())!=null)
		{
			attribute = resource.getAttribute(defaultAttributeName);
			
		} else
		{
			attribute = null;
		}
		return new Executable<Void,Object>()
		{
			@Override
            public Object execute(Void parameter) throws Exception
            {
				if(attribute != null)
				{
					return attribute.getValue();
				}
                return null;
            }
		};
	}
	
	/**
	 * Passes on the invocation of a method of a resource of 
	 * this service to its parent service provider
	 *  
	 * @param type
	 * 		the type of the invoked method
	 * @param resource
	 * 		the name of the targeted resource
	 * @param parameters
	 * 		the objects array parameterizing the call
	 * @return
	 * 		the JSON formated result object 
	 * @throws Exception 
	 */
	@Override
	protected <TASK> TASK passOn(AccessMethod.Type type, 
			String path, Object[] parameters) throws Exception 
	{
		this.subscriptionsCount+=(type==AccessMethod.Type.UNSUBSCRIBE)?-1:0;		
		TASK task = super.passOn(type, path, parameters);
		this.subscriptionsCount+=(type==AccessMethod.Type.SUBSCRIBE)?1:0;
		return task;
	}
	
	/**
	 * Returns the {@link ResourceImpl} provided by
	 * this ServiceImpl, whose name is passed as 
	 * parameter
	 * 
	 * @param resource
	 * 		the name of the {@link ResourceImpl}
	 * @return
	 * 		the {@link ResourceImpl} with the specified
	 * 		name
	 */
    public ResourceImpl getResource(String resource)
    {
	    return super.element(resource);
    }


    /**
	 * Returns the set of {@link ResourceImpl}s 
	 * held by this ServiceImpl
	 * 
	 * @return
	 * 		the set of this ServiceImpl's 
	 * 		{@link ResourceImpl}s
	 */
    public List<ResourceImpl> getResources()
    {
    	synchronized(super.elements)
    	{
    		return Collections.<ResourceImpl>unmodifiableList(
    				super.elements);
    	}
    }
    
    /**
     * Returns the number of subscriptions registered
     * on this service's resources
     * 
     * @return
     * 		the number of registered subscriptions for this
     * 		service
     */
    public int getSubscriptionsCount()
    {
    	return this.subscriptionsCount;
    }

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#getRegisteredEvent()
	 */
	@Override
	protected Lifecycle getRegisteredEvent() 
	{
		return Lifecycle.SERVICE_APPEARING;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#getUnregisteredEvent()
	 */
	@Override
	protected Lifecycle getUnregisteredEvent() 
	{
		return Lifecycle.SERVICE_DISAPPEARING;
	}

	@Override
	protected Class<?> getProxyType()
	{
		return Service.class;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#getElementProxy(AccessLevelOption, Nameable)
	 */
	@Override
	protected Resource getElementProxy(AccessLevelOption accessLevelOption,
	        ResourceImpl element) throws ModelElementProxyBuildException
	{
		if(element.isHidden())
		{
			return null;
		}
		return (Resource) element.getProxy(accessLevelOption);
	}

	/**
	 * @inheritDoc
	 *
	 * @see ModelElement#
	 * getProxy(AccessMethod.Type[], java.util.List, int)
	 */
	@Override
	public ModelElementProxy<Resource> getProxy(
		List<MethodAccessibility> methodAccessibilities,  
		List<Resource> proxies)
	{
		try 
		{
			return new ServiceProxy(super.modelInstance.mediator(), 
				this.getName(), this.getPath(), proxies, 
				methodAccessibilities, null);
			
		} catch (InvalidServiceException e) 
		{
			super.modelInstance.mediator().error(e.getMessage(), e);
		}
		return null;
	}
}
