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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.UnaccessibleAccessMethod;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;

/**
 * A {@link Resource} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceProxy extends ModelElementProxy {
	/**
	 * {@link AccessMethod}s of this ResourceProxy
	 */
	protected final Map<String, AccessMethod<?,?>> methods;

	/**
	 * Constructor
	 * 
	 * @param resource
	 *            the proxied resource
	 */
	ResourceProxy(Mediator mediator, ResourceImpl resource, List<MethodAccessibility> methodAccessibilities) {
		super(mediator, Resource.class, resource.getPath());

		Map<String, AccessMethod<?,?>> methods = new HashMap<>();
		AccessMethod.Type[] existingTypes = AccessMethod.Type.values();

		int index = 0;
		int length = existingTypes == null ? 0 : existingTypes.length;

		for (; index < length; index++) {
			AccessMethod<?,?> method = null;
			if ((method = resource.getAccessMethod(existingTypes[index])) == null) {
				continue;
			}
			String name = existingTypes[index].name();
			
			if (methodAccessibilities.stream()
					.filter(ma -> name.equals(ma.getName()))
					.map(ma -> !ma.isAccessible())
					.findFirst()
					.orElse(true)) {
				methods.put(name, new UnaccessibleAccessMethod(mediator, super.getPath(), existingTypes[index]));
			} else {
				methods.put(name, method);
			}
		}
		this.methods = Collections.unmodifiableMap(methods);
	}

	@Override
	public AccessMethod<?,?> getAccessMethod(String type) {
		return this.methods.get(type);
	}
}
