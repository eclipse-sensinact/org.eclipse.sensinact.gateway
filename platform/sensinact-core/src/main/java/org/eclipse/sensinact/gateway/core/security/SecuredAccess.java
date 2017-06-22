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

import java.security.InvalidKeyException;

import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.tree.PathTree;

/**
 * A secured {@link Session}s provider service 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SecuredAccess
{
	/**
	 * Returns the {@link Session} instance associated
	 * to the anonymous user. The anonymous session can
	 * be used to register the service providers already
	 * known by the system
	 * 
	 * @return
	 * 		the {@link Session} for an anonymous user
	 */
	Session getAnonymousSession();

	/**
	 * Returns the {@link Session} instance associated
	 * for the authentication material provided by the
	 * {@link Authentication } passed as parameter.
	 * 
	 * @return
	 * 		the {@link Session} for the specified {@link 
	 * 		Authentication}
	 * 
	 * @throws DataStoreException 
	 * @throws InvalidKeyException 
	 */
	Session getSession(Authentication<?> authentication) 
		throws InvalidKeyException, DataStoreException;

	/**
	 * Validates the signature of the {@link Bundle} passed
	 * as parameter, and if valid returns its manifest file's 
	 * string SHA-1 signature ; otherwise returns null
	 * 
	 * @param bundle the {@link Bundle} to validate the signature
	 * of
	 * 
	 * @return the string SHA-1 signature of the specified {@link 
	 * Bundle}'s manifest if the {@link Bundle} is valid ; 
	 * null otherwise
	 */
	 String validate(Bundle bundle);

	/**
	 * Creates the {@link AccessNode}s hierarchy for the {@link 
	 * SensiNactResourceModel} whose holding bundle's identifier
	 * and name are passed as parameters, and attaches it
	 * to the parent {@link RootNode} also passed as parameter
	 * 
	 * @param identifier the String identifier of the 
	 * Bundle holding the specified {@link SensiNactResourceModel} 
	 * for which to create the {@link AccessNode}s hierarchy
	 * 
	 * @param name the name of the {@link SensiNactResourceModel}  
	 * for which to create the {@link AccessNode}s hierarchy
	 * 
	 * @param accessTree  the {@link AccessNode}s {@link PathTree} on 
	 * which to attach the {@link AccessNode} to be created
	 * 
	 * @throws SecuredAccessException 
	 */
	 void buildAccessNodesHierarchy(String identifier, String name, 
		AccessTree accessTree) throws SecuredAccessException;
	 
	 /**
	 * Returns the {@link AccessTree} for the Bundle whose identifier 
	 * is passed as parameter
	 * 
	 * @param identifier the String identifier of the 
	 * Bundle  for which to return the corresponding {@link AccessTree}
	 * 
	 * @return the {@link AccessTree} of {@link AccessNode}s for the 
	 * specified the Bundle
	 * 
	 * @throws SecuredAccessException 
	 */
	 AccessTree getAccessTree(String identifier) throws SecuredAccessException;
		 
	/**
	 * Registers the {@link SensiNactResourceModel} passed as parameter
	 * in the OSGi host environment
	 * 
	 * @param modelInstance the sensiNact resource model to be registered
	 * in the OSGi host environment
	 * 
	 * @param the {@link ServiceRegistration} for the specified {@link 
	 * SensiNactResourceModel}
	 * @throws SecuredAccessException 
	 */
	 ServiceRegistration<SensiNactResourceModel> register(
			 SensiNactResourceModel<?> modelInstance) 
			 throws SecuredAccessException;
	 
	/**
	 * Unregisters from the OSGi host environment the {@link SensiNactResourceModel} 
	 * whose {@link ServiceRegistration} is passed as parameter
	 * 
	 * @param registration the {@link SensiNactResourceModel}'s {@link 
	 * ServiceRegistration} to be unregistered
	 * 
	 * @throws SecuredAccessException 
	 */
	 void unregister(ServiceRegistration<SensiNactResourceModel> registration) 
			 throws SecuredAccessException;
	 
	 /**
	 * Registers a new {@link SnaAgent} build with the {@link 
	 * AbstractSnaAgentCallback} and the {@link SnaFilter} passed 
	 * as parameters in the OSGi host environment
	 * 
	 * @param callback the {@link AbstractSnaAgentCallback} in charge of
	 * handling the messages transmitted to the {@link SnaAgent} to be
	 * created and registered
	 * @param filter the {@link SnaFilter} helping in filtering the messages 
	 * transmitted to the {@link SnaAgent} to be created and registered
	 *  
	 * @param the String identifier of the registered {@link SnaAgent}
	 */
	String registerAgent(Mediator mediator, SnaAgentCallback callback, 
			SnaFilter filter);
	
	/**
	 * Unregisters from the OSGi host environment the {@link SnaAgent} 
	 * whose String identifier is passed as parameter
	 * 
	 * @param identifier the String identifier of the {@link 
	 * SnaAgent} to be unregistered
	 */
	void unregisterAgent(String identifier);
		
	/**
	 * Updates the properties of the registered {@link SensiNactResourceModel}
	 * passed as parameters
	 * 
	 * @param modelInstance the {@link SensiNactResourceModel} to 
	 * update the properties of
	 * 
	 * @param registration the {@link SensiNactResourceModel}'s {@link 
	 * ServiceRegistration} to be updated
	 * 
	 * @throws SecuredAccessException 
	 */
	 void update(SensiNactResourceModel<?> modelInstance, 
		ServiceRegistration<SensiNactResourceModel> registration) 
			 throws SecuredAccessException;

	/**
	 * Creates and registers an {@link AuthorizationService} that will
	 * allow to recover the {@link AccessLevel} of a connected user
	 *  
	 * @throws SecuredAccessException 
	 */
	void createAuthorizationService() throws SecuredAccessException;

	/**
	 * Closes this SecuredAccess service and frees all associated
	 * resources
	 */
	void close();
}
