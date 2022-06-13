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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaRemoteMessage.Remote;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessage.Update;
import org.eclipse.sensinact.gateway.util.CastUtils;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * 
 * @param <S>
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class SnaNotificationMessageImpl<S extends Enum<S> & SnaMessageSubType & KeysCollection>
		extends AbstractSnaMessage<S> implements SnaNotificationMessage<S> {
	/**
	 * Notification SnaMessages factory
	 */
	public static final class Builder {
		@SuppressWarnings("unchecked")
		public static final <N extends SnaNotificationMessage<?>> N notification(Mediator mediator,
				SnaMessageSubType notificationType, String uri) {
			N notification = null;
			switch (notificationType.getSnaMessageType()) {
			case ERROR:
				break;
			case LIFECYCLE:
				notification = (N) new SnaLifecycleMessageImpl(uri,(Lifecycle) notificationType);
				break;
			case RESPONSE:
				break;
			case UPDATE:
				notification = (N) new SnaUpdateMessageImpl(uri, (Update) notificationType);
				break;
			case REMOTE:
				notification = (N) new SnaRemoteMessageImpl(uri, (Remote) notificationType);
				break;
			default:
				break;
			}
			return notification;
		}
	}

	/**
	 * Constructor
	 */
	protected SnaNotificationMessageImpl(String uri, S type) {
		super(uri, type);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see SnaNotificationMessage# setNotification(jakarta.json.JSONObject)
	 */
	public void setNotification(JsonObject jsonObject) {
		if (jsonObject == null) {
			return;
		}
		super.putValue(SnaConstants.NOTIFICATION_KEY, jsonObject);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see SnaNotificationMessage# getNotification()
	 */
	public JsonObject getNotification() {
		JsonObject jsonObject = super.<JsonObject>get(SnaConstants.NOTIFICATION_KEY);
		return jsonObject;
	}

	/**
	 * Returns the object value associated to the string key passed as parameter in
	 * the notification {@link JsonObject} embedded in this message
	 * 
	 * @param key
	 *            the string key for which to retrieve the object value
	 * @return the object value associated to the specified key in the notification
	 *         {@link JSONObject}
	 */
	public JsonValue getNotification(String key) {
		JsonValue value = null;
		JsonObject jsonObject = getNotification();

		if (jsonObject != null) {
			value = jsonObject.get(key);
		}
		return value;
	}

	/**
	 * Returns the object value associated to the string key passed as parameter ,
	 * in the notification {@link JsonObject} embedded in this message, casted into
	 * the type also passed as parameter
	 * 
	 * @param type
	 *            the type into which to cast the object value mapped to the
	 *            specified key
	 * @param key
	 *            the string key for which to retrieve the object value
	 * @return the casted object value associated to the specified key in the
	 *         notification {@link JSONObject}
	 */
	public <T> T getNotification(Class<T> type, String key) {
		return CastUtils.cast(type, getNotification(key));
	}
}
