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
package org.eclipse.sensinact.gateway.core.security.impl;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.AuthorizationServiceException;
import org.eclipse.sensinact.gateway.core.security.dao.AgentAccessLevelDAO;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.dao.UserAccessLevelDAO;
import org.eclipse.sensinact.gateway.core.security.entity.AgentAccessLevelEntity;
import org.eclipse.sensinact.gateway.core.security.entity.UserAccessLevelEntity;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AuthorizationServiceImpl implements AuthorizationService
{
	private UserAccessLevelDAO userAccessLevelDAO;
	private AgentAccessLevelDAO agentAccessLevelDAO;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @throws DAOException
	 */
	public AuthorizationServiceImpl(Mediator mediator) throws DAOException
	{
		this.userAccessLevelDAO = new UserAccessLevelDAO(mediator);
		this.agentAccessLevelDAO = new AgentAccessLevelDAO(mediator);
	}

	/** 
	 * @throws DAOException 
	 * @inheritDoc
	 * 
	 * @see AuthorizationService#
	 * getAccessLevel(java.lang.String, long)
	 */
	@Override
	public AccessLevelOption getUserAccessLevelOption(
		String path, long uid) throws AuthorizationServiceException
	{	
		if(uid <= 0)
		{
			return AccessLevelOption.ANONYMOUS;
		}
		try
		{
			UserAccessLevelEntity userAccessLevelEntity = 
					this.userAccessLevelDAO.find(path, uid);
			
			return AccessLevelOption.valueOf(userAccessLevelEntity);
		}
		catch (DAOException e)
		{
			throw new AuthorizationServiceException(e);
		}
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see AuthorizationService#
	 * getAccessLevel(java.lang.String, java.lang.String)
	 */
	@Override
	public AccessLevelOption getUserAccessLevelOption(
			String path, String publicKey)
			throws AuthorizationServiceException 
	{	
    	if(publicKey == null || "anonymous".equals(publicKey))
		{
			return AccessLevelOption.ANONYMOUS;
		}
		try
		{
			UserAccessLevelEntity userAccessLevelEntity = 
					this.userAccessLevelDAO.find(path, publicKey);
			return AccessLevelOption.valueOf(userAccessLevelEntity);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new AuthorizationServiceException(e);
		}
	}

	/** 
	 * @throws DAOException 
	 * @inheritDoc
	 * 
	 * @see AuthorizationService#
	 * getAccessLevel(java.lang.String, long)
	 */
	@Override
	public AccessLevelOption getAgentAccessLevelOption(
			String path, long uid) 
			throws AuthorizationServiceException
	{	
		if(uid <= 0)
		{
			return AccessLevelOption.ANONYMOUS;
		}
		try
		{
			AgentAccessLevelEntity agentAccessLevelEntity = 
					this.agentAccessLevelDAO.find(path, uid);
			
			return AccessLevelOption.valueOf(agentAccessLevelEntity);
		}
		catch (DAOException e)
		{
			throw new AuthorizationServiceException(e);
		}		
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see AuthorizationService#
	 * getAccessLevel(java.lang.String, java.lang.String)
	 */
	@Override
	public AccessLevelOption getAgentAccessLevelOption(
			String path, String publicKey)
			throws AuthorizationServiceException 
	{	
		if(publicKey == null || "anonymous".equals(publicKey))
		{
			return AccessLevelOption.ANONYMOUS;
		}		
		try
		{
			AgentAccessLevelEntity agentAccessLevelEntity = 
					this.agentAccessLevelDAO.find(path, publicKey);
			
			return AccessLevelOption.valueOf(agentAccessLevelEntity);
		}
		catch (DAOException e)
		{
			throw new AuthorizationServiceException(e);
		}		
	}
}
