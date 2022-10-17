/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.task;

import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpBrowsingTask;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsListLoraDevices extends HttpBrowsingTask<SimpleHttpResponse, SimpleHttpRequest> {
    public LiveObjectsListLoraDevices(HttpProtocolStackEndpoint transmitter) {
        super(transmitter, SimpleHttpRequest.class);
    }

    @Override
    public String getUri() {
        String uri = LiveObjectsConstant.ROOT_URL + LiveObjectsConstant.ROOT_PATH + "vendors/lora/devices";
        return uri;
    }

    @Override
    public String getHttpMethod() {
        return HttpConnectionConfiguration.GET;
    }

    @Override
    public String getAccept() {
        return "application/json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
