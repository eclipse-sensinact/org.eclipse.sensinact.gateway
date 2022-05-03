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

import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.json.JSONObject;

/**
 * @param <S>
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaNotificationMessage<S extends Enum<S> & SnaMessageSubType & KeysCollection> extends SnaMessage<S> {
	
	public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[] {
		new TypedKey<JSONObject>(SnaConstants.NOTIFICATION_KEY, JSONObject.class, false) };

	/**
	 * Returns the {@link JSONObject} notification's content
	 * 
	 * @return the {@link JSONObject} notification's content
	 */
	JSONObject getNotification();

	/**
	 * Defines the {@link JSONObject} notification's content
	 * 
	 * @param jsonObject
	 *            the {@link JSONObject} notification's content
	 */
	void setNotification(JSONObject jsonObject);
}
