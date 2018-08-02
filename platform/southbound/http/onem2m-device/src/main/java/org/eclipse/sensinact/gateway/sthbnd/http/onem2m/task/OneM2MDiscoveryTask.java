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
package org.eclipse.sensinact.gateway.sthbnd.http.onem2m.task;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpRequest;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpBrowsingTask;

import java.util.UUID;

public class OneM2MDiscoveryTask extends HttpBrowsingTask<SimpleHttpResponse, SimpleHttpRequest> {
    public OneM2MDiscoveryTask(Mediator mediator, HttpProtocolStackEndpoint transmitter) {
        super(mediator, transmitter, SimpleHttpRequest.class);
    }

    @Override
    public String getUri() {
        String host = (String) mediator.getProperty("http.onem2m.host");
        String port = (String) mediator.getProperty("http.onem2m.port");
        String cseBase = (String) mediator.getProperty("http.onem2m.cse.base");
        //Filtering the resourceType: 2 (application entity), 3 (container), 4 (contentInstance)
        String filter = "?ty=3&fu=1";
        super.addHeader("X-M2M-RI", UUID.randomUUID().toString());
        super.addHeader("X-M2M-Origin", "SOrigin");
        return "http://" + host + ":" + port + "/" + cseBase + filter;
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
