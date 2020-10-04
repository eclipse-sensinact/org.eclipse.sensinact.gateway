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
package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.method.trigger.AbstractAccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;

/** 
 * 
 */
public class MokeTrigger extends  AbstractAccessMethodTrigger {
	
	protected MokeTrigger() {
		super(null, "EMPTY", false);
	}

	/**
	 * @InheritedDoc
	 *
	 * @see Executable#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object v) throws Exception {
		return 0.2f;
	}

	/**
	 * @return
	 */
	@Override
	public String doGetJSON() {
		return null;
	} 
	
	/**
	 * @InheritedDoc
	 *
	 * @see AccessMethodTrigger#getName()
	 */
	@Override
	public String getName() {
		return "VARIATIONTEST_TRIGGER";
	}
}
