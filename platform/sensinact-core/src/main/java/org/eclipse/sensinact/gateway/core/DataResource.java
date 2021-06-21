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
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeResponse;

/**
 * Extended {@link Resource} holding a 'value' attribute
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface DataResource extends Resource {
	// Value attribute name
	static final String VALUE = "value";

	// default attribute name
	static final String ATTRIBUTE_DEFAULT = VALUE;

	static final AttributeBuilder[] ATTRIBUTES = new AttributeBuilder[] {
			// value attribute is not hidden
			// type, value and modifiable has to be defined
			new AttributeBuilder(VALUE, new AttributeBuilder.Requirement[] { AttributeBuilder.Requirement.MODIFIABLE,
					AttributeBuilder.Requirement.HIDDEN, AttributeBuilder.Requirement.TYPE }) };

	// Constant name is the concatenation the name of the targeted
	// AttributeBuilder's field and of the associated requirement
	// constant value
	public static final boolean DEFAULT_VALUE_HIDDEN = false;

	/**
	 * Executes an get access method on this DataResource and returns its 
	 * {@link GetResponse}. The read attribute is the one defined as being
	 * the default attribute of this DataResource (the "value" attribute by default).
	 * 
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the {@link GetResponse} of the executed get access method
	 */
	GetResponse get(Object...args);

	/**
	 * Executes an set access method on this DataResource and returns its 
	 * {@link SetResponse}. The modified attribute is the one defined as being
	 * the default attribute of this DataResource (the "value" attribute by default).
	 * 
	 * @param value the Object to set as value of the default attribute
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the {@link SetResponse} of the executed set access method
	 */
	SetResponse set(Object value, Object...args);

	/**
	 * Executes an subscribe access method on this DataResource and returns its 
	 * {@link SubscribeResponse}. The observed attribute is the one defined as being
	 * the default attribute of this DataResource (the "value" attribute by default).
	 * By default the only {@link Constraint} applying on the subscription will concern 
	 * the "changed status" of the observed attribute, and by default the associated 
	 *  {@link org.eclipse.sensinact.gateway.common.execution.ErrorHandler}'s policy 
	 *  defines that an error during a notification transmission conducts to the 
	 *  subscription closing and to the thrown exception logging
	 * 
	 * @param recipient the {@link Recipient} to which the messages will be sent
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the {@link SubscribeResponse} of the executed subscribe access method
	 */
	SubscribeResponse subscribe(Recipient recipient, Object...args);

	/**
	 * Executes an subscribe access method on this DataResource and returns its 
	 * {@link SubscribeResponse}. The observed attribute is the one defined as being
	 * the default attribute of this DataResource (the "value" attribute by default).
	 * By default the associated  {@link org.eclipse.sensinact.gateway.common.execution.ErrorHandler}'s 
	 * policy defines that an error during a notification transmission conducts to the 
	 * subscription closing and to the thrown exception logging
	 * 
	 * @param recipient the {@link Recipient} to which the messages will be sent
	 * @param conditions the {@link Set} of {@link Constraint}s that will help at
	 * discriminating the messages to be transmitted to the recipient
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the {@link SubscribeResponse} of the executed subscribe access method
	 */
	SubscribeResponse subscribe(Recipient recipient, Set<Constraint> conditions, Object...args);

	/**
	 * Executes an subscribe access method on this DataResource and returns its 
	 * {@link SubscribeResponse}. The observed attribute is the one defined as being
	 * the default attribute of this DataResource (the "value" attribute by default).
	 * 
	 * @param recipient the {@link Recipient} to which the messages will be sent
	 * @param conditions the {@link Set} of {@link Constraint}s that will help at
	 * discriminating the messages to be transmitted to the recipient   
	 * @param policy the String representation of the policy that will apply on the 
	 * {@link org.eclipse.sensinact.gateway.common.execution.ErrorHandler} of the 
	 * subscription to be created.
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the {@link SubscribeResponse} of the executed subscribe access method
	 */
	SubscribeResponse subscribe(Recipient recipient, Set<Constraint> conditions, String policy, Object...args);

	/**
	 * Executes an unsubscribe access method on this DataResource and returns its 
	 * {@link UnsubscribeResponse}. It closes the subscription whose String identifier 
	 * is passed as parameter.
	 * 
	 * @param subscriptionId the String identifier of the subscription to close
	 * @param args optional variable Objects array parameterizing the call
	 * 
	 * @return the {@link UnsubscribeResponse} of the executed unsubscribe access method
	 */
	UnsubscribeResponse unsubscribe(String subscriptionId, Object...args);
}
