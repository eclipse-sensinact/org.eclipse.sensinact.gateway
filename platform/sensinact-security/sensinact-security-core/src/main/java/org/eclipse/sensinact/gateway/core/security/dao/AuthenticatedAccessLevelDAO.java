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
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.core.security.entity.AuthenticatedAccessLevelEntity;

/**
 * User Access Level DAO 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AuthenticatedAccessLevelDAO extends AbstractImmutableSnaDAO<AuthenticatedAccessLevelEntity>
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
	private ObjectDAO objectDAO;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 * @throws DAOException 
	 */
    public AuthenticatedAccessLevelDAO(Mediator mediator) throws DAOException
    {
	    super(mediator, AuthenticatedAccessLevelEntity.class);
	    this.objectDAO = new ObjectDAO(mediator);
    }

	/**
     * Returns the {@link AuthenticatedAccessLevelEntity} from the datastore 
     * matching the given object path and user public key.
     * 
     * @param objectIdentifier the object's long identifier for which 
     * to retrieve the access level
     * @param publicKey the user's public key for which to retrieve 
     * the access level
     * 
     * @return the {@link AuthenticatedAccessLevelEntity} for the specified
     *  object and user.
     *  
	 * @throws DAOException 
     */
    protected AuthenticatedAccessLevelEntity find(final long objectIdentifier, 
    		final String publicKey) throws DAOException 
    {
    	List<AuthenticatedAccessLevelEntity> userAccessLevelEntities = 
    		super.select(new HashMap<String,Object>(){{
    		this.put("UOID", objectIdentifier);
    		this.put("PUBLIC_KEY", publicKey);}});

    	if(userAccessLevelEntities.size() != 1)
    	{
    		return null;
    	}
    	return userAccessLevelEntities.get(0);
    }

	/**
     * Returns the {@link AuthenticatedAccessLevelEntity} from the datastore 
     * matching the given object path and user long identifier.
     * 
     * @param objectIdentifier the object's long identifier for which 
     * to retrieve the access level
     * @param userIdentifier the user's long identifier for which 
     * to retrieve the access level
     * 
     * @return the {@link AuthenticatedAccessLevelEntity} for the specified
     *  object and user.
     *  
	 * @throws DAOException 
     */
    protected AuthenticatedAccessLevelEntity find(final long objectIdentifier, 
    		final long userIdentifier) throws DAOException 
    {
    	List<AuthenticatedAccessLevelEntity> userAccessLevelEntities = 
    		super.select(new HashMap<String,Object>(){{
    		this.put("UOID", objectIdentifier);
    		this.put("UID",userIdentifier);}});
    	
    	if(userAccessLevelEntities.size() != 1)
    	{
    		return null;
    	}
    	return userAccessLevelEntities.get(0);
    }
    
	/**
     * Returns the {@link AuthenticatedAccessLevelEntity} from the datastore 
     * matching the given object path and user public key.
     * 
     * @param path the string path of the object for which to retrieve 
     * the access level
     * @param publicKey the user's public key for which to retrieve 
     * the access level
     * 
     * @return the {@link AuthenticatedAccessLevelEntity} for the specified
     *  object and user.
     *  
	 * @throws DAOException 
     */
    public AuthenticatedAccessLevelEntity find(String path, String publicKey) 
    		throws DAOException 
    {       	
    	
    	ObjectEntity objectEntity = null;
    	List<ObjectEntity> objectEntities = this.objectDAO.find(path); 
    	if(objectEntities.size()>0)
    	{
    		objectEntity = objectEntities.get(0) ;
    		//TODO: define what we should do if the path can be mapped to more
    		//than one ObjectEntity : should we crash ? should we select the more or
    		//the less restrictive AuthenticatedAccessLevelEntity ? 
    		//should we ignore it as it is the case for now ?
    		//Do we have to notify that the DB has not been properly configured
    	} else
    	{
    		throw new DAOException(String.format("Unknown element at '%s'",
    				path));
    	}    	
    	return this.find(objectEntity.getIdentifier(), publicKey);
    }
    
	/**
     * Returns the {@link AuthenticatedAccessLevelEntity} from the datastore 
     * matching the given object path and user identifier.
     * 
     * @param path the string path of the object for which to retrieve 
     * the access level
     * @param identifier the user's long identifier for which to retrieve 
     * the access level
     * 
     * @return the {@link AuthenticatedAccessLevelEntity} for the specified
     *  object and user.
     *  
	 * @throws DAOException 
     */
    public AuthenticatedAccessLevelEntity find(String path, long identifier) 
    		throws DAOException 
    {    
    	ObjectEntity objectEntity = null;
    	List<ObjectEntity> objectEntities = this.objectDAO.find(path); 
    	if(objectEntities.size()>0)
    	{
    		objectEntity = objectEntities.get(0) ;
    		//TODO: define what we should do if the path can be mapped to more
    		//than one ObjectEntity : should we crash ? should we select the more or
    		//the less restrictive AuthenticatedAccessLevelEntity ? 
    		//should we ignore it as it is the case for now ?
    		//Do we have to notify that the DB has not been properly configured
    	} else
    	{
    		throw new DAOException(String.format(
    			"Unknown element at '%s'", path));
    	}    	
    	return this.find(objectEntity.getIdentifier(), identifier);
    }
}
