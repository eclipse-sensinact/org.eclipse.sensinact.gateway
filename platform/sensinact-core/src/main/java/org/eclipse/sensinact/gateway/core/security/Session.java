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
package org.eclipse.sensinact.gateway.core.security;

import java.util.Set;

import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Session
{	 
	/**
	 * Returns the String identifier of the current Session
	 * 
	 * @return this Session's String identifier
	 */
	String getId();
	
	/**
	 * Returns the set of  {@link ServiceProvider}s accessible
	 * for this session's user
	 * 
	 * @return the set of accessible {@link ServiceProvider}s
	 */
	Set<ServiceProvider> serviceProviders();
	
	/**
	 * Returns the set of {@link ServiceProvider}s accessible
	 * for this session's user
	 * 
	 * @param filter the String LDAP formated filter allowing
	 * to discriminate the appropriate {@link ServiceProvider}s
	 * 
	 * @return the set of accessible {@link ServiceProvider}s
	 * according to the specified LDAP filter 
	 */
	Set<ServiceProvider> serviceProviders(String filter);
    
    /**
     * @param serviceProviderName
     * @return
     */
    ServiceProvider serviceProvider(String serviceProviderName);

    /**
     * @param serviceProviderName
     * @param serviceName
     * @return
     */
    Service service(String serviceProviderName, String serviceName);

    /**
     * @param serviceProviderName
     * @param serviceName
     * @param resourceName
     * @return
     */
     Resource resource(String serviceProviderName, String serviceName,
    		 String resourceName);  

   	/**
   	 * @return
   	 */
   	JSONObject getAll();
   	
   	/**
   	 * @param filter
   	 * @return
   	 */
   	JSONObject getAll(String filter);
   	
   	/**
   	 * @return
   	 */
   	JSONObject getLocations();
 
     /**
     * @return
     */
    JSONObject getProviders();

     /**
     * @param serviceProviderId
     * @return
     */
    JSONObject getProvider(String serviceProviderId);

     /**
     * @param serviceProviderId
     * @return
     */
    JSONObject getServices(String serviceProviderId);

     /**
     * @param serviceProviderId
     * @param serviceId
     * @return
     */
    JSONObject getService(String serviceProviderId,String serviceId);

     /**
     * @param serviceProviderId
     * @param serviceId
     * @return
     */
    JSONObject getResources(String serviceProviderId, String serviceId);

     /**
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @return
     */
    JSONObject getResource(String serviceProviderId, 
     		String serviceId, String resourceId);
     
     /**
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param attributeId
     * @return
     */
    JSONObject get(String serviceProviderId, 
     		String serviceId, String resourceId, 
     		String attributeId);

     /**
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param attributeId
     * @param parameter
     * @return
     */
    JSONObject set(String serviceProviderId,
            String serviceId, String resourceId, 
            String attributeId, Object parameter);

     /**
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param parameters
     * @return
     */
    JSONObject act(String serviceProviderId,
             String serviceId, String resourceId, 
             Object[] parameters );
     
     /**
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param recipient
     * @param conditions
     * @return
     */
    JSONObject subscribe(String serviceProviderId,
             String serviceId, String resourceId, 
 	        Recipient recipient, JSONArray conditions);
        
     /**
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param subscriptionId
     * @return
     */
    JSONObject unsubscribe(String serviceProviderId,
             String serviceId, String resourceId, 
            String subscriptionId );
}
