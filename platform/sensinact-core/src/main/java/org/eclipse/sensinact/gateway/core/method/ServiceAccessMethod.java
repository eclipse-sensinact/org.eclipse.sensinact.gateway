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
package org.eclipse.sensinact.gateway.core.method;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ResourceImpl.ResourceProxyWrapper;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;

/**
 * Extended {@link AccessMethod} dedicated to Services
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceAccessMethod implements AccessMethod {
	private final Mediator mediator;

	protected final String uri;
	private final AccessMethod.Type type;
	private final List<Signature> signatures;
	private ErrorHandler handler;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the {@link AccessMethod.Type} of the extended {@link AccessMethod}
	 *            type to instantiate
	 */
	public ServiceAccessMethod(Mediator mediator, String uri, Type type, ErrorHandler handler) {
		this.uri = uri;
		this.type = type;
		this.mediator = mediator;
		this.handler = handler;
		this.signatures = new ArrayList<Signature>();
	}

	/**
	 * Returns the {@link Signature} of this {@link ServiceAccessMethod} whose
	 * parameter types are the same, in order, as the ones specified in the array
	 * argument
	 * 
	 * @param parameterTypes
	 *            the array of types of the searched {@link Signature}
	 * @return the {@link Signature} of this method whose parameter types are the
	 *         same as the specified ones
	 */
	protected Signature getSignature(Class<?>[] parameterTypes) {
		Iterator<Signature> iterator = this.signatures.iterator();

		while (iterator.hasNext()) {
			Signature signature = iterator.next();
			if (signature.equals(this.type.name(), parameterTypes == null ? new Class<?>[0] : parameterTypes)) {
				return signature;
			}
		}
		return null;
	}

	/**
	 * Returns the {@link Signature} of this {@link ServiceAccessMethod} whose
	 * parameter types are the same, in order, as the ones specified in the array
	 * argument
	 * 
	 * @param parameterTypes
	 *            the array of types of the searched {@link Signature}
	 * @return the {@link Signature} of this method whose parameter types are the
	 *         same as the specified ones
	 */
	protected Signature getSignature(Signature signature) {
		Signature invokerSignature = null;

		if (signature != null) {
			Iterator<Signature> iterator = this.signatures.iterator();
			while (iterator.hasNext()) {
				invokerSignature = iterator.next();
				if (signature == invokerSignature || invokerSignature.equals(signature)) {
					break;
				}
				invokerSignature = null;
			}
		}
		return invokerSignature;
	}

	/**
	 * Adds the {@link Signature} argument to this ServiceAccessMethod
	 * 
	 * @param signature
	 *            the {@link Signature} to add
	 */
	public void addSignature(Signature signature) {
		if (signature == null || signature.getName().intern() != this.type.name().intern()
				|| this.getSignature(signature) != null) {
			return;
		}
		this.signatures.add(signature);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethod#invoke(java.lang.Object[])
	 */
	@Override
	public AccessMethodResponse<?> invoke(Object[] parameters) {
		try {
			Resource resource = (Resource) parameters[0];
			Object[] subparameters = getParameters(parameters);

			ResourceImpl.ResourceProxyWrapper resourceProxy = (ResourceProxyWrapper) Proxy
					.getInvocationHandler(resource);

			return resourceProxy.getProxy().invoke(this.getType().name(), subparameters);

		} catch (Throwable e) {
			return AccessMethodResponse.error(this.mediator, uri, this.getType(),
					SnaErrorfulMessage.INTERNAL_SERVER_ERROR_CODE, e.getMessage(), e);
		}
	}

	/**
	 * Returns the type of this ServiceAccessMethod's associated
	 * {@link AccessMethod}
	 * 
	 * @return the associated {@link AccessMethod}'s type
	 */
	public AccessMethod.Type getType() {
		return this.type;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Nameable#getName()
	 */
	@Override
	public String getName() {
		return this.type.name();
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethod#getSignatures()
	 */
	@Override
	public Set<Signature> getSignatures() {
		return Collections.<Signature>unmodifiableSet(new HashSet(this.signatures));
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethod#size()
	 */
	@Override
	public int size() {
		return this.signatures.size();
	}

	/**
	 * Builds and returns the objects array parameterizing an invocation of an
	 * {@link AccessMethod}
	 * 
	 * @param parameters
	 *            the initial objects array parameters from which to build the one
	 *            used to invoke an {@link AccessMethod}
	 * @return the objects array parameterizing the associated {@link AccessMethod}
	 *         invocation
	 */
	private final Object[] getParameters(Object[] parameters) {
		int length = parameters.length - 1;
		Object[] subParameters = new Object[length];
		if (length > 0) {
			System.arraycopy(parameters, 1, subParameters, 0, length);
		}
		return subParameters;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see PathElement#getPath()
	 */
	@Override
	public String getPath() {
		return this.uri;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethod#
	 *      getErrorHandler()
	 */
	@Override
	public ErrorHandler getErrorHandler() {
		return this.handler;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.Describable#
	 *      getDescription()
	 */
	@Override
	public AccessMethodDescription getDescription() {
		return new AccessMethodDescription(this);
	}
}
