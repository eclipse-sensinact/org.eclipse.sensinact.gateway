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
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.NotNull;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;

import jakarta.json.JsonObject;

/**
 * Object Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "OBJECT")
@PrimaryKey(value = { "OID" })
public class ObjectEntity extends SnaEntity {
	@Column(value = "OID")
	private long identifier;

	@Column(value = "BID")
	@ForeignKey(refer = "BID", table = "BUNDLE")
	private long bundleEntity;

	@Column(value = "OPID")
	@ForeignKey(refer = "OPID", table = "OBJECT_PROFILE")
	private long objectProfileEntity;

	@NotNull
	@Column(value = "NAME")
	private String name;

	@Column(value = "PATTERN")
	private int pattern;

	@Column(value = "SAUTH")
	private int sauth;

	@Column(value = "PARENT")
	private long parent;

	@Column(value = "PATH")
	private String path;

	/**
	 * Constructor
	 * 
	 */
	public ObjectEntity() {
		super();
	}

	/**
	 * Constructor

	 * @param row
	 * 
	 */
	public ObjectEntity(JsonObject row) {
		super(row);
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param path
	 * @param userProfileEntity
	 * @param objectProfileEntity
	 */
	public ObjectEntity(long bundleEntity, long objectProfileEntity, String name, int pattern,
			int sauth, long parent, String path) {
		this();
		this.setParent(parent);
		this.setName(name);
		this.setPattern(pattern);
		this.setSauth(sauth);
		this.setObjectProfileEntity(objectProfileEntity);
		this.setBundleEntity(bundleEntity);
		this.setPath(path);
	}

	/**
	 * @inheritDoc
	 *
	 * @see SnaEntity# getIdentifier()
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
	 * @return the parent
	 */
	public long getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(long parent) {
		this.parent = parent;
	}

	/**
	 * @return the path
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public boolean isPattern() {
		return this.pattern != 0;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPattern(int pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return true if a user known by the system is considered as authenticated for
	 *         this object
	 */
	public boolean isSauth() {
		return this.sauth != 0;
	}

	/**
	 * @param sauth
	 *            the system authenticated value to set
	 */
	public void setSauth(int sauth) {
		this.sauth = sauth;
	}

	/**
	 * @return the BundleEntity
	 */
	public long getBundleEntity() {
		return bundleEntity;
	}

	/**
	 * @param bundleEntity
	 *            the BundleEntity to set
	 */
	public void setBundleEntity(long bundleEntity) {
		this.bundleEntity = bundleEntity;
	}

	/**
	 * @return the objectProfile
	 */
	public long getObjectProfileEntity() {
		return objectProfileEntity;
	}

	/**
	 * @param objectProfile
	 *            the objectProfile to set
	 */
	public void setObjectProfileEntity(long objectProfileEntity) {
		this.objectProfileEntity = objectProfileEntity;
	}
}
