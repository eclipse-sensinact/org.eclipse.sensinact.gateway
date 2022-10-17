/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

/**
 * Extended {@link AccessMethodResponseBuilder} dedicated to
 * {@link DescribeMethod} returning a String response
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public class DescribeStringResponseBuilder extends DescribeResponseBuilder<String> {
	private static final String DEFAULT_DELIMITER = ",";

	private String delimiter;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the DescribeStringResponseBuilder to
	 *            be instantiated to interact with the OSGi host environment
	 * @param uri
	 *            the String uri of the model element targeted by the related
	 *            {@link DescribeMethod}
	 * @param describeType
	 *            the sub-describe type configuring the {@link DescribeResponse} to
	 *            be created by the DescribeStringResponseBuilder to be
	 *            instantiated:
	 *            <ul>
	 *            <li>COMPLETE_LIST</li>
	 *            <li>PROVIDERS_LIST</li>
	 *            <li>SERVICES_LIST</li>
	 *            <li>RESOURCES_LIST</li>
	 *            </ul>
	 * @param delimiter
	 *            the String delimiter used to concatenate the multiple String
	 *            responses
	 */
	protected DescribeStringResponseBuilder(Mediator mediator, String uri, DescribeMethod.DescribeType describeType,
			String delimiter) {
		super(mediator, uri, describeType);
		this.delimiter = delimiter;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder#getComponentType()
	 */
	@Override
	public Class<String> getComponentType() {
		return String.class;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder#
	 *      createAccessMethodResponse(org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status)
	 */
	@Override
	public DescribeStringResponse createAccessMethodResponse(AccessMethodResponse.Status status) {
		DescribeStringResponse response = new DescribeStringResponse(super.getPath(), status,
				describeType);

		JsonProvider provider = JsonProviderFactory.getProvider();
		if (exceptions != null && exceptions.size() > 0) {
			Iterator<Exception> iterator = exceptions.iterator();
			JsonArrayBuilder exceptionsArray = provider.createArrayBuilder();

			while (iterator.hasNext()) {
				Exception exception = iterator.next();
				JsonObjectBuilder exceptionObject = provider.createObjectBuilder();
				exceptionObject.add("message", exception.getMessage());

				StringBuilder buffer = new StringBuilder();
				StackTraceElement[] trace = exception.getStackTrace();

				int index = 0;
				int length = trace.length;

				for (; index < length; index++) {
					buffer.append(trace[index].toString());
					buffer.append("\n");
				}
				exceptionObject.add("trace", buffer.toString());
				exceptionsArray.add(exceptionObject);
			}
			response.setErrors(exceptionsArray.build());
		}
		response.setResponse(getAccessMethodObjectResult());
		return response;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder#
	 *      createAccessMethodResponse()
	 */
	public DescribeStringResponse createAccessMethodResponse() {
		AccessMethodResponse.Status status = (exceptions != null && exceptions.size() > 0)
				? AccessMethodResponse.Status.ERROR
				: AccessMethodResponse.Status.SUCCESS;
		return this.createAccessMethodResponse(status);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder#
	 *      setAccessMethodObjectResult(java.lang.Object)
	 */
	public void setAccessMethodObjectResult(String resultObject) {
		super.push(resultObject);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder#
	 *      getAccessMethodObjectResult()
	 */
	public String getAccessMethodObjectResult() {
		int index = 0;
		StringBuilder builder = new StringBuilder();
		Enumeration<String> enumeration = super.elements();
		while (enumeration.hasMoreElements()) {
			if (index > 0) {
				builder.append(this.delimiter == null ? DEFAULT_DELIMITER : this.delimiter);
			}
			builder.append(enumeration.nextElement());
			index++;
		}
		return builder.toString();
	}

}
