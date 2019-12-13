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

import org.eclipse.sensinact.gateway.api.message.LifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.api.message.UpdateMessage.Update;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedProperties;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Abstract implementation of an {@link AbstractSnaMessage}
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class AbstractSnaMessage<S extends Enum<S> & KeysCollection & MessageSubType>
		extends TypedProperties<S> implements SnaMessage<S> {
	
	public static SnaMessage<?> fromJSON(final Mediator mediator, String json) {
		final JSONObject jsonMessage = new JSONObject(json);
		final String typeStr = (String) jsonMessage.remove("type");
		final String uri = (String) jsonMessage.remove("uri");
		if (typeStr == null) {
			return null;
		}
		SnaMessage<?> message = null;
		
		switch(typeStr) {
			case "PROVIDER_APPEARING":
			case "PROVIDER_DISAPPEARING":
			case "SERVICE_APPEARING":
			case "SERVICE_DISAPPEARING":
			case "RESOURCE_APPEARING":
			case "RESOURCE_DISAPPEARING":
				Lifecycle l = Lifecycle.valueOf(typeStr);
				message = new LifecycleMessageImpl(mediator, uri, l);
				break;
			case "ATTRIBUTE_VALUE_UPDATED":
			case "METADATA_VALUE_UPDATED":
			case "ACTUATED":
				Update u = Update.valueOf(typeStr);
				message = new UpdateMessageImpl(mediator, uri, u);
				break;
			case "CONNECTED":
			case "DISCONNECTED":
				RemoteMessage.Remote r = RemoteMessage.Remote.valueOf(typeStr);
				message = new RemoteMessageImpl(mediator, uri, r);
				break;
			case "NO_ERROR" :
			case "UPDATE_ERROR": 
			case "RESPONSE_ERROR":
			case "LIFECYCLE_ERROR": 
			case "SYSTEM_ERROR":
				ErrorMessage.Error e = ErrorMessage.Error.valueOf(typeStr);
				message = new ErrorMessageImpl(mediator, uri, e);
			default:
				break;
		}
		if (message != null) {
			JSONArray names = jsonMessage.names();
			int index = 0;
			int length = names == null ? 0 : names.length();
			for (; index < length; index++) {
				String name = names.getString(index);
				((TypedProperties<?>) message).put(name, jsonMessage.get(name));
			}
		}
		return message;
	}

	/**
	 * Constructor
	 * 
	 * @param uri
	 * @param type
	 */
	protected AbstractSnaMessage(Mediator mediator, String uri, S type) {
		super(mediator, type);
		super.putValue(SnaConstants.URI_KEY, uri);

	}

	/**
	 * @inheritDoc
	 *
	 * @see PathElement#getPath()
	 */
	public String getPath() {
		return super.<String>get(SnaConstants.URI_KEY);
	}

	/**
	 * Returns the {@link SnaMessage.Type} to which this extended
	 * {@link SnaMessage}'s type belongs to
	 */
	public SnaMessage.Type getSnaMessageType() {
		S type = super.getType();
		return type.getSnaMessageType();
	}

}
