/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.entity;

import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;

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
	public ObjectProfileEntity(JSONObject row) {
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
