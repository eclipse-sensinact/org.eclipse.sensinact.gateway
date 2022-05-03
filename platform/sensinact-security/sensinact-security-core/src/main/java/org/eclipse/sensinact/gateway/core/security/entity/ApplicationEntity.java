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
import org.json.JSONObject;

/**
 * Application Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "APPLICATION")
@PrimaryKey(value = { "APPID" })
public class ApplicationEntity extends SnaEntity {
	@Column(value = "APPID")
	private long identifier;

	@Column(value = "OID")
	private long objectId;

	@Column(value = "APP_PUBLIC_KEY")
	private String publicKey;

	@Column(value = "APP_PRIVATE_KEY")
	private String privateKey;

	/**
	 * Constructor
	 * 
	 */
	public ApplicationEntity() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param row
	 */
	public ApplicationEntity(JSONObject row) {
		super(row);
	}

	/**
	 * Constructor
	 * 
	 * @param objectId
	 * @param publicKey
	 */
	public ApplicationEntity(long objectId, String publicKey, String privateKey) {
		this();
		this.setObjectId(objectId);
		this.setPublicKey(publicKey);
		this.setPrivateKey(privateKey);
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
	public long getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId
	 *            the object identifier to set
	 */
	public void setObjectId(long objectId) {
		this.objectId = objectId;
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

	/**
	 * @return the private key
	 */
	public String getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privateKey
	 *            the private key to set
	 */
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
}
