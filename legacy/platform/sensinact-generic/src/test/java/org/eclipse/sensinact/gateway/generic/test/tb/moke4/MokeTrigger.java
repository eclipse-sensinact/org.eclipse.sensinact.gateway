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

import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.trigger.AbstractAccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.TriggerArgumentBuilder;

/**
 *
 */
public class MokeTrigger extends AbstractAccessMethodTrigger {
	
    protected MokeTrigger() {
		super(null, TriggerArgumentBuilder.INTERMEDIATE, false);
	}

	/**
     * @InheritedDoc
     * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
     */
    @Override
    public Object execute(Object parameter) throws Exception {
    	if(!AccessMethodResponseBuilder.class.isAssignableFrom(parameter.getClass()) ) {
    		throw new IllegalArgumentException("AccessMethodResponseBuilder expected");
    	}
        return 0.2f;
    }
    
    /**
     * @InheritedDoc
     * @see AccessMethodTrigger#getName()
     */
    @Override
    public String getName() {
        return "VARIATIONTEST_TRIGGER";
    }

	@Override
	public String doGetJSON() {
		return null;
	}
}
