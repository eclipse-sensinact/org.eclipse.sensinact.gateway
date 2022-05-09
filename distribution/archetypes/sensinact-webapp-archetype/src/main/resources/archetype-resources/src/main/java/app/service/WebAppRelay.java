/*********************************************************************
* Copyright (c) 2020 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package ${package}.app.service;

import org.eclipse.sensinact.gateway.core.message.SnaMessage;

/**
 * A WebAppRelay is in charge of relaying received {@link SnaMessage}
 */
public interface WebAppRelay {

	/**
	 * Relays the received {@link SnaMessage}
	 * 
	 * @param message the {@link SnaMessage} to be relayed
	 */
	void relay(SnaMessage<?> message);
}
