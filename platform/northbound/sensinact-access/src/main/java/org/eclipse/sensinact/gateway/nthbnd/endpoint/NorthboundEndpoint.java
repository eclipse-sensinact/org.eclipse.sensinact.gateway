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
import java.util.Iterator;
//import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
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
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.format.ResponseFormat;
//import org.eclipse.sensinact.gateway.util.UriUtils;

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
	public NorthboundEndpoint(NorthboundMediator mediator,
			Authentication<?> authentication)
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
	public <F> F execute(NorthboundRequest<F> request, 
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

//    /**
//     * Get the information of a specific service providers and returns it
//     * 
//     * @param session the session of the current user
//     * @param serviceProviderId the service provider ID
//     * @return the response containing the information
//     */
//    public ServiceProvider serviceProvider(String serviceProviderId)
//    {
//        ServiceProvider serviceProvider = session.serviceProvider(
//        		serviceProviderId);
//        return serviceProvider;
//    }
//
//    /**
//     * 
//     * Get the information of a specific service and returns it
//     * @param session the session of the current user
//     * @param serviceProviderId the service provider ID
//     * @param serviceId the service ID
//     * @return the response containing the information
//     */
//    public Service service(String serviceProviderId, String serviceId)
//    {
//    	Service service = session.service(
//    			serviceProviderId, serviceId);
//		return service;
//    }
//
//    /**
//     * Get the information of a specific resource and returns it
//     * @param session the session of the current user
//     * @param serviceProviderId the service provider ID
//     * @param serviceId the service ID
//     * @param resourceId the resource ID
//     * @return the response containing the information
//     */
//    public Resource resource(String serviceProviderId, 
//    		String serviceId, String resourceId)
//    {
//        Resource resource = session.resource(serviceProviderId, 
//        		serviceId, resourceId);
//        return resource;
//    }
    
	/**
     * Get the list of service providers and returns it
     * 
     * @return the response containing the information
     */
    public JSONObject serviceProvidersList()
    {
    	return session.getProviders();
//        JSONArray serviceProvidersJson = new JSONArray();
//        Set<ServiceProvider> serviceProviders = session.serviceProviders();
//
//        for (ServiceProvider provider : serviceProviders) 
//        {
//            serviceProvidersJson.put(provider.getName());
//        }
//        JSONObject jsonDevice = new JSONObject();
//    	jsonDevice.put("type", "PROVIDERS_LIST");
//    	jsonDevice.put("uri", UriUtils.PATH_SEPARATOR);
//    	jsonDevice.put("statusCode", 200);
//    	jsonDevice.put("providers", serviceProvidersJson);
//    	
//        return jsonDevice;
    }

    /**
     * Get the information of a specific service providers and returns it
     * 
     * @param session the session of the current user
     * @param serviceProviderId the service provider ID
     * @return the response containing the information
     */
    public JSONObject serviceProviderDescription(String serviceProviderId)
    {
    	return session.getProvider(serviceProviderId);
    	
//        ServiceProvider serviceProvider = serviceProvider(
//        		serviceProviderId);
//
//    	JSONObject jsonServiceProvider = new JSONObject();
//    	jsonServiceProvider.put("type", "DESCRIBE_RESPONSE");
//    	jsonServiceProvider.put("uri", new StringBuilder().append(
//    			UriUtils.PATH_SEPARATOR).append(
//    				serviceProviderId).toString());
//    	
//        if (serviceProvider != null)
//        {
//        	jsonServiceProvider.put("statusCode", 200);
//        	jsonServiceProvider.put("response", new JSONObject(
//        			serviceProvider.getDescription().getJSON()));
//
//        } else
//        {
//        	jsonServiceProvider.put("statusCode", 404);
//        	jsonServiceProvider.put("message",  "sensiNact service provider '" + 
//        	serviceProviderId + "' not found");
//        }
//        return jsonServiceProvider;
    }
    
    /**
     * Get the list of services of a service provider and returns it
     * @param session the session of the current user
     * @param serviceProviderId the service provider ID
     * @return the response containing the information
     */
    public JSONObject servicesList(String serviceProviderId) 
    {
    	return session.getServices(serviceProviderId);
    	
//    	String uri = UriUtils.getUri(new String[]{ serviceProviderId });
//        ServiceProvider serviceProvider = serviceProvider(serviceProviderId);
//        
//        JSONObject jsonServiceProvider = new JSONObject();
//	        	jsonServiceProvider.put("type", "SERVICES_LIST");
//	        	jsonServiceProvider.put("uri", uri); 
//	        	
//        if(serviceProvider != null)
//        {
//        	JSONArray servicesArray = new JSONArray();
//	        List<Service> services = serviceProvider.getServices();
//	        
//	        int index = 0;
//	        int length = services==null?0:services.size();
//	        
//	        for(;index < length;index++)
//	        {
//	        	servicesArray.put(services.get(index).getName());
//	        }    	
//	        jsonServiceProvider.put("statusCode", 200);
//	        jsonServiceProvider.put("services", servicesArray);
//	        
//        } else
//        {
//        	jsonServiceProvider.put("statusCode", 404);
//        	registerError(jsonServiceProvider,"sensiNact service provider '" 
//        	+ uri + "' not found");
//        }
//        return jsonServiceProvider;
    }

    /**
     * Get the information of a specific service and returns it
     * @param session the session of the current user
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @return the response containing the information
     */
    public JSONObject serviceDescription(
    		String serviceProviderId, String serviceId)
    {
    	return session.getService(serviceProviderId, serviceId);
    	
//    	String uri = UriUtils.getUri(new String[]{
//    			serviceProviderId, serviceId });
//		Service service = service(
//				serviceProviderId, serviceId);
//		
//		JSONObject jsonService = new JSONObject();
//		jsonService.put("type", "DESCRIBE_RESPONSE");
//		jsonService.put("uri", uri); 
//
//        if (service != null) 
//        {
//        	jsonService.put("statusCode", 200);
//        	jsonService.put("response", new JSONObject(
//        			service.getDescription().getJSON()));
//        } else
//        {
//        	jsonService.put("statusCode", 404);
//        	registerError(jsonService,"sensiNact service '" 
//        	+ uri + "' not found");
//        }
//        return jsonService;
    }
    
    /**
     * Get the list of resources of a service and returns it
     * @param session the session of the current user
     * @param serviceProviderId the service provider ID
     * @return the response containing the information
     */
    public JSONObject resourcesList(String serviceProviderId, 
    		String serviceId)
    {
    	return session.getResources(serviceProviderId, serviceId);
    	
//    	String uri = UriUtils.getUri(new String[]{ 
//    			serviceProviderId, serviceId });
//        Service service = service(serviceProviderId, serviceId);
//        
//        JSONObject jsonService = new JSONObject();
//	        	jsonService.put("type", "RESOURCES_LIST");
//	        	jsonService.put("uri", uri); 
//	        	
//        if(service != null)
//        {
//        	JSONArray resourcesArray = new JSONArray();
//	        List<Resource> resources = service.getResources();
//	        
//	        int index = 0;
//	        int length = resources==null?0:resources.size();
//	        
//	        for(;index < length;index++)
//	        {
//	        	resourcesArray.put(resources.get(index).getName());
//	        }    	
//	        jsonService.put("statusCode", 200);
//	        jsonService.put("resources", resourcesArray);
//	        
//        } else
//        {
//        	jsonService.put("statusCode", 404);
//        	registerError(jsonService,"sensiNact service '" 
//        	+ uri + "' not found");
//        }
//        return jsonService;
    }

    /**
     * Get the information of a specific resource and returns it
     * @param session the session of the current user
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
    	
//        Resource resource = resource(serviceProviderId, 
//        		serviceId, resourceId);
//    	JSONObject jsonResource = new JSONObject();
//    	jsonResource.put("type", "DESCRIBE_RESPONSE");
//    	jsonResource.put("uri", UriUtils.getUri(new String[]{
//    			serviceProviderId, serviceId, resourceId }));
//    	
//        if (resource != null)
//        {      	
//        	jsonResource.put("statusCode", 200);
//        	jsonResource.put("response", new JSONObject(
//        		resource.getDescription().getDescription()));
//        } else
//        {
//        	jsonResource.put("statusCode", 404);
//        	registerError(jsonResource,"sensiNact resource '" 
//        	+ resourceId + "' not found");
//        }
//        return jsonResource;
    }
        
//    public void registerError(JSONObject jsonObject, String message)
//    {
//    	JSONArray errors = jsonObject.optJSONArray("errors");
//    	if(errors == null)
//    	{
//    		errors = new JSONArray();
//    		jsonObject.put("errors", errors);
//    	}
//    	JSONObject error = new JSONObject();
//    	error.put("message", message);
//    	errors.put(error);
//    }

    /**
     * Perform a sNa GET on a resource
     * @param session the session of the current user
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @param attributeId GET URL parameter with json format
     * @return the response containing the value of the resource
     */
    public /*AccessMethodResponse*/ JSONObject get(String serviceProviderId, 
    	String serviceId, String resourceId, String attributeId)
    {  	
    	return session.get(serviceProviderId, serviceId, 
    			resourceId, attributeId);
    	
//        Resource resource = this.resource(serviceProviderId, 
//        		serviceId, resourceId);
//
//        if(resource == null)
//        {
//            return AccessMethodResponse.error(mediator, UriUtils.getUri(
//            	new String[]{serviceProviderId, serviceId, resourceId}), 
//            	AccessMethod.Type.valueOf(AccessMethod.GET), 
//            	SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
//            		"sensinact Resource '" + resourceId + "' not found",
//            		null);
//        }
//        String attribute = attributeId==null
//        		?DataResource.VALUE:attributeId;
//        
//        return resource.get(attribute);
    }

    /**
     * Perform a sNa SET on a resource
     * @param session the session of the current user
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @param attributetId the value to set
     * @return the response containing the value of the resource
     */
    public /*AccessMethodResponse*/ JSONObject set(String serviceProviderId,
       String serviceId, String resourceId, String attributeId,
              Object value) 
    {  	
    	return session.set(serviceProviderId, serviceId, resourceId, 
    			attributeId, value);
    	
//        Resource resource = this.resource(serviceProviderId, 
//        		serviceId, resourceId);
//
//        if(resource == null)
//        {
//            return AccessMethodResponse.error(mediator, UriUtils.getUri(
//            	new String[]{serviceProviderId, serviceId, resourceId}), 
//            	AccessMethod.Type.valueOf(AccessMethod.SET), 
//            	SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
//            		"sensinact Resource '" + resourceId + "' not found",
//            		null);
//        } 
//        return ((PropertyResource) resource).set(attributeId, value);
    }

    /**
     * Perform a sNa ACT on a resource
     * 
     * @param session the session of the current user
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @param arguments the parameters of the act (can be empty)
     * @return the response containing the value of the resource
     */
    public /*AccessMethodResponse*/ JSONObject act(String serviceProviderId,
          String serviceId, String resourceId, Object[] arguments)
    { 	
    	return session.act(serviceProviderId, serviceId, resourceId, arguments);
    	
//        Resource resource = this.resource(serviceProviderId, 
//        		serviceId, resourceId);
//        
//        if(resource == null)
//        {
//            return AccessMethodResponse.error(mediator, UriUtils.getUri(
//            	new String[]{serviceProviderId, serviceId, resourceId}), 
//            	AccessMethod.Type.valueOf(AccessMethod.ACT), SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
//            		"sensinact Resource '" + resourceId + "' not found",
//            		null);
//        }
//        if(!(resource instanceof ActionResource))
//        {
//            return AccessMethodResponse.error(mediator, UriUtils.getUri(
//            	new String[]{serviceProviderId, serviceId, resourceId}), 
//            		AccessMethod.Type.valueOf(AccessMethod.ACT), SnaErrorfulMessage.BAD_REQUEST_ERROR_CODE,
//            	"Resource '" + resource.getPath() +  "' is not an ActionResource",
//            		null);
//        }
//        if (arguments != null && arguments.length > 0) 
//        {
//           return ((ActionResource) resource).act(arguments);            
//        }        
//        return ((ActionResource) resource).act();
    }

    /**
     * Perform a subscription to a resource
     * @param session the session of the user
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @return the subscription ID
     */
    public /*AccessMethodResponse*/ JSONObject subscribe(NorthboundRecipient recipient, 
    	String serviceProviderId, String serviceId, String resourceId, 
    	String attributeId) 
    {
    	JSONArray conditions = null;
    	Set<Constraint> constraints = recipient.getConstraints();
    	if(!constraints.isEmpty())
    	{
    		 conditions = new JSONArray();
    		 Iterator<Constraint> iterator = constraints.iterator();
    		 while(iterator.hasNext())
    		 {
    			 conditions.put(new JSONObject(iterator.next().getJSON()));
    		 }
    	}
     	return session.subscribe(serviceProviderId, serviceId, resourceId,
     			recipient, conditions);
     	
//        Resource resource = this.resource(serviceProviderId, 
//        		serviceId, resourceId);
//
//        if (resource == null) 
//        {
//            AccessMethodResponse errorResponse = AccessMethodResponse.error(
//                this.mediator,UriUtils.getUri(new String[]{serviceProviderId,
//                  serviceId,resourceId}), AccessMethod.Type.valueOf(
//                    AccessMethod.SUBSCRIBE),  SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
//                    "Cannot create the instance: resource doesn't exist",
//                       null);
//            
//            return errorResponse;
//        }
//        SubscribeResponse response = resource.subscribe(attributeId,
//                recipient, recipient.getConstraints());
//
//        return response;
    }

    /**
     * Perform an unsubscription to a resource
     * @param session the session of the user
     * @param serviceProviderId the service provider ID
     * @param serviceId the service ID
     * @param resourceId the resource ID
     * @param usid the subscription ID
     * @return success or error response
     */
    public /*AccessMethodResponse*/ JSONObject unsubscribe(String serviceProviderId, 
    		String serviceId, String resourceId, String attributeId,
    		String subscriptionId) 
    {  	

     	return session.unsubscribe(serviceProviderId, serviceId, resourceId,
     			subscriptionId);
     	
//        Resource resource = this.resource(serviceProviderId, 
//        		serviceId, resourceId);
//
//        if (resource == null) 
//        {
//            AccessMethodResponse errorResponse = AccessMethodResponse.error(
//               this.mediator, UriUtils.getUri(new String[]{serviceProviderId,
//                 serviceId,resourceId}),AccessMethod.Type.valueOf(
//                	AccessMethod.UNSUBSCRIBE), SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
//                    "Cannot create the instance: resource doesn't exist", 
//                      null);
//            
//            return errorResponse;
//        }
//        UnsubscribeResponse response = resource.unsubscribe(
//        		DataResource.VALUE, subscriptionId);
//        return response;
    }
}