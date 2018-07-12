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
package org.eclipse.sensinact.gateway.generic.test.moke3;

import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;

/**
 *
 */
public class MokeTrigger implements AccessMethodTrigger<AccessMethodResponseBuilder> {
    /**
     * @InheritedDoc
     * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
     */
    @Override
    public Object execute(AccessMethodResponseBuilder parameter) throws Exception {
        return 0.2f;
    }

    /**
     * @InheritedDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        return "{\"index\":0,\"passOn\":false,\"type\":\"VARIATIONTEST_TRIGGER\"}";
    }

    /**
     * @InheritedDoc
     * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger#getParameters()
     */
    @Override
    public Parameters getParameters() {
        return Parameters.INTERMEDIATE;
    }

    /**
     * @InheritedDoc
     * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger#getName()
     */
    @Override
    public String getName() {
        return "VARIATIONTEST_TRIGGER";
    }

    /**
     * @InheritedDoc
     * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger#passOn()
     */
    @Override
    public boolean passOn() {
        return false;
    }
}
