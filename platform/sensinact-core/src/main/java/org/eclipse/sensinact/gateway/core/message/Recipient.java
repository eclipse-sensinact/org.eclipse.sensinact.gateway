/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * A callback recipient
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Recipient extends JSONable {
	static final String RECIPIENT_JSONSCHEMA = "";

	/**
	 * Transmits the array of recorded {@link SnaMessage} to the recipient
	 * 
	 * @param callbackId
	 *            the {@link MidCallback}'s identifier from which the array of
	 *            {@link SnaMessage}s come
	 * @param messages
	 *            the array of recorded {@link SnaMessage} to transmit
	 * @throws Exception
	 *             If an error occurred while transmitting the messages array
	 */
	void callback(String callbackId, SnaMessage<?>[] messages) throws Exception;

}
