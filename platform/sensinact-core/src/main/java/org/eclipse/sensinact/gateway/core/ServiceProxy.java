/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.ServiceAccessMethod;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.UnaccessibleAccessMethod;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;

/**
 * A {@link Service} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceProxy extends ModelElementProxy {
	/**
	 * {@link AccessMethod}s of this ModelElementProxy
	 */
	protected final Map<String, AccessMethod> methods;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param path
	 * @throws InvalidServiceException
	 */
	ServiceProxy(Mediator mediator, String path) throws InvalidServiceException {
		super(mediator, Service.class, path);
		this.methods = new HashMap<String, AccessMethod>();
	}

	/**
	 * @inheritDoc
	 *
	 * @see SensiNactResourceModelElementProxy# getAccessMethod(AccessMethod.Type)
	 */
	@Override
	public AccessMethod getAccessMethod(String method) {
		return this.methods.get(method);
	}

	/**
	 * Registers the {@link AccessMethod} passed as parameter, mapped to the
	 * specified {@link AccessMethod.Type}
	 * 
	 * @param method
	 *            the {@link AccessMethod.Type} of the {@link AccessMethod} to
	 *            register
	 * @param method
	 *            the {@link AccessMethod} to register
	 */
	void registerMethod(String methodType, AccessMethod method) {
		if (this.methods.get(methodType) == null) {
			this.methods.put(methodType, method);
		}
	}

	/**
	 * Builds the set of access methods of this service proxy
	 * 
	 * @throws InvalidValueException
	 */
	final void buildMethods(ErrorHandler handler, List<MethodAccessibility> methodAccessibilities)
			throws InvalidValueException {
		int index = 0;
		int length = methodAccessibilities == null ? 0 : methodAccessibilities.size();

		// Get method
		ServiceAccessMethod getMethod = null;
		// Set method
		ServiceAccessMethod setMethod = null;
		// Subscribe method
		ServiceAccessMethod subscribeMethod = null;
		// Unsubscribe method
		ServiceAccessMethod unsubscribeMethod = null;
		// Act method
		ServiceAccessMethod actMethod = null;

		for (; index < length; index++) {
			MethodAccessibility methodAccessibility = methodAccessibilities.get(index);

			if (!methodAccessibility.isAccessible()) {
				continue;
			}
			switch (methodAccessibility.getMethod().name()) {
			case "ACT":
				actMethod = new ServiceAccessMethod(mediator, getPath(), AccessMethod.Type.valueOf(AccessMethod.ACT),
						handler);
				break;
			case "DESCRIBE":
				break;
			case "GET":
				getMethod = new ServiceAccessMethod(mediator, getPath(), AccessMethod.Type.valueOf(AccessMethod.GET),
						handler);
				break;
			case "SET":
				setMethod = new ServiceAccessMethod(mediator, getPath(), AccessMethod.Type.valueOf(AccessMethod.SET),
						handler);
				break;
			case "SUBSCRIBE":
				subscribeMethod = new ServiceAccessMethod(mediator, getPath(),
						AccessMethod.Type.valueOf(AccessMethod.SUBSCRIBE), handler);
				break;
			case "UNSUBSCRIBE":
				unsubscribeMethod = new ServiceAccessMethod(mediator, getPath(),
						AccessMethod.Type.valueOf(AccessMethod.UNSUBSCRIBE), handler);
				break;
			default:
				break;
			}
		}
		AccessMethod.Type GET = AccessMethod.Type.valueOf(AccessMethod.GET);
		AccessMethod.Type SET = AccessMethod.Type.valueOf(AccessMethod.SET);
		AccessMethod.Type ACT = AccessMethod.Type.valueOf(AccessMethod.ACT);
		AccessMethod.Type SUBSCRIBE = AccessMethod.Type.valueOf(AccessMethod.SUBSCRIBE);
		AccessMethod.Type UNSUBSCRIBE = AccessMethod.Type.valueOf(AccessMethod.UNSUBSCRIBE);
		AccessMethod.Type DESCRIBE = AccessMethod.Type.valueOf(AccessMethod.DESCRIBE);

		Signature getSignature = new Signature(mediator, GET, new Class<?>[] { String.class, String.class },
				new String[] { "resourceName", "attributeName" });

		Signature setSignature = new Signature(mediator, SET,
				new Class<?>[] { String.class, String.class, Object.class },
				new String[] { "resourceName", "attributeName", "value" });

		Signature actSignature = new Signature(mediator, ACT, new Class<?>[] { String.class, Object[].class },
				new String[] { "resourceName", "arguments" });

		Signature subscribeSignature = new Signature(mediator, SUBSCRIBE,
				new Class<?>[] { String.class, String.class, Recipient.class, Set.class },
				new String[] { "resourceName", "attributeName", "listener", "conditions" });

		Signature unsubscribeSignature = new Signature(mediator, UNSUBSCRIBE,
				new Class<?>[] { String.class, String.class, String.class },
				new String[] { "resourceName", "attributeName", "subscriptionId" });

		// Get method
		if (getMethod != null) {
			getMethod.addSignature(getSignature);
			Signature getAttributeShortcut = new Signature(mediator, GET, new Class<?>[] { String.class },
					new String[] { "resourceName" });

			getMethod.addSignature(getAttributeShortcut);
			registerMethod(AccessMethod.GET, getMethod);

		} else {
			AccessMethod snaMethod = new UnaccessibleAccessMethod(mediator, getPath(),
					AccessMethod.Type.valueOf(AccessMethod.GET));
			registerMethod(AccessMethod.GET, snaMethod);
		}
		// Set method
		if (setMethod != null) {
			setMethod.addSignature(setSignature);
			Signature setAttributeShortcut = new Signature(mediator, SET, new Class<?>[] { String.class, Object.class },
					new String[] { "resourceName", "value" });
			setMethod.addSignature(setAttributeShortcut);
			registerMethod(AccessMethod.SET, setMethod);

		} else {
			AccessMethod snaMethod = new UnaccessibleAccessMethod(mediator, getPath(),
					AccessMethod.Type.valueOf(AccessMethod.SET));
			registerMethod(AccessMethod.SET, snaMethod);
		}
		// Set method
		if (actMethod != null) {
			actMethod.addSignature(actSignature);
			registerMethod(AccessMethod.ACT, actMethod);

		} else {
			AccessMethod snaMethod = new UnaccessibleAccessMethod(mediator, getPath(),
					AccessMethod.Type.valueOf(AccessMethod.ACT));
			registerMethod(AccessMethod.ACT, snaMethod);
		}
		// Subscribe method
		if (subscribeMethod != null) {
			subscribeMethod.addSignature(subscribeSignature);

			Signature subscribeConditionsShortcut = new Signature(mediator, SUBSCRIBE,
					new Class<?>[] { String.class, String.class, Recipient.class },
					new String[] { "resourceName", "attributeName", "listener" });

			subscribeMethod.addSignature(subscribeConditionsShortcut);

			Signature subscribeNameConditionsShortcut = new Signature(mediator, SUBSCRIBE,
					new Class<?>[] { String.class, Recipient.class }, new String[] { "resourceName", "listener" });

			Signature subscribeNameShortcut = new Signature(mediator, SUBSCRIBE,
					new Class<?>[] { String.class, Recipient.class, Set.class },
					new String[] { "resourceName", "listener", "conditions" });

			subscribeMethod.addSignature(subscribeNameConditionsShortcut);
			subscribeMethod.addSignature(subscribeNameShortcut);
			registerMethod(AccessMethod.SUBSCRIBE, subscribeMethod);

		} else {
			AccessMethod snaMethod = new UnaccessibleAccessMethod(mediator, getPath(),
					AccessMethod.Type.valueOf(AccessMethod.SUBSCRIBE));
			registerMethod(AccessMethod.SUBSCRIBE, snaMethod);
		}
		// Unsubscribe method
		if (unsubscribeMethod != null) {
			unsubscribeMethod.addSignature(unsubscribeSignature);
			Signature unsubscribeNameShortcut = new Signature(mediator, UNSUBSCRIBE,
					new Class<?>[] { String.class, String.class }, new String[] { "resourceName", "subscriptionId" });
			unsubscribeMethod.addSignature(unsubscribeNameShortcut);
			registerMethod(AccessMethod.UNSUBSCRIBE, unsubscribeMethod);

		} else {
			AccessMethod snaMethod = new UnaccessibleAccessMethod(mediator, getPath(),
					AccessMethod.Type.valueOf(AccessMethod.UNSUBSCRIBE));
			registerMethod(AccessMethod.UNSUBSCRIBE, snaMethod);
		}
	}
}
