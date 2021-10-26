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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;

/**
 * ObjectAccess DAO Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "OBJECT_ACCESS")
@PrimaryKey(value = { "OAID" })
public class ObjectAccessEntity extends ImmutableSnaEntity {
	@Column(value = "OAID")
	private long identifier;

	@Column(value = "OALEVEL")
	private int level;

	@Column(value = "OANAME")
	private String name;

	/**
	 * Constructor
	 * 
	 */
	public ObjectAccessEntity() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param row
	 * 
	 */
	public ObjectAccessEntity(JSONObject row) {
		super(row);
	}

	/**
	 * Constructor
	 * 
	 * @param identifier
	 * @param name
	 * @param level
	 */
	public ObjectAccessEntity(String name, int level) {
		this();
		this.setLevel(level);
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
