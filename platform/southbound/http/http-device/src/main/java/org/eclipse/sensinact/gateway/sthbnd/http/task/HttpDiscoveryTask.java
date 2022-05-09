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
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;

/**
 * Extended {@link HttpTask} dedicated to discovery process
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class HttpDiscoveryTask<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> 
extends HttpTaskImpl<RESPONSE, REQUEST> {

    /**
     * Constructor
     *
     * @param transmitter the {@link HttpProtocolStackEndpoint} transmitting
     *                    the requests build by the HttpDiscoveryTask to instantiate
     * @param requestType the extended {@link HttpRequest} type handled
     *                    by this HttpDiscoveryConnectionConfiguration
     */
    public HttpDiscoveryTask(HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType) {
        this( CommandType.GET, transmitter, requestType, null);
    }

    /**
     * Constructor
     *

     * @param transmitter the {@link HttpProtocolStackEndpoint} transmitting
     *                    the requests build by the HttpDiscoveryTask to instantiate
     * @param requestType the extended {@link HttpRequest} type handled
     *                    by this HttpDiscoveryConnectionConfiguration
     * @param parameters   the Objects array parameterizing the remote discovery call
     */
    public HttpDiscoveryTask(HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType, Object[] parameters) {
        this(CommandType.GET, transmitter, requestType, parameters);
    }
    
    /**
     * Constructor
     *

     * @param command     the {@link CommandType} of the HttpDiscoveryTask to be
     *                    instantiated
     * @param transmitter the {@link HttpProtocolStackEndpoint} transmitting
     *                    the requests build by the HttpDiscoveryTask to instantiate
     * @param requestType the extended {@link HttpRequest} type handled
     *                    by this HttpDiscoveryConnectionConfiguration
     */
    public HttpDiscoveryTask(CommandType command, HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType) {
        this(command, transmitter, requestType, null);
    }

    /**
     * Constructor
     *

     * @param command     the {@link CommandType} of the HttpDiscoveryTask to be
     *                    instantiated
     * @param transmitter the {@link HttpProtocolStackEndpoint} transmitting
     *                    the requests build by the HttpDiscoveryTask to instantiate
     * @param requestType the extended {@link HttpRequest} type handled
     *                    by this HttpDiscoveryConnectionConfiguration
     * @param parameters   the Objects array parameterizing the remote discovery call
     */
    public HttpDiscoveryTask(CommandType command, HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType, Object[] parameters) {
        super(command, transmitter, requestType, null, null, null, parameters);
    }
}
