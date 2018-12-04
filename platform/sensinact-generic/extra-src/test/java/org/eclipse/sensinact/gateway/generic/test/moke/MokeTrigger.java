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
package org.eclipse.sensinact.gateway.generic.test.moke;

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
