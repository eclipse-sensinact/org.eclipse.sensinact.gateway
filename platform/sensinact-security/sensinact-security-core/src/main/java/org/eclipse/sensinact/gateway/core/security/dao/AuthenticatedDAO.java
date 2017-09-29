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
import org.eclipse.sensinact.gateway.core.security.entity.AgentEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ApplicationEntity;
import org.eclipse.sensinact.gateway.core.security.entity.AuthenticatedEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileEntity;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;

/**
 * Authenticated DAO 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AuthenticatedDAO extends AbstractMutableSnaDAO<AuthenticatedEntity>
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
	private UserDAO userDAO = null;
	private AgentDAO agentDAO = null;
	private ApplicationDAO applicationDAO = null;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 * @throws DAOException 
	 */
    public AuthenticatedDAO(Mediator mediator) throws DAOException
    {
	    super(mediator, AuthenticatedEntity.class);
	    this.objectDAO = new ObjectDAO(mediator);
	    this.userDAO = new UserDAO(mediator);
	    this.agentDAO = new AgentDAO(mediator);
	    this.applicationDAO = new ApplicationDAO(mediator);
    }
    
    
    /**
     * Returns the {@link ObjectEntity} from the datastore
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
    public AuthenticatedEntity findFromUser(String path, long uid) 
    		throws DAOException
    {
    	AuthenticatedEntity entity = null;
    	UserEntity user = this.userDAO.find(uid);
    	if(user != null)
    	{
    		entity = this.find(path, user.getPublicKey());
    	}
    	return entity;
    }

    /**
     * Returns the {@link AuthenticatedEntity} from the datastore
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
    public AuthenticatedEntity findFromAgent(String path, long aid) 
    		throws DAOException
    {
    	AuthenticatedEntity entity = null;
    	AgentEntity agent = this.agentDAO.find(aid);
    	if(agent != null)
    	{
    		entity = this.find(path, agent.getPublicKey());
    	}
    	return entity;
    }

    /**
     * Returns the {@link AuthenticatedEntity} from the datastore
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
    public AuthenticatedEntity findFromApplication(String path, long appid) 
    		throws DAOException
    {
    	AuthenticatedEntity entity = null;
    	ApplicationEntity application = this.applicationDAO.find(appid);
    	if(application != null)
    	{
    		entity = this.find(path, application.getPublicKey());
    	}
    	return entity;
    }
    
    /**
     * Returns the {@link ObjectEntity} from the datastore 
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
    public AuthenticatedEntity find(String path, final String publicKey) 
    		throws DAOException
    {     	
    	final ObjectEntity objectEntity = this.objectDAO.find(path); 
    	
    	List<AuthenticatedEntity> authenticatedEntities = 
    		super.select(new HashMap<String,Object>(){{
    		this.put("OID", objectEntity.getIdentifier());
    		this.put("PUBLIC_KEY",publicKey);}});
    	
    	if(authenticatedEntities.size() != 1)
    	{
    		return null;
    	}
    	return authenticatedEntities.get(0);
    }
    
}
