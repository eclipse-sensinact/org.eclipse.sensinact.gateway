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

import java.util.Set;

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;

/**
 * This interface represents devices (called smart object services) and higher
 * level services (called smart services).
 * 
 * It mainly defines the methods to access the resources (ie., properties,
 * sensor data, state variables and actions) exposed by smart object services
 * and smart services.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Service extends ElementsProxy<Resource>, ResourceCollection {
	/**
	 * The service id resource name.
	 */
	public static final String SERVICE_ID = "id";

	/**
	 * The service type resource name.
	 */
	public static final String SERVICE_TYPE = "type";

	/**
	 * Asks for this Resource's associated get execution
	 * 
	 * @param parameters
	 *            objects array parameterizing the invocation
	 * @return the invocation {@link SnaMessage} result
	 */
	GetResponse get(String resourceName, String attributeName);

	/**
	 * Asks for this Resource's associated get execution
	 * 
	 * @param parameters
	 *            objects array parameterizing the invocation
	 * @return the invocation {@link SnaMessage} result
	 */
	GetResponse get(String resourceName);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            objects array parameterizing the invocation
	 * @return the invocation {@link SnaMessage} result
	 */
	SetResponse set(String resourceName, String atributeName, Object value);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            objects array parameterizing the invocation
	 * @return the invocation {@link SnaMessage} result
	 */
	SetResponse set(String resourceName, Object value);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String resourceName, String attributeName, Recipient recipient);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String resourceName, Recipient recipient);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String resourceName, String attributeName, Recipient recipient,
			Set<Constraint> conditions);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String resourceName, Recipient recipient, Set<Constraint> conditions);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String resourceName, Recipient recipient, Set<Constraint> conditions, long lifetime);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	UnsubscribeResponse unsubscribe(String resourceName, String attributeName, String subscriptionId);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	UnsubscribeResponse unsubscribe(String resourceName, String subscriptionId);
}
