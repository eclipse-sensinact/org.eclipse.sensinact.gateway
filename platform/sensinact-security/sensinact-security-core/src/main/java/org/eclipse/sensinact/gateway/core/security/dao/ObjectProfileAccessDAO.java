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
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileAccessEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileEntity;

/**
 * Method DAO 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ObjectProfileAccessDAO extends AbstractImmutableSnaDAO<ObjectProfileAccessEntity>
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

	private ObjectProfileDAO objectProfileDAO;
//	private ObjectAccessDAO objectAccessDAO;
//	private MethodDAO methodDAO;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 */
    public ObjectProfileAccessDAO(Mediator mediator)
    {
	    super(mediator, ObjectProfileAccessEntity.class);
	    this.objectProfileDAO = new ObjectProfileDAO(mediator);
//	    this.objectAccessDAO = new ObjectAccessDAO(mediator);
//	    this.methodDAO = new MethodDAO(mediator);
    }


	/**
	 * @param objectProfile
	 * @return
	 * @throws DAOException
	 */
	public AccessProfileOption getAccessProfileOption(long objectProfile)
			throws DAOException
	{
		ObjectProfileEntity entity = this.objectProfileDAO.find(objectProfile);
		return AccessProfileOption.valueOf(entity.getName());
	}
    
//	/**
//	 * @param objectProfile
//	 * @return
//	 * @throws DAOException
//	 */
//	public AccessProfile buildAccessProfile(long objectProfile) 
//			throws DAOException
//	{
//		List<ObjectProfileAccessEntity> objectProfileAccessesEntities = 
//		getObjectProfileAccesses(objectProfile);
//	
//		int length =  objectProfileAccessesEntities==null
//    		?0:objectProfileAccessesEntities.size();
//		
//		if(length == 0)
//		{
//			return null;
//		}
//		int index = 0;
//		Set<MethodAccess> methodAccesses = new HashSet<MethodAccess>();
//		
//        for(;index < length; index++)
//        {
//        	ObjectProfileAccessEntity objectProfileAccessesEntity = 
//        			 objectProfileAccessesEntities.get(index);	 
//
//        	ObjectAccessEntity objectAccessEntity = this.objectAccessDAO.find(
//        		objectProfileAccessesEntity.getObjectAccessEntity());
//        		
//        	if(objectAccessEntity == null)
//        	{
//        		continue;
//        	}
//        	AccessLevel accessLevel = new AccessLevelImpl(
//        			objectAccessEntity.getLevel());
//        	
//        	MethodEntity methodEntity = this.methodDAO.find(
//        		objectProfileAccessesEntity.getMethodEntity());
//        	
//        	if(methodEntity == null)
//        	{
//        		continue;
//        	}
//        	AccessMethod.Type method =  AccessMethod.Type.valueOf(
//        			methodEntity.getName());  
//    	
//        	MethodAccess methodAccess = new MethodAccessImpl(
//        			accessLevel, method);
//        	
//        	methodAccesses.add(methodAccess);	        	
//        }	  
//        AccessProfile accessProfile = new AccessProfileImpl(methodAccesses);
//        return accessProfile;
//	}
	
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
    public List<ObjectProfileAccessEntity> getObjectProfileAccesses(
    	ObjectProfileEntity objectProfileEntity)
    		throws DAOException
    {
    	return getObjectProfileAccesses(objectProfileEntity.getIdentifier());
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
    public List<ObjectProfileAccessEntity> getObjectProfileAccesses(
        final long identifier) throws DAOException
    {
    	return super.select(new HashMap<String,Object>(){{
    			this.put("OPID", identifier);}});
    }
    
}
