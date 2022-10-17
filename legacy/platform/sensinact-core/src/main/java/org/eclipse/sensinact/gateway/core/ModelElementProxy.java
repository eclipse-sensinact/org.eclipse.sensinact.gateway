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
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * Abstract {@link SensiNactResourceModelElementProxy} implementation
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public abstract class ModelElementProxy implements Nameable, PathElement {
	/**
	 * Returns the {@link AccessMethod} of this ModelElementProxy whose
	 * {@link AccessMethod.Type} is passed as parameter
	 * 
	 * @param type
	 *            the {@link AccessMethod.Type} name of the {@link AccessMethod} to
	 *            be returned
	 * @return the method of this ModelElementProxy with the specified type
	 */
	protected abstract AccessMethod<?, ?> getAccessMethod(String type);

	/**
	 * the proxied class
	 */
	protected final Class<?> proxied;

	/**
	 * the String path of this ModelElementProxy
	 */
	protected final String path;

	/**
	 * the {@link Mediator} allowing to interact with the OSGi host environment
	 */
	protected Mediator mediator;

	/**
	 * the String name of this ModelElementProxy
	 */
	private String name;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} that will allow the ModelElementProxy to
	 *            instantiate to interact with the OSGi host environment
	 * @param proxied
	 *            the extended {@link Resource} type the ModelElementProxy to
	 *            instantiate will be the proxy of
	 * @param path
	 *            the string path of the ModelElementProxy to instantiate
	 */
	protected ModelElementProxy(Mediator mediator, Class<?> proxied, String path) {
		this.path = path;
		this.name = UriUtils.getLeaf(path);
		this.mediator = mediator;
		this.proxied = proxied;
	}
	
	@Override
	public String getPath() {
		return this.path;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the type proxied by this ModelElementProxy
	 * 
	 * @return this ModelElementProxy's proxied type
	 */
	public Class<?> getProxied() {
		return this.proxied;
	}

	/**
	 * Processes an {@link AccessMethod} method invocation on this ModelElementProxy
	 * and returns the {@link AccessMethodResponse} resulting of the invocation
	 * 
	 * @param method
	 *            the {@link AccessMethod.Type} of the {@link AccessMethod} to
	 *            process
	 * @param parameters
	 *            an array of objects containing the values of the arguments passed
	 *            in the method invocation on the proxy instance, or null if
	 *            interface method takes no arguments.
	 * 
	 * @return the resulting {@link AccessMethodResponse}
	 */
	public AccessMethodResponse<?> invoke(String type, Object[] parameters) throws Throwable {
		AccessMethod<?, ?> accessMethod = this.getAccessMethod(type);
		if (accessMethod == null) {
			AccessMethodResponse<?> message = AccessMethodResponse.error(this.mediator, path,
					AccessMethod.Type.valueOf(type), SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
					new StringBuilder().append(type).append(" method not found").toString(), null);
			return message;
		}
		return accessMethod.invoke(parameters);
	}
}
