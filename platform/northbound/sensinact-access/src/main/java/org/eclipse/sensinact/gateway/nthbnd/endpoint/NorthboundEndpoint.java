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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import java.lang.reflect.Method;

import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.core.FilteringDefinition;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
//import org.eclipse.sensinact.gateway.core.ActionResource;
//import org.eclipse.sensinact.gateway.core.DataResource;
//import org.eclipse.sensinact.gateway.core.PropertyResource;
//import org.eclipse.sensinact.gateway.core.Resource;
//import org.eclipse.sensinact.gateway.core.Service;
//import org.eclipse.sensinact.gateway.core.ServiceProvider;
//import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
//import org.eclipse.sensinact.gateway.core.method.AccessMethod;
//import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
//import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
//import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.format.ResponseFormat;
//import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class NorthboundEndpoint
{		
	private Session session;
	private NorthboundMediator mediator;

	/**
	 * @param mediator
	 */
	public NorthboundEndpoint(NorthboundMediator mediator, Authentication<?> authentication)
			throws InvalidCredentialException
	{
		this.mediator = mediator;
		this.session = this.mediator.getSession(authentication);
	}	
	
	/**
	 * @return
	 */
	public String getSessionToken()
	{
		return this.session.getId();
	}
	
	/**
	 * Executes the {@link NorthboundRequest} passed as parameter 
	 * and returns the execution result in the <code>&lt;F&gt;</code>
	 * typed format 
	 * 
	 * @return
	 * 		the execution of this request in the appropriate
	 * 		format
	 */	
	public <F> F execute(NorthboundRequest request,
			ResponseFormat<F> responseFormat)
	{		
		Object result = null;
	
		Argument[] arguments = request.getExecutionArguments();
		Class<?>[] parameterTypes = Argument.getParameterTypes(
				arguments);		
		try
		{
			Method method = getClass().getDeclaredMethod(
			    request.getMethod(), parameterTypes);
			
			result = method.invoke(this, Argument.getParameters(
					arguments));
			
		} catch(Exception e)
		{
			e.printStackTrace();
			this.mediator.error(e);
		}
		return responseFormat.format(result);
	}
	
	/**
     * Registers an {@link SnaAgent} linked to the {@link Session} of 
     * this NorthboundEndpoint 
     * 
     * @return the response containing the information
     */
    public JSONObject registerAgent(AbstractMidAgentCallback callback, 
    		SnaFilter filter)
    {
    	return session.registerSessionAgent(callback, filter);
    }

	/**
     * Unregisters the {@link SnaAgent} linked to the 
     * current Session and whose String identifier is 
     * passed as parameter
     * 
     * @return the response containing the information
     */
    public JSONObject unregisterAgent(String agentId)
    {
    	return session.unregisterSessionAgent(agentId);
    }

	/**
     * Gets the all JSONObject formated list of service 
     * providers, services and resources, including their
     * location 
     * 
     * @return the JSONObject formated list of all the 
     * model instances' hierarchies
     */
    public JSONObject all()
    {
    	return this.all(null,null);
    }

	/**
     * Gets the all JSONObject formated list of service 
     * providers, services and resources, as well as 
     * their location, and compliant with the String 
     * filter passed as parameter
     * 
     * @return the JSONObject formated list of all the 
     * model instances' hierarchies according to the 
     * specified filter
     */
    public JSONObject all(String filter)
    {
    	return this.all(filter, null);
    }

	/**
     * Gets the all JSONObject formated list of service 
     * providers, services and resources, as well as 
     * their location, and compliant with the String 
     * filter passed as parameter
     * 
     * @return the JSONObject formated list of all the 
     * model instances' hierarchies according to the 
     * specified filter
     */
    public JSONObject all(String filter, FilteringDefinition 
    		filterDefinition)
    {
    	return session.getAll(filter, filterDefinition);
    }

//	/**
//     * Gets the all JSONObject formated list of service 
//     * providers with their location
//     * 
//     * @return the JSONObject formated list of service 
//     * providers and their location
//     */
//    public JSONObject locations()
//    {
//    	return session.getLocations();
//    }
    
   	/**
     * Get the list of service providers and returns it
     * 
     * @return the response containing the information
     */
    public JSONObject serviceProvidersList()
    {
   	    return this.serviceProvidersList(null);
    }
    
	/**
     * Get the list of service providers and returns it
     * 
     * @return the response containing the information
     */
    public JSONObject serviceProvidersList(FilteringDefinition 
    		filterDefinition)
    {
    	return session.getProviders(filterDefinition);
    }

    /**
     * Get the information of a specific service providers and returns it
     * 
     * @param serviceProviderId the service provider ID
     * @return the response containing the information
     */
    public JSONObject serviceProviderDescription(String serviceProviderId)
    {
    	return session.getProvider(serviceProviderId);
    }
    
    /**
     * Get the list of services of a service provider and returns it
     *
     * @param serviceProviderId the service provider ID
     * @return the response containing the information
     */
    public JSONObject servicesList(String serviceProviderId) 
    {
    	return this.servicesList(serviceProviderId,null);
    }

    /**
     * Get the list of services of a service provider and returns it
     *
     * @param serviceProviderId the service provider ID
     * @return the response containing the information
     */
    public JSONObject servicesList(String serviceProviderId, 
    		FilteringDefinition filterDefinition) 
    {
    	return session.getServices(serviceProviderId, 
    			filterDefinition);
    }
    
    /**
     * Get the information of a specific service and returns it
     *
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @return the response containing the information
     */
    public JSONObject serviceDescription(
    		String serviceProviderId, String serviceId)
    {
    	return session.getService(serviceProviderId, serviceId);
    }
    
    /**
     * Get the list of resources of a service and returns it
     *
     * @param serviceProviderId the service provider ID
     * @return the response containing the information
     */
    public JSONObject resourcesList(String serviceProviderId, 
    		String serviceId)
    {
    	return this.resourcesList(serviceProviderId, serviceId, null);
    }

    /**
     * Get the list of resources of a service and returns it
     *
     * @param serviceProviderId the service provider ID
     * @return the response containing the information
     */
    public JSONObject resourcesList(String serviceProviderId, 
    	String serviceId, FilteringDefinition filterDefinition) 
    {
    	return session.getResources(serviceProviderId, serviceId, 
    			filterDefinition);
    }
    
    /**
     * Get the information of a specific resource and returns it
     *
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @return the response containing the information
     */
    public JSONObject resourceDescription(
    		String serviceProviderId, String serviceId, 
    		String resourceId)
    {
    	return session.getResource(serviceProviderId, 
    			serviceId, resourceId);
    }
        

    /**
     * Perform a sNa GET on a resource
     *
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @param attributeId GET URL parameter with json format
     * @return the response containing the value of the resource
     */
    public JSONObject get(String serviceProviderId, 
    	String serviceId, String resourceId, String attributeId)
    {  	
    	return session.get(serviceProviderId, serviceId, 
    			resourceId, attributeId);
    }

    /**
     * Perform a sNa SET on a resource
     *
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @param attributeId the value to set
     * @return the response containing the value of the resource
     */
    public JSONObject set(String serviceProviderId,
       String serviceId, String resourceId, String attributeId,
              Object value) 
    {  	
    	return session.set(serviceProviderId, serviceId, resourceId, 
    			attributeId, value);
    }

    /**
     * Perform a sNa ACT on a resource
     * 
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @param arguments the parameters of the act (can be empty)
     * @return the response containing the value of the resource
     */
    public JSONObject act(String serviceProviderId,
          String serviceId, String resourceId, Object[] arguments)
    { 	
    	return session.act(serviceProviderId, serviceId, resourceId, arguments);
    }

    /**
     * Perform a subscription to a resource
     *
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @return the subscription ID
     */
    public JSONObject subscribe(String serviceProviderId, 
    	String serviceId, String resourceId, String attributeId, 
    	NorthboundRecipient recipient, JSONArray conditions) 
    {
     	return session.subscribe(serviceProviderId, serviceId, resourceId,
     			recipient, conditions);
    }

    /**
     * Perform an unsubscription to a resource
     *
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @param attributeId the value to unsubscribe
     * @return success or error response
     */
    public JSONObject unsubscribe(String serviceProviderId, 
    		String serviceId, String resourceId, String attributeId,
    		String subscriptionId) 
    {  	
     	return session.unsubscribe(serviceProviderId, serviceId, resourceId,
     			subscriptionId);
    }
}