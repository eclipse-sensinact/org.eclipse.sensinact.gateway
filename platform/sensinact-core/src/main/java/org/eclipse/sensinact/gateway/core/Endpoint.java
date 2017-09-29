package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An endpoint service represent one link in the communication chain 
 * between two instances of the sensiNact platform
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Endpoint 
{
  	/**
  	 * Returns the Json formated model instances hierarchy 
  	 * of the all 
  	 * 
  	 * @return
  	 */
  	JSONObject getAll(String publicKey);
  	
  	/**
  	 * Returns the Json formated model instances hierarchy 
  	 * of the all 
  	 * 
  	 * @return
  	 */
  	JSONObject getAll(String publicKey, String filter);

  	/**
  	 * @param publicKey
  	 * @return
  	 */
  	JSONObject getLocations(String publicKey);

    /**
     * @param publicKey
     * @return
     */
    JSONObject getProviders(String publicKey);


    /**
     * @param publicKey
     * @param serviceProviderId
     * @return
     */
    JSONObject getProvider(String publicKey, String serviceProviderId);

    /**
     * @param publicKey
     * @param serviceProviderId
     * @return
     */
    JSONObject getServices(String publicKey, String serviceProviderId);

    /**
     * @param publicKey
     * @param serviceProviderId
     * @param serviceId
     * @return
     */
    JSONObject getService(String publicKey, String serviceProviderId,String serviceId);

    /**
     * @param publicKey
     * @param serviceProviderId
     * @param serviceId
     * @return
     */
    JSONObject getResources(String publicKey, String serviceProviderId, String serviceId);

    /**
     * @param publicKey
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @return
     */
    JSONObject getResource(String publicKey, String serviceProviderId, 
    		String serviceId, String resourceId);

    /**
     * @param publicKey
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param attributeId
     * @return
     */
    JSONObject get(String publicKey, String serviceProviderId, 
    		String serviceId, String resourceId, 
    		String attributeId);
    
    /**
     * @param publicKey
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param attributeId
     * @param parameter
     * @return
     */
    JSONObject set(String publicKey, String serviceProviderId,
           String serviceId, String resourceId, 
           String attributeId, Object parameter);

    /**
     * @param publicKey
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param parameters
     * @return
     */
    JSONObject act(String publicKey, String serviceProviderId,
            String serviceId, String resourceId, 
            Object[] parameters );
    
    /**
     * @param publicKey
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param recipient
     * @param conditions
     * @return
     */
    JSONObject subscribe(String publicKey, String serviceProviderId,
            String serviceId, String resourceId, 
	        Recipient recipient, JSONArray conditions);
   
    /**
     * @param publicKey
     * @param serviceProviderId
     * @param serviceId
     * @param resourceId
     * @param subscriptionId
     * @return
     */
    JSONObject unsubscribe(String publicKey, String serviceProviderId,
            String serviceId, String resourceId, 
           String subscriptionId );
}
