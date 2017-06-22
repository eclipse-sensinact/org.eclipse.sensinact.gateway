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

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle4;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponsePacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpTestPacket extends HttpResponsePacket
{
	private String serviceProviderId;
	private String serviceId;
	private String resourceId;
	private Object data;


	public HttpTestPacket(HttpResponse response)
			throws JSONException
    {
    	super(response);
    	if(content != null &&  content.length>0)
    	{
			try
			{
				JSONObject json = new JSONObject(new String(content));
				this.serviceProviderId = json.getString("serviceProviderId");
				this.serviceId = json.getString("serviceId");
				this.resourceId = json.getString("resourceId");
				this.data = json.get("data");
				
			} catch(JSONException e)
			{
				JSONObject json = new JSONObject(new String(content));
				this.serviceProviderId = json.getString("serviceProviderId");
				JSONArray jsonServices  = json.getJSONArray("services");
				int index = 0;
				int length = jsonServices == null?0:jsonServices.length();
				String[] services = new String[length];
				for(;index < length; index++)
				{
					services[index] = jsonServices.getString(index);
				}
				this.data = services;
				super.command = Task.CommandType.SERVICES_ENUMERATION;
			}
    	}
	}
	
	@ServiceProviderID
	public String getServiceProviderId()
	{
		return this.serviceProviderId;
	}

	@ServiceID
	public String getServiceId()
	{
		return this.serviceId;
	}

	@ResourceID
	public String getResourceId()
	{
		return this.resourceId;
	}
	
	@Data
	public Object getData()
	{
		return this.data;
	}

	@CommandID
	public Task.CommandType getCommand()
	{
		return super.command;
	}
}