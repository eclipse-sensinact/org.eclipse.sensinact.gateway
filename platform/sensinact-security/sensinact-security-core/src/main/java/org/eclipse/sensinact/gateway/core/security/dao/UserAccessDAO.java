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
import org.eclipse.sensinact.gateway.core.security.entity.UserAccessEntity;

/**
 * Method DAO 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UserAccessDAO extends AbstractImmutableSnaDAO<UserAccessEntity>
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
	 */
    UserAccessDAO(Mediator mediator)
    {
	    super(mediator, UserAccessEntity.class);
    }
    
    /**
     * Returns the {@link UserAccessEntity} from the datastore 
     * matching the given String publicKey, otherwise null.
     * 
     * @param identifier the long identifier of the {@link UserAccessEntity} 
     * 		to be returned.
     * @return 
     * 		the {@link UserAccessEntity} from the datastore matching 
     * 		the given long identifier, otherwise null.
     * @throws DAOException 
     */
    public UserAccessEntity find(final long identifier) 
    		throws DAOException 
    {
    	List<UserAccessEntity> userAccessEntities = 
            super.select(new HashMap<String,Object>(){{
            	this.put("UAID", identifier);}});                	
    	if(userAccessEntities.size() != 1)
    	{
    		return null;
    	}
    	return userAccessEntities.get(0);
    }
    
    /**
     * Returns the {@link UserAccessEntity} from the datastore 
     * matching the given String name, otherwise null.
     * 
     * @param name the String name of the {@link UserAccessEntity} 
     * 		to be returned.
     * @return 
     * 		the {@link UserAccessEntity} from the datastore matching 
     * 		the given String name, otherwise null.
     * @throws DAOException 
     */
    public UserAccessEntity find(final String name) 
    		throws DAOException 
    {
    	List<UserAccessEntity> userAccessEntities = 
            super.select(new HashMap<String,Object>(){{
                this.put("UANAME", name);}});                    	
    	if(userAccessEntities.size() != 1)
    	{
    		return null;
    	}
    	return userAccessEntities.get(0);
    }
    
    
    /**
     * Returns the {@link UserAccessEntity} from the datastore 
     * matching the given integer access level, otherwise null.
     * 
     * @param level the integer access level of the {@link UserAccessEntity} 
     * 		to be returned.
     * @return 
     * 		the {@link UserAccessEntity} from the datastore matching 
     * 		the given integer access level, otherwise null.
     */
    public UserAccessEntity find(final int level) 
    		throws DAOException 
    {
    	List<UserAccessEntity> userAccessEntities = 
                super.select(new HashMap<String,Object>(){{
                    this.put("UALEVEL", level);}});                        	
    	if(userAccessEntities.size() != 1)
    	{
    		return null;
    	}
    	return userAccessEntities.get(0);
    }
}
