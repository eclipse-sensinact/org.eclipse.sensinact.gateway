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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;

/**
 * Abstract extended {@link ElementsProxyWrapper} implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ElementsProxyWrapper<M extends ModelElementProxy, P extends Nameable>
		implements ElementsProxy<P>, InvocationHandler, Nameable, PathElement {
	/**
	 * the proxied class
	 */
	protected final M proxy;

	/**
	 * Constructor
	 * 
	 * @param proxy
	 *            the extended {@link ModelElementProxy} to be wrapped by the
	 *            ProxyWrapper to be instantiated
	 */
	protected ElementsProxyWrapper(M proxy) {
		this.proxy = proxy;
	}

	/**
	 * @inheritDoc
	 *
	 * @see java.lang.reflect.InvocationHandler# invoke(java.lang.Object,
	 *      java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
		Object result = null;
		if (this.proxy.getProxied().isAssignableFrom(method.getDeclaringClass())) {
			Object[] calledParameters = null;

			if (method.isVarArgs() && parameters != null && parameters.length == 1
					&& parameters[0].getClass().isArray()) {
				calledParameters = (Object[]) parameters[0];

			} else {
				calledParameters = parameters;
			}
			result = this.proxy.invoke(method.getName().toUpperCase(), calledParameters);
		} else {
			result = method.invoke(this, parameters);
		}
		if (result == this.proxy || result == this) {
			return proxy;
		}
		return result;
	}

	/**
	 * Returns the {@link ModelElementProxy} wrapped by this ProxyWrapper
	 * 
	 * @return this ProxyWrapper's {@link ModelElementProxy}
	 */
	public M getProxy() {
		return this.proxy;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxyWrapper#
	 *      getName()
	 */
	public String getName() {
		return this.proxy.getName();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxyWrapper#
	 *      getPath()
	 */
	public String getPath() {
		return this.proxy.getPath();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxy#
	 *      addElement(org.eclipse.sensinact.gateway.common.primitive.Nameable)
	 */
	@Override
	public boolean addElement(P element) {
		return false;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxy#
	 *      removeElement(java.lang.String)
	 */
	@Override
	public P removeElement(String element) {
		return null;
	}
}
