/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Name;
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

		Map<String, AccessMethod<?,?>> methods = new HashMap<String, AccessMethod<?,?>>();
		AccessMethod.Type[] existingTypes = AccessMethod.Type.values();

		int index = 0;
		int length = existingTypes == null ? 0 : existingTypes.length;

		for (; index < length; index++) {
			AccessMethod<?,?> method = null;
			if ((method = resource.getAccessMethod(existingTypes[index])) == null) {
				continue;
			}
			int accessIndex = -1;

			if ((accessIndex = methodAccessibilities.indexOf(new Name<MethodAccessibility>(existingTypes[index].name()))) == -1
					|| !methodAccessibilities.get(accessIndex).isAccessible()) {
				methods.put(existingTypes[index].name(),new UnaccessibleAccessMethod(mediator, super.getPath(), existingTypes[index]));
			} else {
				methods.put(existingTypes[index].name(), method);
			}
		}
		this.methods = Collections.<String, AccessMethod<?,?>>unmodifiableMap(methods);
	}

	@Override
	public AccessMethod<?,?> getAccessMethod(String type) {
		return this.methods.get(type);
	}
}
