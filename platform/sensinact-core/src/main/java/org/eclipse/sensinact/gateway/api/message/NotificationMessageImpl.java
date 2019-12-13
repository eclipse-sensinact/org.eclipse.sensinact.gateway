/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.api.message;

import org.json.JSONObject;
import org.eclipse.sensinact.gateway.api.message.LifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.api.message.RemoteMessage.Remote;
import org.eclipse.sensinact.gateway.api.message.UpdateMessage.Update;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.util.CastUtils;

/**
 * 
 * @param <S>
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class NotificationMessageImpl<S extends Enum<S> & MessageSubType & KeysCollection>
		extends AbstractSnaMessage<S> implements NotificationMessage<S> {
	/**
	 * Notification SnaMessages factory
	 */
	public static final class Builder {
		@SuppressWarnings("unchecked")
		public static final <N extends NotificationMessage<?>> N notification(Mediator mediator,
				MessageSubType notificationType, String uri) {
			N notification = null;
			switch (notificationType.getSnaMessageType()) {
			case ERROR:
				break;
			case LIFECYCLE:
				notification = (N) new LifecycleMessageImpl(mediator, uri,(Lifecycle) notificationType);
				break;
			case RESPONSE:
				break;
			case UPDATE:
				notification = (N) new UpdateMessageImpl(mediator, uri, (Update) notificationType);
				break;
			case REMOTE:
				notification = (N) new RemoteMessageImpl(mediator, uri, (Remote) notificationType);
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
	protected NotificationMessageImpl(Mediator mediator, String uri, S type) {
		super(mediator, uri, type);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see NotificationMessage# setNotification(org.json.JSONObject)
	 */
	public void setNotification(JSONObject jsonObject) {
		if (JSONObject.NULL.equals(jsonObject)) {
			return;
		}
		super.putValue(SnaConstants.NOTIFICATION_KEY, jsonObject);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see NotificationMessage# getNotification()
	 */
	public JSONObject getNotification() {
		JSONObject jsonObject = super.<JSONObject>get(SnaConstants.NOTIFICATION_KEY);
		return jsonObject;
	}

	/**
	 * Returns the object value associated to the string key passed as parameter in
	 * the notification {@link JSONObject} embedded in this message
	 * 
	 * @param key
	 *            the string key for which to retrieve the object value
	 * @return the object value associated to the specified key in the notification
	 *         {@link JSONObject}
	 */
	public Object getNotification(String key) {
		Object value = null;
		JSONObject jsonObject = getNotification();

		if (jsonObject != null) {
			value = jsonObject.opt(key);
		}
		return value;
	}

	/**
	 * Returns the object value associated to the string key passed as parameter ,
	 * in the notification {@link JSONObject} embedded in this message, casted into
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
		return CastUtils.cast(super.mediator.getClassLoader(), type, getNotification(key));
	}
}
