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
 * ObjectProfileAccess DAO Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "AUTHENTICATED")
@PrimaryKey(value = { "PUBLIC_KEY", "OID", "UAID" })
public class AuthenticatedEntity extends SnaEntity {
	@NotNull
	@Column(value = "PUBLIC_KEY")
	private String publicKey;

	@NotNull
	@Column(value = "OID")
	@ForeignKey(refer = "OID", table = "OBJECT")
	private long objectEntity;

	@NotNull
	@Column(value = "UAID")
	@ForeignKey(refer = "UAID", table = "USER_ACCESS")
	private long userAccessEntity;

	/**
	 * Constructor
	 * 
	 */
	public AuthenticatedEntity() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param row
	 */
	public AuthenticatedEntity(JsonObject row) {
		super(row);
	}

	/**
	 * Constructor
	 * 
	 * @param methodEntity
	 * @param objectProfileEntity
	 * @param objectAccessEntity
	 */
	public AuthenticatedEntity(String publicKey, long objectEntity, long userAccessEntity) {
		super();
		this.setPublicKey(publicKey);
		this.setObjectEntity(objectEntity);
		this.setUserAccessEntity(userAccessEntity);
	}

	/**
	 * @return the userEntity
	 */
	public String getPublicKey() {
		return this.publicKey;
	}

	/**
	 * @param userEntity
	 *            the userEntity to set
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * @return the objectEntity
	 */
	public long getObjectEntity() {
		return objectEntity;
	}

	/**
	 * @param objectEntity
	 *            the objectEntity to set
	 */
	public void setObjectEntity(long objectEntity) {
		this.objectEntity = objectEntity;
	}

	/**
	 * @return the userAccessEntity
	 */
	public long getUserAccessEntity() {
		return userAccessEntity;
	}

	/**
	 * @param userAccessEntity
	 *            the userAccessEntity to set
	 */
	public void setUserAccessEntity(long userAccessEntity) {
		this.userAccessEntity = userAccessEntity;
	}

}
