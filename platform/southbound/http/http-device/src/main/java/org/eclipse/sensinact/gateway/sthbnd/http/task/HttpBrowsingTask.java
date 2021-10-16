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
package org.eclipse.sensinact.gateway.sthbnd.http.task;

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
    public HttpBrowsingTask(Mediator mediator, HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType) {
        super(mediator, transmitter, requestType);
    }

    @Override
    public boolean isDirect() {
        return false;
    }
}
