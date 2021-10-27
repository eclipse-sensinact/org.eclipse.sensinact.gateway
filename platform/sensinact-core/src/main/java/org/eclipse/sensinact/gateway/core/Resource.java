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
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeResponse;

/**
 * A Resource owns {@link Attribute}s and {@link AccessMethod}s whose call allow
 * to manipulate those {@link Attribute}s
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
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
	 * @param atributeName the String name of the targeted attribute
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the resulting {@link GetResponse}
	 */
	GetResponse get(String attributeName, Object...args);

	/**
	 * Asks for this Resource's associated set execution
	 * 
	 * @param atributeName the String name of the targeted attribute
	 * @param value the value Object to be set
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the resulting {@link SetResponse}
	 */
	SetResponse set(String atributeName, Object value, Object...args);

	/**
	 * Asks for this Resource's associated subscription execution
	 * 
	 * @param atributeName the String name of the targeted attribute
	 * @param recipient the {@link Recipient} of the subscription to be created
	 * @param args optional variable Objects array parameterizing the call
	 *
	 * @return the resulting {@link SubscribeResponse}
	 */
	SubscribeResponse subscribe(String attributeName, Recipient recipient, Object...args);

	/**
	 * Asks for this Resource's associated subscription execution
	 * 
	 * @param atributeName the String name of the targeted attribute
	 * @param recipient the {@link Recipient} of the subscription to be created
	 * @param conditions Set of {@link Constraint}s applying on the subscription to be created
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the resulting {@link SubscribeResponse}
	 */
	SubscribeResponse subscribe(String attributeName, Recipient recipient, Set<Constraint> conditions, Object...args);

	/**
	 * Asks for this Resource's associated subscription execution
	 * 
	 * @param atributeName the String name of the targeted attribute
	 * @param recipient the {@link Recipient} of the subscription to be created
	 * @param conditions Set of {@link Constraint}s applying on the subscription to be created
	 * @param policy the String definition of the error policy applying on the subscription to be created
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the resulting {@link SubscribeResponse}
	 */
	SubscribeResponse subscribe(String attributeName, Recipient recipient, Set<Constraint> conditions, String policy, Object...args);

	/**
	 * Asks for this Resource's associated unsubscription execution
	 * 
	 * @param atributeName the String name of the targeted attribute
	 * @param subscriptionId the String identifier of the subscription to be deleted
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the resulting {@link UnsubscribeResponse}
	 */
	UnsubscribeResponse unsubscribe(String attributeName, String subscriptionId, Object...args);
}
