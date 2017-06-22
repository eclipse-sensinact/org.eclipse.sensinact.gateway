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
package org.eclipse.sensinact.gateway.nthbnd.endpoint.format;

import org.json.JSONObject;

import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;

public class StringResponseFormat implements ResponseFormat<String>
{
	@Override
	public String format(Object object) 
	{
		if(object == null)
		{
			return null;
		}
		if(JSONObject.class.isAssignableFrom(object.getClass()))
		{
			return ((JSONObject)object).toString();
		}
		if(AccessMethodResponse.class.isAssignableFrom(object.getClass()))
		{
			return ((AccessMethodResponse)object).getJSON();
		}
		return object.toString();
	}
}
