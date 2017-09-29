package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Service allowing to make a call to an access method of a resource
 * belonging to a remote sensiNact 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface RemoteCore extends Endpoint
{		
	/**
	 * @return
	 */
	RemoteEndpoint endpoint();

	/**
	 * @param namespace
	 */
	void open(String namespace);
	
	/**
	 * 
	 */
	void close();
	
	/**
	 * @return
	 */
	int localID();
	
	/**
	 * @return
	 */
	String namespace();
	
	/**
	 * @param publicKey
	 * @param serviceProviderId
	 * @param serviceId
	 * @param resourceId
	 * @param conditions
	 * @return
	 */
	JSONObject subscribe(String publicKey, String serviceProviderId,
            String serviceId, String resourceId, JSONArray conditions);
	
	/**
	 * Registers an {@link SnaAgent} in the local instance of sensiNact 
	 * this RemoteCore is attached to. This {@link SnaAgent} will be
	 * connected to a remote one (in the remote connected sensiNact instance)
	 * to witch it will dispatch the messages provided by the local instance
	 * of sensiNact
	 * 
	 * @param identifier the String identifier of the remote related {@link 
	 * SnaAgent} 
	 * @param filter the {@link SnaFilter} applying on the remote related 
	 * {@link SnaAgent} 
	 * @param agentKey the public key of the remote related {@link SnaAgent} 
	 */
	void registerAgent(String identifier, SnaFilter filter, String agentKey);


	/**
	 * Unregisters the {@link SnaAgent} whose identifier is passed 
	 * as parameter
	 * 
	 * @param identifier the String identifier of the {@link 
	 * SnaAgent} to be unregistered
	 */	
	 void unregisterAgent(String identifier);

	/**
	 * @param agentId
	 * @param message
	 */
	void dispatch(String agentId, SnaMessage<?> message);
}
