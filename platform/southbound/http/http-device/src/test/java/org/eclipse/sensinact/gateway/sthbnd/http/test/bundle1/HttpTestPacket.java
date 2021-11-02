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

package org.eclipse.sensinact.gateway.sthbnd.http.test.bundle1;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponsePacket;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpTestPacket extends HttpResponsePacket {
	private String serviceProviderId;
	private String serviceId;
	private String resourceId;
	private Object data;

	public HttpTestPacket(HttpResponse response) throws JSONException {
    	super(response);    	
    	byte[] c = super.content;
    	if(c != null &&  c.length>0) {
    		String str = new String(c);    		
			JSONObject json = new JSONObject(str);
			this.serviceProviderId = json.getString("serviceProviderId");
			this.serviceId = json.getString("serviceId");
			this.resourceId = json.getString("resourceId");
			this.data = json.get("data");
    	}
	}
	
	@ServiceProviderID
	public String getServiceProviderId() {
		return this.serviceProviderId;
	}

	@ServiceID
	public String getServiceId() {
		return this.serviceId;
	}

	@ResourceID
	public String getResourceId() {
		return this.resourceId;
	}
	
	@Data
	public Object getData() {
		return this.data;
	}

	@CommandID
	public Task.CommandType getCommand() {
		return super.command;
	}
}