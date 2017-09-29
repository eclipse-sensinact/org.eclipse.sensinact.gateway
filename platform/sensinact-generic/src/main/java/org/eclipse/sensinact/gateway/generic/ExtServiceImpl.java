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
package org.eclipse.sensinact.gateway.generic;

import java.util.List;

import org.eclipse.sensinact.gateway.generic.parser.ReferenceDefinition;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.InvalidServiceException;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceBuilder;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ResourceProxy;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.ServiceProxy;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodExecutor.ExecutionPolicy;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory;

/**
 * Extended abstract {@link ServiceImpl} implementation 
 * to reify a Service in the gateway
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ExtServiceImpl extends ServiceImpl
{       
    protected final String defaultResourceConfig;

    /**
     * Constructor
     * 
     * @param mediator
     * @param uri
     * @param defaultResourceConfig
     * 
     * @throws InvalidServiceException 
     */
    public ExtServiceImpl(ExtModelInstance<?> snaModelInstance, 
    		String name, ServiceProviderImpl serviceProvider) 
    				throws InvalidServiceException
    {
        this(snaModelInstance, name, serviceProvider,null); 
    }
    
    /**
     * Constructor
     * 
     * @param mediator
     * @param uri
     * @param defaultResourceConfig
     * 
     * @throws InvalidServiceException 
     */
    public ExtServiceImpl(ExtModelInstance<?> snaModelInstance,
    		String name, ServiceProviderImpl serviceProvider, 
    		String defaultResourceConfig) throws InvalidServiceException
    {
        super(snaModelInstance, name, serviceProvider);     
        this.defaultResourceConfig = defaultResourceConfig;
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
    	ExtResourceImpl resource = (ExtResourceImpl) super.addResource(builder);
    	if(resource != null)
    	{
    		ExtResourceConfig resourceConfig = (ExtResourceConfig) 
    				builder.getResourceConfig();
    		resource.buildMethod(resourceConfig, this);
    	}
		return resource;
	}
    
    /**
	 * Creates and registers the {@link AccessMethodTrigger}(s) described
	 * by the set of {@link ReferenceDefinition}s passed as parameter, and
	 * to associate to the {@link AccessMethod}'s  {@link Signature} also passed 
	 * as parameter

	 * @param resourceName
	 * 		the name of the {@link ActionResource} whose invocation triggers the
	 * 		{@link AccessMethodTrigger}(s) to create
	 * @param signature
	 * 		the {@link Signature} to which to link the {@link AccessMethodTrigger}(s)
	 * 		to instantiate
	 * @param references
	 * 		the list of {@link ReferenceDefinition} describing the 
	 * 		{@link AccessMethodTrigger}(s) to create
	 */
    protected void buildTriggers(String resourceName, 
    		Signature signature, List<ReferenceDefinition> references) 
    {    	
    	if(signature.getName().intern() != AccessMethod.ACT)
    	{
  	      super.modelInstance.mediator().debug("Action trigger allowed for ACT method only");
  	      return;
    	}
		int index = 0;
		int length = references==null?0:references.size();		
		AccessMethodTriggerFactory.Loader loader = AccessMethodTriggerFactory.LOADER.get();
		try
		{
			for(;index < length;index++)
			{
				JSONObject referenceJson = new JSONObject(references.get(index).getJSON());
				JSONObject triggerJson = referenceJson.getJSONObject(
						AccessMethodTrigger.TRIGGER_KEY);
				
				AccessMethodTriggerFactory  factory = loader.load(
					super.modelInstance.mediator(),
					triggerJson.getString(AccessMethodTrigger.TRIGGER_TYPE_KEY));
				
				super.addActionTrigger(resourceName, referenceJson.getString("reference"), 
				    signature, factory.newInstance(super.modelInstance.mediator(), 
						triggerJson), ExecutionPolicy.AFTER); 
			}
		}
        catch (Exception e)
        {
        	super.modelInstance.mediator().error(e);
	        
        } finally
        {
        	AccessMethodTriggerFactory.LOADER.remove();
        }
    }
}
