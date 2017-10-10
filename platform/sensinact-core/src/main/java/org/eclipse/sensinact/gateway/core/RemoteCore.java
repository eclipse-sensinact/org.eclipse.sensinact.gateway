package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A RemoteCore represents a remote instance of sensiNact to which the local
 * one is connected to. It is linked to the local instance of sensiNact 
 * by the way of a {@link LocalEndpoint}, and it is linked to the remote 
 * instance of sensiNact using a {@link RemoteEndpoint}. The RemoteCore is 
 * in charge, between others, of maintaining the set of {@link Session}s 
 * instantiated because of the accesses to the local sensiNact instance 
 * from the remote connected one. It is also in charge of deleting them 
 * when their remote counterpart disappear
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface RemoteCore extends Endpoint
{		
	/**
	 * Returns the {@link RemoteEndpoint} allowing this
	 * RemoteCore to interact to the remote connected 
	 * sensiNact instance
	 * 
	 * @return this RemoteCore's {@link RemoteEndpoint}
	 */
	RemoteEndpoint endpoint();

	/**
	 * Opens the connection with the remote sensiNact 
	 * instance using the String namespace of the 
	 * local sensiNact instance
	 *  
	 * @param namespace the local sensiNact instance's
	 * String namespace
	 */
	void open(String namespace);
	
	/**
	 * Closes the connection with the remote sensiNact
	 * instance 
	 */
	void close();
	
	/**
	 * Returns the unique integer identifier of this 
	 * RemoteCore
	 * 
	 * @return this RemoteCore's integer identifier
	 */
	int localID();
	
	/**
	 * Returns the String namespace of the local instance of 
	 * sensiNact this RemoteCore is connected to.
	 * 
	 * @return this local instance of sensiNact's String 
	 * namespace 
	 */
	String namespace();
	
	/**
	 * Relays a subscription called from the remote connected instance
	 * of sensiNact
	 * 
	 * @param publicKey the subscription invoker's String public key
	 * @param serviceProviderId the String identifier  of the service 
	 * provider holding the service providing the resource on which the 
	 * subscription applies 
	 * @param serviceId  the String identifier  of the service providing 
	 * the resource on which the subscription applies 
	 * @param resourceId the String identifier  of the resource on which 
	 * the subscription applies
	 * @param conditions the JSON array formated set of the constraints 
	 * applying on the subscription
	 * @return the JSON formated response of the subscription access 
	 * method  invocation
	 */
	JSONObject subscribe(String publicKey, String serviceProviderId,
            String serviceId, String resourceId, JSONArray conditions);
	
	/**
	 * Registers an {@link SnaAgent} in the local instance of sensiNact. 
	 * The {@link SnaAgent} links to a remote one (in the remote connected 
	 * sensiNact instance) to witch it will dispatch the {@link SnaMessage}s
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
	 * Relays the {@link SnaMessage} passed as parameter to the local
	 * {@link SnaAgent} whose identifier is also passed as parameter
	 * 
	 * @param agentId the String identifier of the {@link SnaAgent} to
	 * which relay the event message
	 * @param message the {@link SnaMessage} to be relayed
	 */
	void dispatch(String agentId, SnaMessage<?> message);

	/**
	 * Closes the {@link Session} of the user whose public key is  passed 
	 * as parameter, because of its remote counterpart disappearance. 
	 * 
	 * @param publicKey the String public key of the user
	 */
	void closeSession(String publicKey);
}
