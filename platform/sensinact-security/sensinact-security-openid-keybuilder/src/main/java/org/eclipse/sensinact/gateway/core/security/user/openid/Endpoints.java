/*
* Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.user.openid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Endpoints wraps endpoint addresses to the third party authentication server 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Endpoints {

	@JsonProperty(value="authorization_endpoint")
	private String authEP;

	@JsonProperty(value="token_endpoint")
	private String tokenEP;

	@JsonProperty(value="userinfo_endpoint")
	private String userinfoEP;

	@JsonProperty(value="end_session_endpoint")
	private String logoutEP;

	@JsonProperty(value="issuer")
	private String issuer;
	
	@JsonProperty(value="jwks_uri")
	private String certsEP;

	/**
	 * Constructor
	 */
	public Endpoints() {}

	/**
	 * Constructor
	 * 
	 * @param authEP the authorization endpoint address for the Endpoints to be instantiated
	 * @param tokenEP the token endpoint address for the Endpoints to be instantiated
	 * @param userinfoEP the user information endpoint address for the Endpoints to be instantiated
	 * @param issuer the issuer endpoint address for the Endpoints to be instantiated
	 * @param certsEP the certificats endpoint address for the Endpoints to be instantiated
	 */
	public Endpoints(String authEP, String tokenEP, String userinfoEP, String issuer, String certsEP){
		this.authEP = authEP;
		this.tokenEP = tokenEP;
	    this.userinfoEP = userinfoEP;
		this.issuer = issuer;
		this.certsEP = certsEP;
	}

	/**
	 * Returns the authorization endpoint address of this Endpoints
	 * 
	 * @return this Endpoints' authorization endpoint address
	 */
	public String getAuthEP() {
		return authEP;
	}

	/**
	 * Defines the authorization endpoint address of this Endpoints
	 * 
	 * @param authEP the authorization endpoint address to be set
	 */
	public void setAuthEP(String authEP) {
		this.authEP = authEP;
	}
	
	/**
	 * Returns the user information endpoint address of this Endpoints
	 * 
	 * @return this Endpoints' user information endpoint address 
	 */
	public String getUserinfoEP() {
		return userinfoEP;
	}

	/**
	 * Defines the user information endpoint address of this Endpoints
	 * 
	 * @param userinfoEP the user information endpoint address to be set
	 */
	public void setUserinfoEP(String userinfoEP) {
		this.userinfoEP = userinfoEP;
	}

	/**
	 * Returns the token endpoint address of this Endpoints
	 * 
	 * @return this Endpoints' token endpoint address 
	 */
	public String getTokenEP() {
		return tokenEP;
	}

	/**
	 * Defines the token endpoint address of this Endpoints
	 * 
	 * @param tokenEP the token endpoint address to be set
	 */
	public void setTokenEP(String tokenEP) {
		this.tokenEP = tokenEP;
	}

	/**
	 * Returns the logout endpoint address of this Endpoints
	 * 
	 * @return this Endpoints' logout endpoint address 
	 */
	public String getLogoutEP() {
		return logoutEP;
	}
	
	/**
	 * Defines the logout endpoint address of this Endpoints
	 * 
	 * @param logout the token endpoint address to be set
	 */
	public void setLogoutEP(String logoutEP) {
		this.logoutEP = logoutEP;
	}

	/**
	 * Returns the issuer endpoint address of this Endpoints
	 * 
	 * @return this Endpoints' issuer endpoint address 
	 */
	public String getIssuer() {
		return issuer;
	}

	/**
	 * Defines the issuer endpoint address of this Endpoints
	 * 
	 * @param issuer the issuer endpoint address to be set
	 */
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	/**
	 * Returns the certifiats endpoint address for this Endpoints
	 * 
	 * @return the certificats endpoint address for this Endpoints
	 */
	public String getCertsEP() {
		return certsEP;
	}

	/**
	 * Defines the certifiats endpoint address for this Endpoints
	 * 
	 * @param certsEP the certificats endpoint address for this Endpoints
	 */
	public void setCertsEP(String certsEP) {
		this.certsEP = certsEP;
	}
}
