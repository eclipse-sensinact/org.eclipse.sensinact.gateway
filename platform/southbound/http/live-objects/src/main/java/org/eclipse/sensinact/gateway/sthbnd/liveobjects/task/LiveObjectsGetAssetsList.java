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
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.task;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpBrowsingTask;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsGetAssetsList extends HttpBrowsingTask<SimpleHttpResponse, SimpleHttpRequest> {
    public LiveObjectsGetAssetsList(Mediator mediator, HttpProtocolStackEndpoint transmitter) {
        super(mediator, transmitter, SimpleHttpRequest.class);
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
