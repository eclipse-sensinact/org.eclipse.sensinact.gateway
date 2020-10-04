/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle2;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
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
			JSONObject json = new JSONObject(new String(content));
			this.serviceProviderId = json.getString("serviceProviderId");
			this.serviceId = json.getString("serviceId");
			this.resourceId = json.getString("resourceId");
			this.data = json.get("data");
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