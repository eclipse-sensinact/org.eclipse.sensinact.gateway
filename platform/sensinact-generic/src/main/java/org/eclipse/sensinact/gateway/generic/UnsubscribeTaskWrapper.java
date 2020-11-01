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
