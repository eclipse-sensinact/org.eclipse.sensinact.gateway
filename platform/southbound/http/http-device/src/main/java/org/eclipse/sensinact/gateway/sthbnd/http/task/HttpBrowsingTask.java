/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.task;

import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;

/**
 * Extended {@link HttpTask} dedicated to discovery process
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpBrowsingTask<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> 
extends HttpDiscoveryTask<RESPONSE, REQUEST> {
    /**
     * Constructor
     *
     * @param mediator    the {@link Mediator} allowing to interact with
     *                    the OSGi host environment
     * @param transmitter the {@link HttpProtocolStackEndpoint} transmitting
     *                    the requests build by the HttpDiscoveryTask to instantiate
     * @param requestType the extended {@link HttpRequest} type handled
     *                    by this HttpDiscoveryConnectionConfiguration
     */
    public HttpBrowsingTask(HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType) {
        super(transmitter, requestType);
    }

    @Override
    public boolean isDirect() {
        return false;
    }
}
