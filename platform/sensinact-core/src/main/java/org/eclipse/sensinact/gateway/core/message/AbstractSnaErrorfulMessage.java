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
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

/**
 * Abstract implementation of an {@link AbstractSnaErrorfulMessage}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSnaErrorfulMessage<S extends Enum<S> & KeysCollection & SnaMessageSubType>
		extends AbstractSnaMessage<S> implements SnaErrorfulMessage<S> {
	/**
	 * Constructor
	 * 
	 * @param uri
	 * @param type
	 */
	protected AbstractSnaErrorfulMessage(String uri, S type) {
		super(uri, type);
	}

	/**
	 * @InheritedDoc
	 *
	 * @see SnaErrorfulMessage#setErrors(org.json.JSONArray)
	 */
	@Override
	public void setErrors(JsonArray errorsArray) {
		int length = 0;

		if (errorsArray == null || (length = errorsArray.size()) == 0) {
			return;
		}
		JsonArrayBuilder errors = JsonProviderFactory.getProvider().createArrayBuilder(getErrors());
		int index = 0;
		for (; index < length; index++) {
			errors.add(errorsArray.get(index));
		}
		super.putValue(SnaConstants.ERRORS_KEY, errors.build());
	}

	/**
	 *
	 * @param message
	 *            the thrown error message
	 * @param exception
	 *            the thrown exception
	 */
	protected void setErrors(String message, Throwable exception) {
		JsonArrayBuilder exceptionsArray = JsonProviderFactory.getProvider().createArrayBuilder();

		JsonObjectBuilder exceptionObject = JsonProviderFactory.getProvider().createObjectBuilder();
		exceptionObject.add("message", message == null ? exception.getMessage() : message);

		StringBuilder buffer = new StringBuilder();
		if (exception != null) {
			StackTraceElement[] trace = exception.getStackTrace();

			int index = 0;
			int length = trace.length;

			for (; index < length; index++) {
				buffer.append(trace[index].toString());
				buffer.append("\n");
			}
		}
		exceptionObject.add("trace", buffer.toString());
		exceptionsArray.add(exceptionObject);

		this.setErrors(exceptionsArray.build());
	}

	/**
	 * @inheritDoc
	 * 
	 * @see SnaErrorfulMessage# setErrors(java.lang.Exception)
	 */
	@Override
	public void setErrors(Exception exception) {
		this.setErrors(null, exception);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see SnaErrorfulMessage#getErrors()
	 */
	@Override
	public JsonArray getErrors() {
		JsonArray jsonArray  = super.<JsonArray>get(SnaConstants.ERRORS_KEY);
		return jsonArray == null ? JsonArray.EMPTY_JSON_ARRAY : jsonArray;
	}
}
