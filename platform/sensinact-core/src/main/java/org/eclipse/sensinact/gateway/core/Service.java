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

import java.util.Set;

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeResponse;

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
	 * @param parameters objects array parameterizing the invocation
	 * 
	 * @return the resulting {@link GetResponse} 
	 */
	GetResponse get(String resourceName, String attributeName, Object...args);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters objects array parameterizing the invocation
	 * 
	 * @return the resulting {@link SetResponse} 
	 */
	SetResponse set(String resourceName, String atributeName, Object value, Object...args);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String resourceName, String attributeName, Recipient recipient, Object...args);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String resourceName, String attributeName, Recipient recipient, Set<Constraint> conditions, Object...args);
	
	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String resourceName, String attributeName, Recipient recipient, Set<Constraint> conditions, String policy, Object...args);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters array of {@link Parameter}s parameterizing the invocation
	 * 
	 * @return the resulting {@link UnsubscribeResponse} 
	 */
	UnsubscribeResponse unsubscribe(String resourceName, String attributeName, String subscriptionId, Object...args);

}
