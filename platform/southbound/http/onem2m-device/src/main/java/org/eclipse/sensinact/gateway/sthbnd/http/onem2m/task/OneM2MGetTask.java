/*
 * Copyright (c) 2018 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.onem2m.task;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.onem2m.internal.OneM2MHttpPacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.json.JSONObject;

public class OneM2MGetTask extends HttpTask<SimpleHttpResponse, SimpleHttpRequest> {
    public OneM2MGetTask(Mediator mediator, CommandType command, TaskTranslator transmitter, Class<SimpleHttpRequest> requestType, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        super(mediator, command, transmitter, requestType, path, profileId, resourceConfig, parameters);
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
        if (content.has("m2m:cin")) {
            super.setResult(content.getJSONObject("m2m:cin").getString("con"));
        } else {
            super.setResult(AccessMethod.EMPTY);
        }
    }
}
