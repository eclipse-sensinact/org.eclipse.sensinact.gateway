/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
