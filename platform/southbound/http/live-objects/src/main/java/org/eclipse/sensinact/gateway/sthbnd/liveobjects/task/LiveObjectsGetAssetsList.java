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

import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpBrowsingTask;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsGetAssetsList extends HttpBrowsingTask<SimpleHttpResponse, SimpleHttpRequest> {
    public LiveObjectsGetAssetsList(HttpProtocolStackEndpoint transmitter) {
        super(transmitter, SimpleHttpRequest.class);
    }

    @Override
    public String getUri() {
        return LiveObjectsConstant.ROOT_URL + LiveObjectsConstant.ROOT_PATH + "assets";
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
