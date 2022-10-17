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

/**
 * Extended notification message dedicated to update events
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SnaRemoteMessageImpl extends SnaNotificationMessageImpl<SnaRemoteMessage.Remote>
		implements SnaRemoteMessage {

	/**
	 * Constructor
	 * 
	 * @param uri
	 * @param type
	 */
	public SnaRemoteMessageImpl(String uri, Remote type) {
		super(uri, type);
	}
}
