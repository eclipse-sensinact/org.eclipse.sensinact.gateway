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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;

public class JSONResponseFormat implements ResponseFormat<JSONObject> {

	private Mediator mediator;
	
	public JSONResponseFormat(Mediator mediator)
	{
		this.mediator = mediator;
	}

	@Override
	public JSONObject format(Object object) 
	{
		if(object == null)
		{
			return null;
		}
		if(JSONObject.class.isAssignableFrom(object.getClass()))
		{
			return (JSONObject) object;
		}
		if(JSONable.class.isAssignableFrom(object.getClass()))
		{
			return new JSONObject(((JSONable)object).getJSON());
		}
		String json = JSONUtils.toJSONFormat(object);
		try
		{
			return CastUtils.cast(
				this.mediator.getClassLoader(), 
					JSONObject.class, json);
			
		} catch(ClassCastException e)
		{
			return new JSONObject().put("response", json);
		}
	}
}
