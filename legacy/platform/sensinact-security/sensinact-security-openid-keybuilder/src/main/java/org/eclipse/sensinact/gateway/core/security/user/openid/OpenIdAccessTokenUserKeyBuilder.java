/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.user.openid;

import java.io.IOException;
import java.security.InvalidKeyException;

import org.eclipse.sensinact.gateway.core.security.AccessToken;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  {@link UserKeyBuilder} implementation in charge of building {@link UserKey}
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class OpenIdAccessTokenUserKeyBuilder implements UserKeyBuilder<String,AccessToken> {

	private static final Logger LOG = LoggerFactory.getLogger(OpenIdAccessTokenUserKeyBuilder.class);
	
	private OpenIdUserKeyBuilderConfig config;

	/**
	 * Constructor
	 * 
	 * @param config the {@link OpenIdUserKeyBuilderConfig} of the {@link UserKeyBuilder} to be instantiated
	 */
	public OpenIdAccessTokenUserKeyBuilder(OpenIdUserKeyBuilderConfig config)  {
		this.config = config;
	}

	@Override
	public UserKey buildKey(AccessToken token) throws InvalidKeyException, InvalidCredentialException, DataStoreException {
		OpenIdUser user = null;
		try {
			user = getUserInfo(token.getAuthenticationMaterial());
		} catch (IOException e) {
			LOG.error(e.getMessage(),e);
		}
		if (user == null) 
			return null;
		else
			return new UserKey(user.getSensiNactPublicKey());
	}
	
	private OpenIdUser getUserInfo(String bearer) throws IOException {
		if(!this.config.isConfigured())
			return null;
		
		JsonWebToken jwt = new JsonWebToken(bearer, config.getPublicKeys());
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
