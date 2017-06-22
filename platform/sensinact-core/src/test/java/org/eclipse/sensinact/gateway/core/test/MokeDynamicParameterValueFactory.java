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
package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.builder.DynamicParameterValueFactory;

/**
 * 
 */
public class MokeDynamicParameterValueFactory implements DynamicParameterValueFactory
{

	/**
	 * @InheritedDoc
	 *
	 * @see AccessMethodTriggerFactory#handle(java.lang.String)
	 */
	@Override
	public boolean handle(String type)
	{
		return "VARIABLE_PARAMETER_BUILDER".equals(type);
	}


	/**
	 * @InheritedDoc
	 *
	 * @see DynamicParameterValueFactory#newInstance(org.eclipse.sensinact.gateway.util.mediator.AbstractMediator, org.eclipse.sensinact.gateway.core.model.ServiceImpl, java.lang.String, org.json.JSONObject)
	 */
    @Override
    public DynamicParameterValue newInstance(Mediator mediator,
            Executable<Void,Object> resourceValueExtractor, JSONObject builder)
            throws InvalidValueException
    {
		String resourceName = builder.optString(DynamicParameterValue.BUILDER_RESOURCE_KEY);
		String parameterName = builder.optString(DynamicParameterValue.BUILDER_PARAMETER_KEY);
		MokeDynamicParameterValue moke =  new MokeDynamicParameterValue(mediator, parameterName, resourceName);
	    moke.setResourceValueExtractor(resourceValueExtractor);
		return moke;
    }

}
