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
package org.eclipse.sensinact.gateway.core.security.entity;

import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;

/**
 * UserAccessLevel DAO Entity 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "AGENT_ACCESS_LEVEL")
@PrimaryKey(value = {"AID, SUID"})
public class AgentAccessLevelEntity extends ImmutableSnaEntity implements AccessLevel
{
	@Column(value = "AID")
	@ForeignKey(refer = "AID", table = "AGENT")
	private long agent;
	
	@Column(value = "OID")
	@ForeignKey(refer = "OID", table = "OBJECT")
	private long object;
	
	@Column(value = "APUBLIC_KEY")
	private String publicKey;
	
	@Column(value = "UALEVEL")
	private int accessLevel;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 */
	public AgentAccessLevelEntity(Mediator mediator)
	{
		super(mediator);
	}
	
	/**
	 * Constructor 
	 * 
	 * @param mediator
	 * 		the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 * @param publicKey
	 * @param user
	 * @param object
	 * @param accessLevel
	 */
	public AgentAccessLevelEntity(Mediator mediator,
			JSONObject row)
	{
		super(mediator, row);
	}
	
	/**
	 * Constructor 
	 * 
	 * @param mediator
	 * 		the {@link Mediator} allowing to
	 * 		interact with the OSGi host environment
	 * @param publicKey
	 * @param user
	 * @param object
	 * @param accessLevel
	 */
	public AgentAccessLevelEntity(Mediator mediator, 
			String publicKey, long agent, long object, 
			int accessLevel)
	{
		this(mediator);
		this.setAgent(agent);
		this.setObject(object);
		this.setPublicKey(publicKey);
		this.setAccessLevel(accessLevel);
	}

	/**
	 * 
	 * @return the public key of the associated user
	 */
    public String getPublicKey()
    {
	    return publicKey;
    }

	/**
	 * @param publicKey the user's public key to set
	 */
    public void setPublicKey(String publicKey)
    {
	    this.publicKey = publicKey;
    }

	/**
	 * @return the object identifier
	 */
    public long getObject()
    {
	    return object;
    }

	/**
	 * @param object the object identifier 
	 * to set
	 */
    public void setObject(long object)
    {
	    this.object = object;
    }

	/**
	 * @return the user identifier
	 */
	public long getAgent() {
		return agent;
	}

	/**
	 * @param user the user identifier to set
	 */
	public void setAgent(long agent) {
		this.agent = agent;
	}
	
	/**
	 * @return access level
	 */
	public int getLevel() 
	{
		return this.accessLevel;
	}

	/**
	 * @param the access level to set
	 */
	public void setAccessLevel(int accessLevel)
	{
		this.accessLevel = accessLevel;
	}
}
