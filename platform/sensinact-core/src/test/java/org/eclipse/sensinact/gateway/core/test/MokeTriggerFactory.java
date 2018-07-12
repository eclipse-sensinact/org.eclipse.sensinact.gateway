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
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory;
import org.json.JSONObject;

/**
 *
 */
public class MokeTriggerFactory implements AccessMethodTriggerFactory {
    /**
     * @InheritedDoc
     * @see AccessMethodTriggerFactory#handle(java.lang.String)
     */
    @Override
    public boolean handle(String type) {
        return "VARIATIONTEST_TRIGGER".equals(type);
    }

    /**
     * @InheritedDoc
     * @see AccessMethodTriggerFactory#newInstance(org.eclipse.sensinact.gateway.util.mediator.AbstractMediator, org.json.JSONObject)
     */
    @Override
    public <P> AccessMethodTrigger<P> newInstance(Mediator mediator, JSONObject trigger) throws InvalidValueException {
        return (AccessMethodTrigger<P>) new MokeTrigger();
    }
}
