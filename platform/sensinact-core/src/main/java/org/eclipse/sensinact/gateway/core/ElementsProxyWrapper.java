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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * Abstract extended {@link ElementsProxy} implementation
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public abstract class ElementsProxyWrapper<M extends ModelElementProxy, P extends Nameable>
		implements ElementsProxy<P>, InvocationHandler {
	/**
	 * the proxied class
	 */
	protected final M proxy;

	/**
	 * Constructor
	 * 
	 * @param proxy the extended {@link ModelElementProxy} to be wrapped by the
	 * ElementsProxyWrapper to be instantiated
	 */
	protected ElementsProxyWrapper(M proxy) {
		this.proxy = proxy;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
		Object result = null;
		if (this.proxy.getProxied().isAssignableFrom(method.getDeclaringClass())) {
			Object[] calledParameters = null;
			if(method.isVarArgs()) {
				if(parameters[parameters.length-1]==null || Array.getLength(parameters[parameters.length-1]) == 0
					|| (Array.getLength(parameters[parameters.length-1]) == 1 && Array.get(parameters[parameters.length-1], 0)==null)) {
					int length = method.getParameterCount() -1 ;
					calledParameters = new Object[length];
					if(length > 0)
						System.arraycopy(parameters, 0, calledParameters, 0, length);
				} else if(parameters.length == 1) 
					calledParameters = (Object[]) parameters[0];
				else {
					int length = Array.getLength(parameters[parameters.length-1]);
					length+=(parameters.length-1);
					calledParameters = new Object[length];
					System.arraycopy(parameters, 0, calledParameters, 0, parameters.length-1);
					System.arraycopy(parameters[parameters.length-1],0,calledParameters, parameters.length-1,Array.getLength(parameters[parameters.length-1]));
				}
			} else 
				calledParameters = parameters;			
			result = this.proxy.invoke(method.getName().toUpperCase(), calledParameters);
		} else 
			result = method.invoke(this, parameters);
		
		if (result == this.proxy || result == this) 
			return proxy;
		
		return result;
	}

	/**
	 * Returns the {@link ModelElementProxy} wrapped by this ElementsProxyWrapper
	 * 
	 * @return this ElementsProxyWrapper's {@link ModelElementProxy}
	 */
	public M getProxy() {
		return this.proxy;
	}

	@Override
	public String getName() {
		return this.proxy.getName();
	}

	@Override
	public String getPath() {
		return this.proxy.getPath();
	}

	@Override
	public boolean addElement(P element) {
		return false;
	}

	@Override
	public P removeElement(String element) {
		return null;
	}
}
