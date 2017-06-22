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
package org.eclipse.sensinact.gateway.core.security.dao;

import java.util.HashMap;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.entity.AgentAccessLevelEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;

/**
 * Agent Access DAO 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AgentAccessLevelDAO extends AbstractImmutableSnaDAO<AgentAccessLevelEntity>
{
	//********************************************************************//
	//						NESTED DECLARATIONS	     					  //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS  						  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	ObjectDAO objectDAO = null;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 * @throws DAOException 
	 */
    public AgentAccessLevelDAO(Mediator mediator) throws DAOException
    {
	    super(mediator, AgentAccessLevelEntity.class);
	    this.objectDAO = new ObjectDAO(mediator);
    }

	/**
     * Returns the {@link AgentAccessLevelEntity} from the datastore 
     * matching the given object path and agent public key.
     * 
     * @param objectIdentifier the object's long identifier for which 
     * to retrieve the access level
     * @param publicKey the agent's public key for which to retrieve 
     * the access level
     * 
     * @return the {@link AgentAccessLevelEntity} for the specified
     *  object and agent.
     *  
	 * @throws DAOException 
     */
    protected AgentAccessLevelEntity find(final long objectIdentifier, 
    		final String publicKey) throws DAOException 
    {
    	List<AgentAccessLevelEntity> agentAccessLevelEntities = 
    		super.select(new HashMap<String,Object>(){{
    		this.put("OID", objectIdentifier);
    		this.put("APUBLIC_KEY",publicKey);}});
    	
    	if(agentAccessLevelEntities.size() != 1)
    	{
    		return null;
    	}
    	return agentAccessLevelEntities.get(0);
    }

	/**
     * Returns the {@link AgentAccessLevelEntity} from the datastore 
     * matching the given object path and agent public key.
     * 
     * @param objectIdentifier the object's long identifier for which 
     * to retrieve the access level
     * @param agentIdentifier the agent's long identifier for which 
     * to retrieve the access level
     * 
     * @return the {@link AgentAccessLevelEntity} for the specified
     *  object and agent.
     *  
	 * @throws DAOException 
     */
    protected AgentAccessLevelEntity find(final long objectIdentifier, 
    		final long agentIdentifier) throws DAOException 
    {
    	List<AgentAccessLevelEntity> agentAccessLevelEntities = 
    		super.select(new HashMap<String,Object>(){{
    		this.put("OID", objectIdentifier);
    		this.put("AID",agentIdentifier);}});
    	
    	if(agentAccessLevelEntities.size() != 1)
    	{
    		return null;
    	}
    	return agentAccessLevelEntities.get(0);
    }
    
	/**
     * Returns the {@link AgentAccessLevelEntity} from the datastore 
     * matching the given object path and agent public key.
     * 
     * @param path the string path of the object for which to retrieve 
     * the access level
     * @param publicKey the agent's public key for which to retrieve 
     * the access level
     * 
     * @return the {@link AgentAccessLevelEntity} for the specified
     *  object and agent.
     *  
	 * @throws DAOException 
     */
    public AgentAccessLevelEntity find(String path, String publicKey) 
    		throws DAOException 
    {    
    	ObjectEntity objectEntity = this.objectDAO.find(path);
    	return this.find(objectEntity.getIdentifier(), publicKey);
    }
    
	/**
     * Returns the {@link AgentAccessLevelEntity} from the datastore 
     * matching the given object path and agent identifier.
     * 
     * @param path the string path of the object for which to retrieve 
     * the access level
     * @param identifier the agent's long identifier for which to retrieve 
     * the access level
     * 
     * @return the {@link AgentAccessLevelEntity} for the specified
     *  object and agent.
     *  
	 * @throws DAOException 
     */
    public AgentAccessLevelEntity find(String path, long identifier) 
    		throws DAOException 
    {    
    	final ObjectEntity objectEntity = this.objectDAO.find(path); 
    	return this.find(objectEntity.getIdentifier(), identifier);
    }
}
