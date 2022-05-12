/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle4;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.annotation.CommandID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponsePacket;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

public class HttpTestPacket extends HttpResponsePacket
{
	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
	
	private String serviceProviderId;
	private String serviceId;
	private String resourceId;
	private Object data;


	public HttpTestPacket(HttpResponse response)
			throws Exception
    {
    	super(response);
    	if(content != null &&  content.length>0)
    	{
			try
			{
				JsonObject jo = mapper.readValue(content, JsonObject.class);
				this.serviceProviderId = jo.getString("serviceProviderId");
				this.serviceId = jo.getString("serviceId");
				this.resourceId = jo.getString("resourceId");
				this.data = jo.getInt("data");
				
			} catch(Exception e)
			{
				JsonObject jo = mapper.readValue(content, JsonObject.class);
				this.serviceProviderId = jo.getString("serviceProviderId");
				
				JsonArray jsonServices  = jo.getJsonArray("services");
				int index = 0;
				int length = jsonServices == null?0:jsonServices.size();
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