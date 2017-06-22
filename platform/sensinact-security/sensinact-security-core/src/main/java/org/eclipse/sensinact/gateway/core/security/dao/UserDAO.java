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
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;

/**
 * User DAO 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UserDAO extends AbstractMutableSnaDAO<UserEntity>
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
		
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 * @throws DAOException 
	 */
    public UserDAO(Mediator mediator) throws DAOException
    {
	    super(mediator, UserEntity.class);
    }

    /**
     * Returns the {@link UserEntity} from the datastore 
     * matching the given Long identifier, otherwise null.
     * 
     * @param identifier
     * 		The Long identifier specifying the primary key of 
     * 		the {@link UserEntity} to be returned.
     * @return 
     * 		the {@link UserEntity} from the datastore matching 
     * 		the given Long identifier, otherwise null.
     * 
     * @throws DAOException If something fails at datastore level.
     */
    public UserEntity find( final long identifier ) 
    		throws DAOException 
    {
    	List<UserEntity> userEntities = 
            super.select(new HashMap<String,Object>(){{
            	this.put("SUID", identifier);}});   
    	
    	if(userEntities.size() != 1)
    	{
    		return null;
    	}
    	return userEntities.get(0);
    }
    
    /**
     * Returns the {@link UserEntity} from the datastore 
     * matching the given Long identifier, otherwise null.
     * 
     * @param login
     * 		The string login of the {@link UserEntity} to be returned.
     * @param password
     * 		The MD5 encoded string password of the {@link UserEntity} 
     * 		to be returned.
     * @return 
     * 		the {@link UserEntity} from the datastore matching 
     * 		the given login and password, otherwise null.
     * 
     * @throws DAOException If something fails at datastore level.
     */
    public UserEntity find( final String login, final String password ) 
    		throws DAOException 
    {
    	List<UserEntity> userEntities = 
            super.select(new HashMap<String,Object>(){{
            	this.put("SULOGIN", login);
            	this.put("SUPASSWORD", password);}});   
    	
    	if(userEntities.size() != 1)
    	{
    		return null;
    	}
    	return userEntities.get(0);
    }

    /**
     * Returns the {@link UserEntity} from the datastore 
     * matching the given String public key, otherwise null.
     * 
     * @param publicKey
     * 		The String public key of the {@link UserEntity} to 
     * 		be returned.
     * @return 
     * 		the {@link UserEntity} from the datastore matching 
     * 		the given Long identifier, otherwise null.
     * 
     * @throws DAOException If something fails at datastore level.
     */
	public UserEntity find( final String publicKey )
    		throws DAOException 
	{
    	List<UserEntity> userEntities = 
            super.select(new HashMap<String,Object>(){{
            	this.put("SUPUBLIC_KEY", publicKey);}});   
    	
    	if(userEntities.size() != 1)
    	{
    		return null;
    	}
    	return userEntities.get(0);
	}
}
