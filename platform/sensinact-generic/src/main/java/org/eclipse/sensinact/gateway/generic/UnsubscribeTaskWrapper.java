/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.execution.Executable;

/**
 *
 */
public interface UnsubscribeTaskWrapper extends TaskWrapper {

	/**
	 * {@link Executable} in charge of extracting the subscriber identifier
	 * from the wrapped {@link Task}
	 */
	Executable<Task, String> targetIdExtractor();
	
	void setSubscriptionId(String subscriptionId) ;

	String getTargetId();
}
