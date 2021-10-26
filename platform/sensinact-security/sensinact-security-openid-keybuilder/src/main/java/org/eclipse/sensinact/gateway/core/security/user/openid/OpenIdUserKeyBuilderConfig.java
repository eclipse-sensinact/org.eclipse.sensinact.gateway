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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.user.openid.KeyCollection.Keys;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


public class OpenIdUserKeyBuilderConfig {
	
	private static final Logger LOG = LoggerFactory.getLogger(OpenIdUserKeyBuilderConfig.class);
	private static final String AUTHENTICATION_SERVICE_CONFIG = "authentication.service.config";
		
	private String discoveryURL = null;
	private String clientId;
	private String clientSecret;
	
	private URI authEP;
	private URI tokenEP;
	private URI userinfoEP;
	private URI logoutEP;
	
	private List<Keys> publicKeys;

	private final AtomicBoolean configured = new AtomicBoolean(false);
	
	private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	
	public OpenIdUserKeyBuilderConfig(Mediator mediator) {
		if(mediator == null||mediator.getProperty("org.eclipse.sensinact.security.keybuilder.openid.discoveryURL") == null) {
			try {
				Properties properties = new Properties();
				properties.load(new FileInputStream(AUTHENTICATION_SERVICE_CONFIG));
				
				discoveryURL = properties.getProperty(
					"org.eclipse.sensinact.security.keybuilder.openid.discoveryURL").toString();				
				clientId = properties.getProperty(
					"org.eclipse.sensinact.security.keybuilder.openid.client_id").toString();
				clientSecret = properties.getProperty(
					"org.eclipse.sensinact.security.keybuilder.openid.client_secret").toString();
				
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw new RuntimeException("Error while loading configuration", e);
			}
		} else {
			discoveryURL = mediator.getProperty(
				"org.eclipse.sensinact.security.keybuilder.openid.discoveryURL").toString();			
			clientId = mediator.getProperty(
				"org.eclipse.sensinact.security.keybuilder.openid.client_id").toString();
			clientSecret = mediator.getProperty(
				"org.eclipse.sensinact.security.keybuilder.openid.client_secret").toString();
		}
		if(discoveryURL != null)
			this.worker.scheduleWithFixedDelay(()->loadEndpoints(),1, 20, TimeUnit.SECONDS);
		else
			LOG.error("Unable to load connected OpenID server endpoints");
	}
	
	private void loadEndpoints() {
		try {
			ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> connection = 
					new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
			connection.setUri(discoveryURL);
			connection.setAccept("application/json");
			
			SimpleRequest req = new SimpleRequest(connection);
			SimpleResponse resp = null;
			resp = req.send();

			ObjectMapper mapper = new ObjectMapper();
			Endpoints endpoints =mapper.readValue(resp.getContent(), Endpoints.class);
						
			authEP = new URI(endpoints.getAuthEP());
			tokenEP = new URI(endpoints.getTokenEP());
			userinfoEP = new URI(endpoints.getUserinfoEP());
			logoutEP = new URI(endpoints.getLogoutEP());

			connection = new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
			connection.setUri(endpoints.getCertsEP());
			connection.setAccept("application/json");

			req = new SimpleRequest(connection);
			resp = req.send();
			
			mapper = new ObjectMapper();
			KeyCollection keyCollection = mapper.readValue(resp.getContent(), KeyCollection.class);
			
			this.publicKeys = keyCollection.getKeys();
			this.worker.shutdownNow();
			this.worker = null;
			synchronized(this.configured) {
				this.configured.set(true);
			}
		} catch (Exception e) {
			LOG.error( e.getMessage() , e);
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

	/**
	 * @return the configured status
	 */
	public boolean isConfigured() {
		boolean configured = false;
		synchronized(this.configured) {
			configured = this.configured.get();
		}
		return configured;
	}
	
	@Override
	public void finalize() {
		if(this.worker!=null && !this.worker.isShutdown())
			this.worker.shutdownNow();
		this.worker = null;
	}
}
