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
import org.eclipse.sensinact.gateway.core.security.entity.annotation.NotNull;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;

/**
 * Method Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "BUNDLE")
@PrimaryKey(value = { "BID" })
public class BundleEntity extends SnaEntity {
	@Column(value = "BID")
	private long identifier;

	@NotNull
	@Column(value = "BNAME")
	private String name;

	@NotNull
	@Column(value = "BSHA")
	private String signature;

	@Column(value = "SAUTH")
	private int sauth;

	@Column(value = "OPID")
	@ForeignKey(refer = "OPID", table = "OBJECT_PROFILE")
	private long objectProfileEntity;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 */
	public BundleEntity(Mediator mediator) {
		super(mediator);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 * @param row
	 * 
	 */
	public BundleEntity(Mediator mediator, JSONObject row) {
		super(mediator, row);
	}

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 * @param parent
	 * @param path
	 * @param userProfileEntity
	 * @param objectProfileEntity
	 */
	public BundleEntity(Mediator mediator, String name, String signature, int sauth, long objectProfileEntity) {
		this(mediator);
		this.setName(name);
		this.setSignature(signature);
		this.setSauth(sauth);
		this.setObjectProfileEntity(objectProfileEntity);
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
	 * @param path
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the sha
	 */
	public String getSignature() {
		return this.signature;
	}

	/**
	 * @param path
	 *            the sha to set
	 */
	public void setSignature(String signature) {
		this.signature = signature;
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
