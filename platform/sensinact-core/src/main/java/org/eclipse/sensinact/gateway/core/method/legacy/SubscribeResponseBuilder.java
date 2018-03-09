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
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.json.JSONObject;

/**
 * Extended  {@link AccessMethodResponseBuilder} dedicated to {@link SubscribeMethod}
 * execution
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@SuppressWarnings("serial")
public class SubscribeResponseBuilder extends 
AccessMethodResponseBuilder<JSONObject, SubscribeResponse>
{
	/**
	 * @param uri 
	 * @param parameters
	 */
    protected SubscribeResponseBuilder(Mediator mediator, 
    		String uri, Object[] parameters)
    {
	    super(mediator, uri,parameters);
    }

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodResponseBuilder#
	 * createAccessMethodResponse(org.eclipse.sensinact.gateway.core.model.message.SnaMessage.Status)
	 */
    @Override
	public SubscribeResponse createAccessMethodResponse(AccessMethodResponse.Status status)
    {
		return new SubscribeResponse(super.mediator, 
				super.getPath(), status);
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
