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
import org.json.JSONObject;

public class OneM2MGetTask extends HttpTaskImpl<SimpleHttpResponse, SimpleHttpRequest> {
    
	private Mediator mediator;

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
        JSONObject content = new JSONObject(new String((byte[]) result));
        if (content.has("m2m:cin"))
            super.setResult(content.getJSONObject("m2m:cin").getString("con"));
        else
            super.setResult(AccessMethod.EMPTY);
    }
}
