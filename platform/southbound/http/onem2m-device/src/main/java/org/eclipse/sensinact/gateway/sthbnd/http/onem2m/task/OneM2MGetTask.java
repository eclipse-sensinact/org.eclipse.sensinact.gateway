/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.onem2m.task;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.onem2m.internal.OneM2MHttpPacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTaskImpl;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

public class OneM2MGetTask extends HttpTaskImpl<SimpleHttpResponse, SimpleHttpRequest> {
    
	private Mediator mediator;
	
	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();

	public OneM2MGetTask(Mediator mediator, CommandType command, TaskTranslator transmitter, 
		Class<SimpleHttpRequest> requestType, String path, String profileId, 
		ResourceConfig resourceConfig, Object[] parameters) {
        super(command, transmitter, requestType, path, profileId, 
        		resourceConfig, parameters);
        this.mediator=mediator;
    }

    @Override
    public String getUri() {
        String host = (String) mediator.getProperty("http.onem2m.host");
        String port = (String) mediator.getProperty("http.onem2m.port");
        String cseBase = (String) mediator.getProperty("http.onem2m.cse.base");
        String[] path = super.getPath().split("/");
        String uri;
        if (OneM2MHttpPacketReader.DEFAULT_SERVICE_NAME.equalsIgnoreCase(path[2])) {
            uri = "http://" + host + ":" + port + "/" + cseBase + "/" + path[1] + "/" + path[3] + "/latest";
        } else {
            uri = "http://" + host + ":" + port + "/" + cseBase + "/" + path[1] + "/" + path[2] + "/" + path[3] + "/latest";
        }
        return uri;
    }

    @Override
    public void setResult(Object result) {
        JsonObject content;
		try {
			content = mapper.readValue((byte[]) result, JsonObject.class);
		} catch (Exception e) {
			e.printStackTrace();
			super.setResult(AccessMethod.EMPTY);
			return;
		}
        if (content.containsKey("m2m:cin"))
            super.setResult(content.getJsonObject("m2m:cin").getString("con"));
        else
            super.setResult(AccessMethod.EMPTY);
    }
}
