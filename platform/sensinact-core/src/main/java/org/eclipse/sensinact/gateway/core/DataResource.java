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

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;

import java.util.Set;

/**
 * Extended {@link Resource} holding a 'value' attribute
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface DataResource extends Resource {
    // Value attribute name
    static final String VALUE = "value";
    // default attribute name
    static final String ATTRIBUTE_DEFAULT = VALUE;
    static final AttributeBuilder[] ATTRIBUTES = new AttributeBuilder[]{
            // value attribute is not hidden
            // type, value and modifiable has to be defined
            new AttributeBuilder(VALUE, new AttributeBuilder.Requirement[]{AttributeBuilder.Requirement.MODIFIABLE, AttributeBuilder.Requirement.HIDDEN, AttributeBuilder.Requirement.TYPE})};

    // Constant name is the concatenation the name of the targeted
    // AttributeBuilder's field and of the associated requirement
    // constant value
    public static final boolean DEFAULT_VALUE_HIDDEN = false;

    /**
     * Asks for this Resource's associated get
     * execution
     *
     * @param parameters objects array parameterizing
     *                   the invocation
     * @return the invocation {@link SnaMessage} result
     */
    GetResponse get();

    /**
     * Asks for this Resource's associated set
     * execution
     *
     * @param parameters objects array parameterizing the invocation
     * @return the invocation {@link SnaMessage} result
     */
    SetResponse set(Object value);

    /**
     * Asks for this Resource's associated set execution
     *
     * @param parameters array of {@link Parameter}s parameterizing the invocation
     * @return the {@link SnaMessage} resulting of the invocation
     */
    SubscribeResponse subscribe(Recipient recipient);

    /**
     * Asks for this Resource's associated set execution
     *
     * @param parameters array of {@link Parameter}s parameterizing the invocation
     * @return the {@link SnaMessage} resulting of the invocation
     */
    SubscribeResponse subscribe(Recipient recipient, Set<Constraint> conditions);

    /**
     * Asks for this Resource's associated set execution
     *
     * @param parameters array of {@link Parameter}s parameterizing the invocation
     * @return the {@link SnaMessage} resulting of the invocation
     */
    UnsubscribeResponse unsubscribe(String subscriptionId);
}
