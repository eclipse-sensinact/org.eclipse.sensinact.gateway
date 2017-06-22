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
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileEntity;

/**
 * Method DAO 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ObjectProfileDAO extends AbstractImmutableSnaDAO<ObjectProfileEntity>
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
	
	public static final String DEFAULT_OBJECT_PROFILE =  "DEFAULT";
	    
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
    ObjectProfileDAO(Mediator mediator)
    {
	    super(mediator, ObjectProfileEntity.class);
    }
    
    /**
     * Returns the {@link ObjectProfileEntity} from the datastore 
     * matching the given Long identifier, otherwise null.
     * 
     * @param identifier
     * 		The Long identifier specifying the primary key of 
     * 		the {@link ObjectProfileEntity} to be returned.
     * @return 
     * 		the {@link ObjectProfileEntity} from the datastore matching 
     * 		the given Long identifier, otherwise null.
     * 
     * @throws DAOException If something fails at datastore level.
     */
    public ObjectProfileEntity find(final long identifier) 
    		throws DAOException
    {
    	List<ObjectProfileEntity> objectProfileEntities = 
    		super.select(new HashMap<String,Object>(){{
    			this.put("OPID", identifier);}});
        	
    	if(objectProfileEntities.size() != 1)
    	{
    		return null;
    	}
    	return objectProfileEntities.get(0);
    }
    

    /**
     * Returns the {@link ObjectEntity} from the datastore
     * matching the given Long identifier, otherwise null.
     * 
     * @param objectProfileEntity
     * 		
     * @return 
     * 		the {@link ObjectEntity} from the datastore matching 
     * 		the given Long identifier, otherwise null.
     * 
     * @throws DAOException If something fails at datastore level.
     */
    public ObjectProfileEntity find(final String name) 
    		throws DAOException
    {
    	List<ObjectProfileEntity> objectProfileEntities = 
        	super.select(new HashMap<String,Object>(){{
        		this.put("OPNAME", name);}});
            	
    	if(objectProfileEntities.size() != 1)
    	{
    		return null;
    	}
    	return objectProfileEntities.get(0);
    }
}
