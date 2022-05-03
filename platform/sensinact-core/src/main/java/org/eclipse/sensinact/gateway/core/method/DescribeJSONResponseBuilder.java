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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONObject;

/**
 * Extended {@link AccessMethodResponseBuilder} dedicated to
 * {@link DescribeMethod} returning a JSONObject response
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public class DescribeJSONResponseBuilder extends DescribeResponseBuilder<JSONObject> {
	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the DescribeJSONResponseBuilder to
	 *            be instantiated to interact with the OSGi host environment
	 * @param uri
	 *            the String uri of the model element targeted by the related
	 *            {@link DescribeMethod}
	 * @param describeType
	 *            the sub-describe type configuring the {@link DescribeResponse} to
	 *            be created by the DescribeJSONResponseBuilder to be instantiated:
	 *            <ul>
	 *            <li>PROVIDER</li>
	 *            <li>SERVICE</li>
	 *            <li>RESOURCE</li>
	 *            </ul>
	 */
	protected DescribeJSONResponseBuilder(Mediator mediator, String uri, DescribeMethod.DescribeType describeType) {
		super(mediator, uri, describeType);
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodResponseBuilder#
	 *      createAccessMethodResponse(org.eclipse.sensinact.gateway.core.model.message.SnaMessage.Status)
	 */
	@Override
	public DescribeJSONResponse createAccessMethodResponse(AccessMethodResponse.Status status) {
		return new DescribeJSONResponse(super.getPath(), status, describeType);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder#getComponentType()
	 */
	@Override
	public Class<JSONObject> getComponentType() {
		return JSONObject.class;
	}
}
