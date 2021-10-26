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

import java.security.InvalidKeyException;

import org.eclipse.sensinact.gateway.core.security.User;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Method Entity
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "SNAUSER")
@PrimaryKey(value = { "SUID" })
public class UserEntity extends SnaEntity implements User {
	private static final Logger LOG = LoggerFactory.getLogger(UserEntity.class);

	@Column(value = "SUID")
	private long identifier;

	@Column(value = "SULOGIN")
	private String login;

	@Column(value = "SUPASSWORD")
	private String password;

	@Column(value = "SUACCOUNT")
	private String account;

	@Column(value = "SUACCOUNTTYPE")
	private String accounttype;

	@Column(value = "SUPUBLIC_KEY")
	private String publicKey;

	/**
	 * Constructor
	 */
	public UserEntity() {
		super();
	}

	/**
	 * Constructor
	 * @param row
	 * 
	 */
	public UserEntity(JSONObject row) {
		super(row);
	}

	/**
	 * Constructor
	 * 
	 * @param login
	 * @param password
	 * @param mail
	 */
	public UserEntity(String login, String password, String account) {
		this(login, password, account, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param login
	 * @param password
	 * @param account
	 * @param accountType
	 */
	public UserEntity(String login, String password, String account, String accounttype) {
		this(login, password, account, accounttype, null);
	}

	/**
	 * Constructor
	 * 
	 * @param login
	 * @param password
	 * @param account
	 * @param accountType
	 * @param publicKey
	 */
	public UserEntity(String login, String password, String account, String accounttype,
			String publicKey) {
		this();
		this.setLogin(login);
		this.setPassword(password);
		this.setAccount(account);
		this.setAccounttype(accounttype);
		this.setPublicKey(publicKey);
	}

	public long getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(long identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the account
	 */
	public String getAccount() {
		return this.account;
	}

	/**
	 * @param account the account to be set
	 */
	public void setAccount(String account) {
		this.account = account;
	}

	/**
	 * @return the accounttype
	 */
	public String getAccounttype() {
		if (this.accounttype == null) {
			return User.MAIL_ACCOUNT;
		}
		return this.accounttype;
	}

	/**
	 * @param accounttype the accounttype to be set
	 */
	public void setAccounttype(String accounttype) {
		this.accounttype = accounttype;
	}

	/**
	 * @return the public key
	 */
	public String getPublicKey() {
		if (this.publicKey == null) {
			String publicKeyStr = new StringBuilder().append(this.login).append(":").append(this.account)
					.append(System.currentTimeMillis()).toString();
			try {
				this.publicKey = CryptoUtils.cryptWithMD5(publicKeyStr);

			} catch (InvalidKeyException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return this.publicKey;
	}

	/**
	 * @param publicKey the public key to set
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public String getAccountType() {
		return this.getAccounttype();
	}

	@Override
	public boolean isAnonymous() {
		return this.getPublicKey().startsWith(UserManager.ANONYMOUS_PKEY);
	}
}
