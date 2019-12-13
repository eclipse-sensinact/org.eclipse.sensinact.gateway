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
package org.eclipse.sensinact.gateway.core.method;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.api.message.AbstractSnaErrorfulMessage;
import org.eclipse.sensinact.gateway.api.message.ErrorfulMessage;
import org.eclipse.sensinact.gateway.api.message.SnaMessage;
import org.eclipse.sensinact.gateway.api.message.MessageSubType;
import org.eclipse.sensinact.gateway.api.message.ResponseMessage;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.method.legacy.ActResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeJSONResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeMethod;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeStringResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;

/**
 * Extended {@link SnaMessage} dedicated to the responses to the
 * {@link AccessMethod}s invocation
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public abstract class AccessMethodResponse<T> extends AbstractSnaErrorfulMessage<AccessMethodResponse.Response>
		implements ResponseMessage<T, AccessMethodResponse.Response> {
	
	public static final int SUCCESS_CODE = ErrorfulMessage.NO_ERROR;

	public enum Response implements MessageSubType, KeysCollection {
		DESCRIBE_RESPONSE, 
		GET_RESPONSE, 
		SET_RESPONSE, 
		ACT_RESPONSE, 
		SUBSCRIBE_RESPONSE, 
		UNSUBSCRIBE_RESPONSE, 
		UNKNOWN_METHOD_RESPONSE;

		final Set<TypedKey<?>> keys;

		Response() {
			List<TypedKey<?>> list = Arrays.asList(new SnaMessage.KeysBuilder(AccessMethodResponse.class).keys());

			Set<TypedKey<?>> tmpKeys = new HashSet<TypedKey<?>>();
			tmpKeys.addAll(list);
			keys = Collections.unmodifiableSet(tmpKeys);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see MessageSubType#getSnaMessageType()
		 */
		@Override
		public SnaMessage.Type getSnaMessageType() {
			return ResponseMessage.TYPE;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see KeysCollection#keys()
		 */
		@Override
		public Set<TypedKey<?>> keys() {
			return this.keys;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see KeysCollection#key(java.lang.String)
		 */
		@Override
		public TypedKey<?> key(String key) {
			TypedKey<?> typedKey = null;

			Iterator<TypedKey<?>> iterator = this.keys.iterator();
			while (iterator.hasNext()) {
				typedKey = iterator.next();
				if (typedKey.equals(key)) {
					break;
				}
				typedKey = null;
			}
			return typedKey;
		}
	}

	/**
	 * SnaObjectMessage possible status
	 */
	public enum Status {
		ERROR, SUCCESS;
	}

	/**
	 * Error SnaMessages factory
	 * 
	 * @param mediator
	 * @param uri
	 * @param method
	 * @param statusCode
	 * @param message
	 * @param throwable
	 * @return
	 */
	public static final <T, R extends AccessMethodResponse<T>> R error(Mediator mediator, String uri,
			AccessMethod.Type type, int statusCode, String message, Throwable throwable) {
		return AccessMethodResponse.<T, R>error(mediator, uri, type.name(), statusCode, message, throwable);
	}

	/**
	 * Error {@link AccessMethodJSONResponse}s factory
	 * 
	 * @param mediator
	 * @param uri
	 * @param method
	 * @param statusCode
	 * @param message
	 * @param throwable
	 * @return
	 */
	private static final <T, R extends AccessMethodResponse<T>> R error(Mediator mediator, String uri, String method,
			int statusCode, String message, Throwable throwable) {
		int code = statusCode == AccessMethodResponse.SUCCESS_CODE ? ErrorfulMessage.UNKNOWN_ERROR_CODE : statusCode;

		AccessMethodResponse<?> response = null;
		switch (method) {
		case "ACT":
			response = new ActResponse(mediator, uri, AccessMethodResponse.Status.ERROR, code);
			break;
		case "GET":
			response = new GetResponse(mediator, uri, AccessMethodResponse.Status.ERROR, code);
			break;
		case "SET":
			response = new SetResponse(mediator, uri, AccessMethodResponse.Status.ERROR, code);
			break;
		case "SUBSCRIBE":
			response = new SubscribeResponse(mediator, uri, AccessMethodResponse.Status.ERROR, code);
			break;
		case "UNSUBSCRIBE":
			response = new UnsubscribeResponse(mediator, uri, AccessMethodResponse.Status.ERROR, code);
			break;
		default:
			try {
				DescribeType t = DescribeType.valueOf(method);
				response = error(mediator, uri, t, statusCode, message, throwable);

			} catch (Exception e) {
				response = new UnknownAccessMethodResponse(mediator, uri);
			}
			break;
		}
		if (response != null && message != null) {
			response.setErrors(message, throwable);
		}
		return (R) response;
	}

	/**
	 * Error SnaMessages factory
	 * 
	 * @param mediator
	 * @param uri
	 * @param method
	 * @param statusCode
	 * @param message
	 * @param throwable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T, R extends DescribeResponse<T>> R error(Mediator mediator, String uri,
			DescribeType describeType, int statusCode, String message, Throwable throwable) {
		int code = statusCode == AccessMethodResponse.SUCCESS_CODE ? ErrorfulMessage.UNKNOWN_ERROR_CODE : statusCode;

		DescribeMethod.DescribeType type = describeType == null ? DescribeMethod.DescribeType.COMPLETE_LIST
				: describeType;

		DescribeResponse<?> response = null;
		switch (type) {
		case COMPLETE_LIST:
		case PROVIDERS_LIST:
		case SERVICES_LIST:
		case RESOURCES_LIST:
			response = new DescribeStringResponse(mediator, uri, Status.ERROR, code, type);
			break;
		case PROVIDER:
		case SERVICE:
		case RESOURCE:
			response = new DescribeJSONResponse(mediator, uri, Status.ERROR, code, type);
			break;
		default:
			break;
		}
		if (response != null && message != null) {
			response.setErrors(message, throwable);
		}
		return (R) response;
	}

	/**
	 * this SnaMessage status
	 */
	protected final Status status;

	/**
	 * @param uri
	 * @param type
	 * @param status
	 */
	protected AccessMethodResponse(Mediator mediator, String uri, Response type, Status status) {
		this(mediator, uri, type, status,
				(status == Status.SUCCESS) ? AccessMethodResponse.SUCCESS_CODE : UNKNOWN_ERROR_CODE);
	}

	/**
	 * @param uri
	 * @param type
	 * @param status
	 */
	protected AccessMethodResponse(Mediator mediator, String uri, Response type, Status status, int statusCode) {
		super(mediator, uri, type);
		this.status = status;
		super.putValue(SnaConstants.STATUS_CODE_KEY, statusCode);
	}

	/**
	 * @param resultObject
	 */
	public void setResponse(T resultObject) {
		if (resultObject == null) {
			return;
		}
		super.putValue(SnaConstants.RESPONSE_KEY, resultObject);
	}

	/**
	 * 
	 * @see org.eclipse.sensinact.gateway.api.message.ResponseMessage#getResponse()
	 */
	public T getResponse() {
		return super.<T>get(SnaConstants.RESPONSE_KEY);
	}

	/**
	 * Returns the state of this message
	 * 
	 * @return
	 */
	public Status getStatus() {
		return this.status;
	}

	/**
	 * Returns the code of this SnaMessage's status
	 * 
	 * @return the code of this SnaMessage's {@link Status}
	 */
	public int getStatusCode() {
		return super.<Integer>get(SnaConstants.STATUS_CODE_KEY);
	}

	/**
	 * Defines this SnaMessage's status code
	 * 
	 * @param statusCode
	 *            the status code to set
	 * @return the status code of this SnaMessage's status
	 */
	public int setStatusCode(int statusCode) {
		if (this.status == Status.ERROR) {
			super.putValue(SnaConstants.STATUS_CODE_KEY, statusCode);
		}
		return super.<Integer>get(SnaConstants.STATUS_CODE_KEY);
	}
}
