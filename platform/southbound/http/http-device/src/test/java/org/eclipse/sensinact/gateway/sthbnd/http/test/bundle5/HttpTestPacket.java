/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle5;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.annotation.CommandID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

public class HttpTestPacket extends HttpPacket
{
	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
	
	private String serviceProviderId;
	private String serviceId;
	private String resourceId;
	private Object data;
	private Task.CommandType command;


	public HttpTestPacket(Map<String,List<String>> headers, byte[] content) 
			throws Exception
    {
    	super(headers, content);
    	if(content != null &&  content.length>0)
    	{
    		JsonObject jo = mapper.readValue(content, JsonObject.class);
			this.serviceProviderId = jo.getString("serviceProviderId");
			this.serviceId = jo.getString("serviceId");
			this.resourceId = jo.getString("resourceId");
			this.data = jo.getInt("data");
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