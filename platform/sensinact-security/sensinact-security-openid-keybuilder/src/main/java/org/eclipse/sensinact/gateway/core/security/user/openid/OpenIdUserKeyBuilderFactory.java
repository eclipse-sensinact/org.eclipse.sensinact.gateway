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

import java.util.Hashtable;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilder;
import org.eclipse.sensinact.gateway.core.security.UserKeyBuilderFactory;
import org.eclipse.sensinact.gateway.core.security.AccessToken;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;

/**
 * {@link UserKeyBuilderFactory} service implementation
 */
public class OpenIdUserKeyBuilderFactory implements UserKeyBuilderFactory<String,AccessToken,OpenIdUserKeyBuilder> {
	
	@Override
	public Class<OpenIdUserKeyBuilder> getType() {
		return OpenIdUserKeyBuilder.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	@Override
	public void newInstance(Mediator mediator) throws SecuredAccessException {
		UserKeyBuilder<String,AccessToken> builder = new OpenIdUserKeyBuilder(mediator);
		mediator.register(new Hashtable() {	{
			this.put("identityMaterial",AccessToken.class.getCanonicalName());
		}}, builder, new Class<?>[] { UserKeyBuilder.class });
	}
}
