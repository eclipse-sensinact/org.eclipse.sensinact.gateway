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
import org.eclipse.sensinact.gateway.core.security.entity.AgentAccessEntity;
import org.eclipse.sensinact.gateway.core.security.entity.AgentEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileEntity;

/**
 * Authenticated DAO 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AgentAccessDAO extends AbstractMutableSnaDAO<AgentAccessEntity>
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

	private ObjectDAO objectDAO = null;
	private AgentDAO agentDAO = null;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 * 
	 * @throws DAOException 
	 */
    public AgentAccessDAO(Mediator mediator) throws DAOException
    {
	    super(mediator, AgentAccessEntity.class);
	    this.objectDAO = new ObjectDAO(mediator);
	    this.agentDAO = new AgentDAO(mediator);
    }
    
    /**
     * Returns the {@link AgentAccessEntity} from the datastore 
     * matching the given Long identifier, otherwise null.
     * 
     * @param path
     * 		The Long identifier specifying the primary key of 
     * 		the {@link AgentEntity} to be returned.
     * @param uid
     * 		The Long identifier specifying the primary key of 
     * 		the {@link AgentEntity} targeted by the {@link 
     * 		AgentAccessEntity} to be returned
     * @return 
     * 		the {@link AgentAccessEntity} from the datastore matching 
     * 		the given Long identifier, otherwise null.
     * 
     * @throws DAOException If something fails at datastore level.
     */
    public AgentAccessEntity find(String path, long uid) throws DAOException
    {
    	AgentAccessEntity entity = null;
    	AgentEntity user = this.agentDAO.find(uid);
    	if(user != null)
    	{
    		entity =  this.find(path, user.getPublicKey());
    	}
    	return entity;
    }
	  
    /**
     * Returns the {@link AgentAccessEntity} from the datastore 
     * matching the given Long identifier, otherwise null.
     * 
     * @param objectProfileEntityId
     * 		The Long identifier specifying the primary key of 
     * 		the {@link ObjectProfileEntity} to be returned.
     * @return 
     * 		the {@link ObjectProfileEntity} from the datastore matching 
     * 		the given Long identifier, otherwise null.
     * 
     * @throws DAOException If something fails at datastore level.
     */
    public AgentAccessEntity find(String path, final String publicKey) 
    		throws DAOException
    {     	
    	final ObjectEntity objectEntity = this.objectDAO.find(path);
    	
    	List<AgentAccessEntity> agentAccessEntities = 
    		super.select(new HashMap<String,Object>(){{
    		this.put("OID", objectEntity.getIdentifier());
    		this.put("APUBLIC_KEY",publicKey);}});
    	
    	if(agentAccessEntities.size() != 1)
    	{
    		return null;
    	}
    	return agentAccessEntities.get(0);
    }
}
