package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.MessageRegisterer;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;

/**
 * Extended {@link Endpoint} in charge of realizing and maintaining the 
 * connection to another RemoteEndpoint provided by a remote instance of 
 * the sensiNact gateway to be connected to
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface RemoteEndpoint extends Endpoint, Recipient, MessageRegisterer
{
	/**
	 * Connects this RemoteEndpoint to one provided by a remote
	 * sensiNact instance 
	 */
	void connect();

	/**
	 * Disconnects this RemoteEndpoint from the one provided by the remote
	 * sensiNact instance it is connected to
	 */
	void disconnect();
	
	/**
	 * Registers the {@link Executable} to be triggered when this 
	 * RemoteEndpoint is connected 
	 *  
	 * @param onConnectedCallback the {@link Executable} to be registered
	 */
	void onConnected(Executable<String, Void> onConnectedCallback);

	/**
	 * Registers the {@link Executable} to be triggered when this 
	 * RemoteEndpoint is disconnected 
	 *  
	 * @param onDisconnectedCallback the {@link Executable} to be registered
	 */
	void onDisconnected(Executable<String, Void> onDisconnectedCallback);
    
	/**
	 * Returns the namespace of the remotely connected sensiNact
	 * instance
	 * 
	 * @return the namespace of the remotely connected sensiNact
	 */
	String namespace();  
	
	/**
	 * @param identifier
	 * @param filter
	 * @param agentKey
	 */
	void registerAgent(String identifier, SnaFilter filter, String agentKey);

	/**
	 * @param identifier
	 */
	void unregisterAgent(String identifier);

	/**
	 * @param agentId
	 * @param message
	 */
	void dispatch(String agentId, SnaMessage<?> message);
}
