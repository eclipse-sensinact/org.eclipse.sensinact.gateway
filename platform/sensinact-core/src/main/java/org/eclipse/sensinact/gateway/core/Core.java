package org.eclipse.sensinact.gateway.core;

import java.security.InvalidKeyException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;

/**
 * Core service of the sensiNact platform
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Core
{
	/**
	 * Namespace property key
	 */
	public static final String NAMESPACE_PROP = "org.eclipse.sensinact.gateway.namespace";
	
	/**
	 * Returns this Core's String namespace. The namespace will be used to 
	 * prefix the identifiers of the service providers that belongs to this 
	 * Core
	 * 
	 * @return this Core's String namespace
	 */
	String namespace();
	
	/**
	 * Instantiates a new {@link RemoteCore} that will be connected to a 
	 * remote instance of the sensiNact gateway by the way of the 
	 * extended {@link AbstractRemoteEndpoint} passed as parameter
	 * 
	 * @param remoteEndpoint the extended {@link AbstractRemoteEndpoint} 
	 * to connect to the {@link RemoteCore} to be instantiated
	 */
	void createRemoteCore(AbstractRemoteEndpoint remoteEnpoint);
	
	/**
	 * Instantiates a new {@link SnaAgent} build with the {@link 
	 * AbstractSnaAgentCallback} and the {@link SnaFilter} passed as parameters 
	 * in the OSGi host environment
	 * 
	 * @param mediator the {@link Mediator} provided by the bundle to which 
	 * the {@link SnaAgent} to be instantiated belongs to.
	 * @param callback the {@link AbstractSnaAgentCallback} in charge of
	 * handling the messages transmitted to the {@link SnaAgent} to be
	 * instantiated
	 * @param filter the {@link SnaFilter} helping in filtering the messages 
	 * transmitted to the {@link SnaAgent} to be created and registered
	 *  
	 * @return the String identifier of the new {@link SnaAgent}
	 */
	String registerAgent(Mediator mediator, SnaAgentCallback callback, 
			SnaFilter filter);
	
	/**
	 * Unregisters the {@link SnaAgent} whose String identifier is passed as 
	 * parameter
	 * 
	 * @param identifier the String identifier of the {@link SnaAgent} to be 
	 * unregistered
	 */
	void unregisterAgent(String identifier);
	
	/**
	 * Creates and returns a {@link Session} for the application
	 * whose private String identifier is passed as parameter.
	 * 
	 * @param mediator the {@link Mediator} allowing to interact
	 * with the OSGi host environment
	 * @param privateKey the application's private String identifier
	 * 
	 * @return the {@link Session} for the specified application
	 */
	Session getApplicationSession(Mediator mediator, String  privateKey);
	
	/**
	 * Returns the {@link Session} for the anonymous user
	 * 
	 * @return the anonymous user's {@link Session}
	 */
	Session getAnonymousSession();

	/**
	 * Returns the {@link Session} whose String identifier is passed 
	 * as parameter
	 * 
	 * @param token the String identifier of the {@link Session}
	 * 
	 * @return the {@link Session} with the specified identifier
	 */
	Session getSession(String token);

	/**
	 * Creates and returns a {@link Session} for the user whose 
	 * {@link Authentication} instance is passed as parameter 
	 * 
	 * @param authenticaton a user's {@link Authentication}
	 * 
	 * @return the {@link Session} for the specified user
	 */
	Session getSession(Authentication<?> authentication)
	    throws InvalidKeyException, DataStoreException;

	/**
	 * Closes this Core
	 */
	void close();		
}
