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

import java.util.Hashtable;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilderFactory;

import aQute.bnd.annotation.Resolution;
import aQute.bnd.annotation.spi.ServiceProvider;

import org.eclipse.sensinact.gateway.core.security.AccessToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;

/**
 * {@link UserKeyBuilderFactory} service implementation
 */
@ServiceProvider(value = UserKeyBuilderFactory.class, resolution = Resolution.OPTIONAL)
public class OpenIdUserKeyBuilderFactory implements UserKeyBuilderFactory<String,AccessToken,OpenIdAccessTokenUserKeyBuilder> {
	
	@Override
	public Class<OpenIdAccessTokenUserKeyBuilder> getType() {
		return OpenIdAccessTokenUserKeyBuilder.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	@Override
	public void newInstance(Mediator mediator) throws SecuredAccessException {

		OpenIdUserKeyBuilderConfig config = new OpenIdUserKeyBuilderConfig(mediator);
		
		UserKeyBuilder<String,AccessToken> accessTokenUserKeyBuilder = new OpenIdAccessTokenUserKeyBuilder(config);
		mediator.register(new Hashtable() {	{
			this.put("identityMaterial", AccessToken.class.getCanonicalName());
		}}, accessTokenUserKeyBuilder, new Class<?>[] { UserKeyBuilder.class });
		
		UserKeyBuilder<Credentials,Credentials> credentialsUserKeyBuilder = new OpenIdCredentialsUserKeyBuilder(config);
		mediator.register(new Hashtable() {	{
			this.put("identityMaterial", Credentials.class.getCanonicalName());
		}}, credentialsUserKeyBuilder, new Class<?>[] { UserKeyBuilder.class });
	}
}
