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
import java.security.InvalidKeyException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.AccessToken;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.UserKey;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;

/**
 *  {@link UserKeyBuilder} implementation in charge of building {@link UserKey}
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class OpenIdUserKeyBuilder implements UserKeyBuilder<String,AccessToken> {
	
	private Mediator mediator;
	private OpenIdUserKeyBuilderConfig config;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the {@link UserKeyBuilder}	to be instantiated to
	 * interat with the OSGi host environement 
	 */
	public OpenIdUserKeyBuilder(Mediator mediator)  {
		this.mediator = mediator;
		this.config = new OpenIdUserKeyBuilderConfig(mediator);
	}

	@Override
	public UserKey buildKey(AccessToken token) throws InvalidKeyException, InvalidCredentialException, DataStoreException {
		OpenIdUser user = null;
		try {
			user = getUserInfo(token.getAuthenticationMaterial());
		} catch (IOException e) {
			mediator.error(e);
		}
		if (user == null) 
			return null;
		else
			return new UserKey(user.getSensiNactPublicKey());
	}
	
	private OpenIdUser getUserInfo(String bearer) throws IOException {

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
