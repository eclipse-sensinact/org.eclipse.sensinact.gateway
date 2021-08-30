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

import java.io.FileInputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.user.openid.KeyCollection.Keys;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;

import com.fasterxml.jackson.databind.ObjectMapper;


public class OpenIdUserKeyBuilderConfig {
	
	private static final String AUTHENTICATION_SERVICE_CONFIG = "authentication.service.config";
		
	private String discoveryURL = null;
	private String certsURL = null;
	private String clientId;
	private String clientSecret;
	
	private URI authEP;
	private URI tokenEP;
	private URI userinfoEP;
	private URI logoutEP;
	
	private List<Keys> publicKeys;

	public OpenIdUserKeyBuilderConfig(Mediator mediator) {
		
		if(mediator == null || mediator.getProperty("discoveryURL") == null) {
			try {
				Properties properties = new Properties();
				properties.load(new FileInputStream(AUTHENTICATION_SERVICE_CONFIG));
				
				discoveryURL = properties.getProperty("discoveryURL").toString();
				certsURL = properties.getProperty("certsURL");
				
				clientId = properties.getProperty("client_id").toString();
				clientSecret = properties.getProperty("client_secret").toString();
			} catch (Exception e) {
				mediator.error(e.getMessage(), e);
				throw new RuntimeException("Error while loading configuration", e);
			}
		} else {
			
			discoveryURL = mediator.getProperty("discoveryURL").toString();
			certsURL = mediator.getProperty("certsURL").toString();			
			clientId = mediator.getProperty("client_id").toString();
			clientSecret = mediator.getProperty("client_secret").toString();
		}
		if(discoveryURL != null)
			loadEndpoints();
		else
			mediator.error("Unable to load connected OpenID server endpoints");
	}
	
	private void loadEndpoints() {
		try {
			ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> connection = 
					new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
			connection.setUri(discoveryURL);
			connection.setAccept("application/json");
			
			SimpleRequest req = new SimpleRequest(connection);
			SimpleResponse resp = req.send();
			
			ObjectMapper mapper = new ObjectMapper();
			Endpoints endpoints =mapper.readValue(resp.getContent(), Endpoints.class);
						
			authEP = new URI(endpoints.getAuthEP());
			tokenEP = new URI(endpoints.getTokenEP());
			userinfoEP = new URI(endpoints.getUserinfoEP());
			logoutEP = new URI(endpoints.getLogoutEP());

			connection = new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
			connection.setUri(certsURL);
			connection.setAccept("application/json");

			req = new SimpleRequest(connection);
			resp = req.send();
			
			mapper = new ObjectMapper();
			KeyCollection keyCollection =  mapper.readValue(resp.getContent(), KeyCollection.class);
			
			this.publicKeys = keyCollection.getKeys();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error while loading endpoints", e);
		}
	}

	/**
	 * @return the authEP
	 */
	public URI getAuthEP() {
		return authEP;
	}

	/**
	 * @return the tokenEP
	 */
	public URI getTokenEP() {
		return tokenEP;
	}

	/**
	 * @return the userinfoEP
	 */
	public URI getUserinfoEP() {
		return userinfoEP;
	}

	/**
	 * @return the userinfoEP
	 */
	public URI getLogoutEP() {
		return logoutEP;
	}

	/**
	 * @return the publicKey
	 */
	public List<Keys> getPublicKeys() {
		return publicKeys;
	}


	/**
	 * @return the clientId
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return clientSecret;
	}
}
