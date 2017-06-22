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

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle3;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpTestPacket extends HttpPacket
{
	private String serviceProviderId;
	private String serviceId;
	private String resourceId;
	private Object data;
	private Task.CommandType command;


	public HttpTestPacket(Map<String,List<String>> headers, byte[] content) 
			throws JSONException
    {
    	super(headers, content);
    	if(content != null &&  content.length>0)
    	{
			JSONArray json = new JSONArray(new String(content));
			
			JSONObject object = json.getJSONObject(2);
			JSONArray array = object.optJSONArray("resourceId");			
			this.resourceId = array.getString(0);
			this.data = array.get(1);
			
			object = json.getJSONObject(1);
			this.serviceId = object.getString("serviceId");
			
			object = json.getJSONObject(0);
			object = object.getJSONObject("serviceProviderId");			
			this.serviceProviderId = object.getString("serviceProviderId");
			
			this.command = Task.CommandType.GET;
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
	public Object getCommand()
	{
		return this.command;
	}
}