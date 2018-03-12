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
package org.eclipse.sensinact.gateway.core.method.legacy;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.json.JSONObject;

/**
 * Extended  {@link AccessMethodResponseBuilder} dedicated to {@link GetMethod}
 * execution
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public class GetResponseBuilder extends 
AccessMethodResponseBuilder<JSONObject, GetResponse>
{
	/**
	 * @param uri 
	 * @param parameters
	 */
    public GetResponseBuilder(Mediator mediator, 
    		String uri, Object[] parameters)
    {
	    super(mediator, uri, parameters);
    }

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodResponseBuilder#
	 * createAccessMethodResponse(org.eclipse.sensinact.gateway.core.model.message.SnaMessage.Status)
	 */
    @Override
	public GetResponse createAccessMethodResponse(Status status)
    {
		return new GetResponse(super.mediator, super.getPath(), status);
    }

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder#getComponentType()
	 */
	@Override
	public Class<JSONObject> getComponentType() 
	{
		return JSONObject.class;
	}
}
