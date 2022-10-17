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

import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;

import jakarta.json.JsonObject;

/**
 * ObjectProfile DAO Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "OBJECT_PROFILE")
@PrimaryKey(value = { "OPID" })
public class ObjectProfileEntity extends ImmutableSnaEntity {
	@Column(value = "OPID")
	private long identifier;

	@Column(value = "OPNAME")
	private String name;

	/**
	 * Constructor
	 * 
	 */
	public ObjectProfileEntity() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param row
	 * 
	 */
	public ObjectProfileEntity(JsonObject row) {
		super(row);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 * @param name
	 */
	public ObjectProfileEntity(String name) {
		this();
		this.setName(name);
	}

	/**
	 * @return
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
}
