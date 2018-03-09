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

import org.eclipse.sensinact.gateway.core.FilteringDefinition;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.ActResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.format.ResponseFormat;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A NorthboundEndpoint is a connection point to a sensiNact 
 * instance for an northbound access service
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class NorthboundEndpoint
{		
	private Session session;
	private NorthboundMediator mediator;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link NorthboundMediator} that will allow
	 * the NothboundEndpoint to be instantiated to interact with the
	 * OSGi host environment
	 * 
	 * @param authentication the {@link Authentication}  that will allow
	 * the NothboundEndpoint to be instantiated to build the appropriate
	 * {@link Session}
	 * 
	 * @throws InvalidCredentialException
	 */
	public NorthboundEndpoint(NorthboundMediator mediator, 
		Authentication<?> authentication) throws InvalidCredentialException
	{
		this.mediator = mediator;
		this.session = this.mediator.getSession(authentication);
		if(this.session == null)
		{
			throw new NullPointerException("null sensiNact session");
		}
	}
	
	/**
	 * Returns the String identifier of the {@link Session} of this
	 * NorthboundEndpoint
	 * 
	 * @return the String identifier of this NorthboundEndpoint's 
	 * {@link Session}
	 */
	public String getSessionToken()
	{
		return this.session.getSessionId();
	}
	
	/**
	 * Executes the {@link NorthboundRequest} passed as parameter 
	 * and returns the execution result in the <code>&lt;F&gt;</code>
	 * typed format 
	 * 
	 * @param request the {@link NorthboundRequest} to be executed
	 * @param responseFormat the {@link ResponseFormat} allowing to
	 * format the execution result Object inn the expected format
	 * 
	 * @return the execution result Object of this request in
	 * the appropriate format
	 */
	public AccessMethodResponse<?> execute(NorthboundRequest request)
	{		
		AccessMethodResponse<?> result = null;
	
		Argument[] arguments = request.getExecutionArguments();
		Class<?>[] parameterTypes = Argument.getParameterTypes(
				arguments);		
		try
		{
			Method method = getClass().getDeclaredMethod(
			    request.getMethod(), parameterTypes);
			
			result = (AccessMethodResponse<?>) method.invoke(
				this, Argument.getParameters(arguments));
			
		} catch(Exception e)
		{
			this.mediator.error(e);
		}
		return result;
	}
	
	/**
     * Registers an {@link SnaAgent} whose lifetime will be linked 
     * to the {@link Session} of this NorthboundEndpoint 
     * 
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param callback
     * @param filter
     * 
     * @return the {@link SnaAgent} registration response
     */
    public SubscribeResponse registerAgent(String requestIdentifier, 
    	AbstractMidAgentCallback callback, SnaFilter filter)
    {
    	return session.registerSessionAgent(requestIdentifier, 
    			callback, filter);
    }

	/**
     * Unregisters the {@link SnaAgent} whose String 
     * identifier is passed as parameter
     * 
     * @param requestIdentifier the String identifier of the 
     * request calling this method
     * @param agentId
     * 
     * @return the {@link SnaAgent} unregistration response
     */
    public UnsubscribeResponse unregisterAgent(String requestIdentifier, 
        	String agentId)
    {
    	return session.unregisterSessionAgent(requestIdentifier, 
    			agentId);
    }

	/**
     * Gets the all JSONObject formated list of service 
     * providers, services and resources, including their
     * location 
     * 
     * @param requestIdentifier the String identifier of 
     * the request calling this method
     * 
     * @return the JSONObject formated list of all the 
     * model instances' hierarchies
     */
    public DescribeResponse<String> all(String requestIdentifier)
    {
    	return this.all(requestIdentifier, null,null);
    }

	/**
     * Gets the all JSONObject formated list of service 
     * providers, services and resources, as well as 
     * their location, and compliant with the String 
     * filter passed as parameter
     * 
     * @param requestIdentifier the String identifier of 
     * the request calling this method
     * @param filter the LDAP formated String filter allowing 
     * to discriminate the targeted service providers, services, 
     * and/or resources
     * 
     * @return the JSONObject formated list of all the 
     * model instances' hierarchies according to the 
     * specified filter
     */
    public DescribeResponse<String> all(String requestIdentifier, 
    		String filter)
    {
    	return this.all(requestIdentifier, filter, null);
    }

	/**
     * Gets the all JSONObject formated list of service 
     * providers, services and resources, as well as 
     * their location, and compliant with the String 
     * filter passed as parameter
     * 
     * @param requestIdentifier the String identifier of the 
     * request calling this method
     * @param filter the LDAP formated String filter allowing 
     * to discriminate the targeted service providers, services, 
     * and/or resources
     * @param filterDefinition the {@link FilteringDefinition} 
     * specifying the filter to be applied on the result
     * 
     * @return the JSONObject formated list of all the model 
     * instances' hierarchies according to the specified filter
     */
    public DescribeResponse<String> all(String requestIdentifier, 
    		String filter, FilteringDefinition 
    		filterDefinition)
    {
    	return session.getAll(requestIdentifier, filter, 
    			filterDefinition);
    }

   	/**
     * Get the list of service providers and returns it
     * 
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @return
     */
    public DescribeResponse<String> serviceProvidersList(String requestIdentifier)
    {
   	    return this.serviceProvidersList(requestIdentifier,null);
    }
    
	/**
     * Get the list of service providers and returns it
     * 
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param filterDefinition the {@link FilteringDefinition} specifying 
     * the filter to be applied on the result
     * @return
     */
    public DescribeResponse<String> serviceProvidersList(String requestIdentifier,
    	FilteringDefinition filterDefinition)
    {
    	return session.getProviders(requestIdentifier, 
    			filterDefinition);
    }

    /**
     * Get the information of a specific service providers and returns it
     * 
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @return
     */
    public DescribeResponse<JSONObject> serviceProviderDescription(String requestIdentifier,
    		String serviceProviderId)
    {
    	return session.getProvider(requestIdentifier,
    			serviceProviderId);
    }
    
    /**
     * Get the list of services of a service provider and returns it
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @return
     */
    public DescribeResponse<String> servicesList(String requestIdentifier, String serviceProviderId) 
    {
    	return this.servicesList(requestIdentifier, serviceProviderId,null);
    }

    /**
     * Get the list of services of a service provider and returns it
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param filterDefinition the {@link FilteringDefinition} specifying 
     * the filter to be applied on the result
     * @return
     */
    public DescribeResponse<String> servicesList(String requestIdentifier, 
    	String serviceProviderId, FilteringDefinition filterDefinition) 
    {
    	return session.getServices(requestIdentifier, 
    		serviceProviderId, filterDefinition);
    }
    
    /**
     * Get the information of a specific service and returns it
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @return
     */
    public DescribeResponse<JSONObject> serviceDescription(String requestIdentifier,
    		String serviceProviderId, String serviceId)
    {
    	return session.getService(requestIdentifier, 
    		serviceProviderId, serviceId);
    }
    
    /**
     * Get the list of resources of a service and returns it
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @return
     */
    public DescribeResponse<String> resourcesList(String requestIdentifier, 
    	String serviceProviderId, String serviceId)
    {
    	return this.resourcesList(requestIdentifier, 
    		serviceProviderId, serviceId, null);
    }

    /**
     * Get the list of resources of a service and returns it
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @param filterDefinition the {@link FilteringDefinition} specifying 
     * the filter to be applied on the result
     * @return
     */
    public DescribeResponse<String> resourcesList(String requestIdentifier, 
    	String serviceProviderId, String serviceId, 
    	    FilteringDefinition filterDefinition) 
    {
    	return session.getResources(requestIdentifier, 
    		serviceProviderId, serviceId, filterDefinition);
    }
    
    /**
     * Get the information of a specific resource and returns it
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @param resourceId the String identifier of the resource
     * @return
     */
    public DescribeResponse<JSONObject> resourceDescription(String requestIdentifier,
    		String serviceProviderId, String serviceId, 
    		String resourceId)
    {
    	return session.getResource(requestIdentifier, 
    		serviceProviderId, serviceId, resourceId);
    }        

    /**
     * Perform a sNa GET on a resource
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @param resourceId the String identifier of the resource
     * @param attributeId the String identifier of the attribute
     * @return
     */
    public GetResponse get(String requestIdentifier, 
    	String serviceProviderId, String serviceId, 
    	    String resourceId, String attributeId)
    {  	
    	return session.get(requestIdentifier, serviceProviderId, 
    		serviceId, resourceId, attributeId);
    }

    /**
     * Perform a sNa SET on a resource
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @param resourceId the String identifier of the resource
     * @param attributeId the String identifier of the attribute
     * @param value
     * @return
     */
    public SetResponse set(String requestIdentifier, String serviceProviderId,
       String serviceId, String resourceId, String attributeId,
              Object value) 
    {  	
    	return session.set(requestIdentifier, serviceProviderId, 
    		serviceId, resourceId, attributeId, value);
    }

    /**
     * Perform a sNa ACT on a resource
     * 
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @param resourceId the String identifier of the resource
     * @param arguments
     * @return
     */
    public ActResponse act(String requestIdentifier, String serviceProviderId,
          String serviceId, String resourceId, Object[] arguments)
    { 	
    	return session.act(requestIdentifier, serviceProviderId, 
    		serviceId, resourceId, arguments);
    }

    /**
     * Perform a subscription to a resource
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @param resourceId the String identifier of the resource
     * @param attributeId the String identifier of the attribute
     * @param recipient
     * @param conditions
     * @return
     */
    public SubscribeResponse subscribe(String requestIdentifier, 
    	String serviceProviderId, String serviceId, 
    	String resourceId, String attributeId, 
    	NorthboundRecipient recipient, JSONArray conditions) 
    {
     	return session.subscribe(requestIdentifier, 
     		serviceProviderId, serviceId, resourceId,
     			recipient, conditions);
    }

    /**
     * Perform an unsubscription to a resource
     *
     * @param requestIdentifier the String identifier of the request 
     * calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId the String identifier of the service
     * @param resourceId the String identifier of the resource
     * @param attributeId the String identifier of the attribute
     * @param subscriptionId
     * @return
     */
    public UnsubscribeResponse unsubscribe(String requestIdentifier, 
    	String serviceProviderId, String serviceId, 
    	String resourceId, String attributeId, String subscriptionId) 
    {  	
     	return session.unsubscribe(requestIdentifier, 
     		serviceProviderId, serviceId, resourceId,
     			subscriptionId);
    }
}