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
