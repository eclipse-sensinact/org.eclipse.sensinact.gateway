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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;

import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  {@link UserKeyBuilder} implementation in charge of building {@link UserKey}
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class OpenIdCredentialsUserKeyBuilder implements UserKeyBuilder<Credentials,Credentials> {

	private static final Logger LOG = LoggerFactory.getLogger(OpenIdCredentialsUserKeyBuilder.class);
	
	private OpenIdUserKeyBuilderConfig config;

	/**
	 * Constructor
	 * 
	 * @param config the {@link OpenIdUserKeyBuilderConfig} of the {@link UserKeyBuilder} to be instantiated
	 */
	public OpenIdCredentialsUserKeyBuilder(OpenIdUserKeyBuilderConfig config)  {
		this.config = config;
	}

	@Override
	public UserKey buildKey(Credentials credentials) throws InvalidKeyException, InvalidCredentialException, DataStoreException {
		OpenIdUser user = null;
		try {
			user = getUserInfo(credentials);
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
		if (user == null) 
			return null;
		else
			return new UserKey(user.getSensiNactPublicKey());
	}
	
	private OpenIdUser getUserInfo(Credentials credentials) throws IOException {
		if(!this.config.isConfigured())
			return null;
		String token = null;
		try {
			ConnectionConfigurationImpl<SimpleResponse,SimpleRequest> conf = 
					new ConnectionConfigurationImpl<>();
			conf.setHttpMethod("POST");
			conf.setContentType("application/x-www-form-urlencoded");
			
			conf.setUri(this.config.getTokenEP().toURL().toExternalForm());
			String clientCredentials = new String(this.config.getClientId() + ":" + this.config.getClientSecret());
			String basic = Base64.getEncoder().encodeToString(clientCredentials.getBytes(StandardCharsets.UTF_8));
			conf.addHeader("Authorization", "Basic " + basic);
			
			StringBuilder urlParameters = new StringBuilder();
			
			String username = credentials.login;
			String password = credentials.password;
					
			urlParameters.append("client_id=");
			urlParameters.append(this.config.getClientId());
			urlParameters.append("&username=");
			urlParameters.append(username);
			urlParameters.append("&password=");
			urlParameters.append(password);
			urlParameters.append("&scope=openid%20roles");
			urlParameters.append("&grant_type=password");
			urlParameters.append("&response_type=id_token%20token");
			conf.setContent(urlParameters.toString());
			
			SimpleResponse response = new SimpleRequest(conf).send();

			if (response.getStatusCode() == 200) {
				JSONObject obj = new JSONObject(new String(response.getContent(),"UTF-8"));
				token = obj.getString("access_token");
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(),e);
		}
		if(token == null)
			return null;
		
		JsonWebToken jwt = new JsonWebToken(token,this.config.getPublicKeys());

		if(!jwt.isValid()) 
			return null;
		
		ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> connection = 
				new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
		connection.setUri(config.getUserinfoEP().toURL().toExternalForm());
		connection.queryParameter("client_id", config.getClientId());
		connection.addHeader("Authorization", "Bearer " + jwt.token());
		connection.setHttpMethod("GET");
		
		SimpleRequest req = new SimpleRequest(connection);
		SimpleResponse resp = req.send();
		
		byte[] content = resp.getContent();
		String data = new String(content);
		
		OpenIdUser user = new OpenIdUser(config, data, jwt);
		if(user.isValid())
			return user;
		return null;
	}

}
