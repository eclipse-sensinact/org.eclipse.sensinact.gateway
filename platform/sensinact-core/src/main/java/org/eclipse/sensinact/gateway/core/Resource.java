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

import java.util.Set;

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.Typable;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;

/**
 * A Resource owns {@link Attribute}s and {@link AccessMethod}s whose call allow
 * to manipulate those {@link Attribute}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Resource extends ElementsProxy<AttributeDescription>, Typable<Resource.Type> {
	// Type attribute property key
	static final String TYPE_PROPERTY = "TYPE_VALUE";

	// default attribute name property key
	static final String ATTRIBUTE_DEFAULT_PROPERTY = "ATTRIBUTE_DEFAULT";

	// default attribute name
	static final String ATTRIBUTE_DEFAULT = null;

	// Name attribute name
	static final String NAME = "name";

	// Type attribute name
	static final String TYPE = "type";

	static final AttributeBuilder[] ATTRIBUTES = new AttributeBuilder[] {
			// the name attribute is hidden and cannot be modified
			// value should be defined
			new AttributeBuilder(NAME,
					new AttributeBuilder.Requirement[] { AttributeBuilder.Requirement.MODIFIABLE,
							AttributeBuilder.Requirement.HIDDEN, AttributeBuilder.Requirement.VALUE,
							AttributeBuilder.Requirement.TYPE }).type(String.class).hidden(true)
									.modifiable(Modifiable.FIXED),
			// the type attribute is hidden and cannot be modified
			// value should be defined
			new AttributeBuilder(TYPE,
					new AttributeBuilder.Requirement[] { AttributeBuilder.Requirement.MODIFIABLE,
							AttributeBuilder.Requirement.HIDDEN, AttributeBuilder.Requirement.VALUE,
							AttributeBuilder.Requirement.TYPE }).type(Resource.Type.class).hidden(true)
									.modifiable(Modifiable.FIXED) };

	/**
	 * Resource type enumeration
	 */
	public static enum Type {
		ACTION, PROPERTY, SENSOR, STATE_VARIABLE;
	}

	/**
	 * - NONE means that the resource has to update its value every time it is asked
	 * for giving it - AUTO means that the resource is updated automatically and has
	 * to returned its current value when it is asked for giving it - INIT means
	 * that the resource has to update its value the first time it is asked for
	 * giving it, and that it will be automatically updated after
	 */
	public static enum UpdatePolicy {
		NONE, AUTO, INIT;
	}

	/**
	 * Asks for this Resource's associated get execution
	 * 
	 * @param parameters
	 *            objects array parameterizing the invocation
	 * @return the invocation {@link SnaMessage} result
	 */
	GetResponse get(String attributeName);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            objects array parameterizing the invocation
	 * @return the invocation {@link SnaMessage} result
	 */
	SetResponse set(String atributeName, Object value);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String attributeName, Recipient recipient);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String attributeName, Recipient recipient, Set<Constraint> conditions);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	SubscribeResponse subscribe(String attributeName, Recipient recipient, Set<Constraint> conditions, String policy);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param parameters
	 *            array of {@link Parameter}s parameterizing the invocation
	 * @return the {@link SnaMessage} resulting of the invocation
	 */
	UnsubscribeResponse unsubscribe(String attributeName, String subscriptionId);
}
