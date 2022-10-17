/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * {@link ServiceProvider} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceProviderProxy extends ModelElementProxy {
	/**
	 * @param mediator
	 * @param name
	 */
	public ServiceProviderProxy(Mediator mediator, String name) {
		super(mediator, ServiceProvider.class, UriUtils.getUri(new String[] { name }));
	}

	@Override
	public AccessMethod<?,?> getAccessMethod(String name) {
		return null;
	}
}
