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
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;

/**
 * Agent Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "AGENT")
@PrimaryKey(value = { "AID" })
public class AgentEntity extends SnaEntity {
	@Column(value = "AID")
	private long identifier;

	@Column(value = "APUBLIC_KEY")
	private String publicKey;

	@Column(value = "BID")
	@ForeignKey(refer = "BID", table = "BUNDLE")
	private long bundleEntity;

	/**
	 * Constructor
	 * 
	 */
	public AgentEntity() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param row
	 * 
	 */
	public AgentEntity(JSONObject row) {
		super(row);
	}

	/**
	 * Constructor
	 * 
	 * @param login
	 * @param password
	 * @param mail
	 */
	public AgentEntity(String publicKey, long bundleEntity) {
		this();
		this.setPublicKey(publicKey);
		this.setBundleEntity(bundleEntity);
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
	 * @return the login
	 */
	public long getBundleEntity() {
		return this.bundleEntity;
	}

	/**
	 * @param login
	 *            the login to set
	 */
	public void setBundleEntity(long bundleEntity) {
		this.bundleEntity = bundleEntity;
	}

	/**
	 * @return the public key
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey
	 *            the public key to set
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
}
