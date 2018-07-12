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
package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.builder.AbstractDynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;

/**
 *
 */
public class MokeDynamicParameterValue extends AbstractDynamicParameterValue {

    /**
     * @param mediator
     * @param parameterName
     * @param resourceName
     */
    protected MokeDynamicParameterValue(Mediator mediator, String parameterName, String resourceName) {
        super(mediator, parameterName, resourceName);
    }

    /**
     * @InheritedDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        return "{\"resource\":\"fake\",\"parameter\":\"fake\",\"type\":\"VARIABLE_PARAMETER_BUILDER\"}";
    }

    /**
     * @InheritedDoc
     * @see AccessMethodTrigger#getName()
     */
    @Override
    public String getName() {
        return "VARIABLE_PARAMETER_BUILDER";
    }

    /**
     * @InheritedDoc
     * @see DynamicParameterValue#getValue()
     */
    @Override
    public Object getValue() {
        return (Float) (new Float((Integer) this.getResourceValue()).floatValue() / 100.0f);
    }
}
