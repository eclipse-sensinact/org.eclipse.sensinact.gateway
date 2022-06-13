/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.moke4;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory;

import aQute.bnd.annotation.Resolution;
import aQute.bnd.annotation.spi.ServiceProvider;
import jakarta.json.JsonObject;

/**
 *
 */
@ServiceProvider(value=AccessMethodTriggerFactory.class,resolution = Resolution.OPTIONAL)
public class MokeTriggerFactory implements AccessMethodTriggerFactory {
    /**
     * @InheritedDoc
     * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory#handle(java.lang.String)
     */
    @Override
    public boolean handle(String type) {
        return "VARIATIONTEST_TRIGGER".equals(type);
    }

    /**
     * @InheritedDoc
     * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory#newInstance(org.eclipse.sensinact.gateway.common.bundle.Mediator, jakarta.json.JsonObject)
     */
    @Override
    public AccessMethodTrigger newInstance(Mediator mediator, JsonObject trigger) throws InvalidValueException {
        return new MokeTrigger();
    }
}
