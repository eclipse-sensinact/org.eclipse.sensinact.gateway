/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.entity;

import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;

/**
 * UserAccess DAO Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "USER_ACCESS")
@PrimaryKey(value = { "UAID" })
public class UserAccessEntity extends ImmutableSnaEntity implements AccessLevel {
	@Column(value = "UAID")
	private long identifier;

	@Column(value = "UALEVEL")
	private int level;

	@Column(value = "UANAME")
	private String name;

	/**
	 * Constructor
	 * 
	 */
	public UserAccessEntity() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 * @param name
	 * @param level
	 */
	public UserAccessEntity(String name, int level) {
		this();
		this.setLevel(level);
		this.setName(name);
	}

	/**
	 * @inheritDoc
	 *
	 * @see SnaEntity#getIdentifier()
	 */
	public long getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(long identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}
}
