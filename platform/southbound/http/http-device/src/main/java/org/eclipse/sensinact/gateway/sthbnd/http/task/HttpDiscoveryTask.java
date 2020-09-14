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
public abstract class HttpDiscoveryTask<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> 
extends HttpTaskImpl<RESPONSE, REQUEST> {

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
    public HttpDiscoveryTask(Mediator mediator, HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType) {
        this(mediator, CommandType.GET, transmitter, requestType, null);
    }

    /**
     * Constructor
     *
     * @param mediator    the {@link Mediator} allowing to interact with
     *                    the OSGi host environment
     * @param transmitter the {@link HttpProtocolStackEndpoint} transmitting
     *                    the requests build by the HttpDiscoveryTask to instantiate
     * @param requestType the extended {@link HttpRequest} type handled
     *                    by this HttpDiscoveryConnectionConfiguration
     * @param parameters   the Objects array parameterizing the remote discovery call
     */
    public HttpDiscoveryTask(Mediator mediator, HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType, Object[] parameters) {
        this(mediator, CommandType.GET, transmitter, requestType, parameters);
    }
    
    /**
     * Constructor
     *
     * @param mediator    the {@link Mediator} allowing to interact with
     *                    the OSGi host environment
     * @param command     the {@link CommandType} of the HttpDiscoveryTask to be
     *                    instantiated
     * @param transmitter the {@link HttpProtocolStackEndpoint} transmitting
     *                    the requests build by the HttpDiscoveryTask to instantiate
     * @param requestType the extended {@link HttpRequest} type handled
     *                    by this HttpDiscoveryConnectionConfiguration
     */
    public HttpDiscoveryTask(Mediator mediator, CommandType command, HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType) {
        this(mediator, command, transmitter, requestType, null);
    }

    /**
     * Constructor
     *
     * @param mediator    the {@link Mediator} allowing to interact with
     *                    the OSGi host environment
     * @param command     the {@link CommandType} of the HttpDiscoveryTask to be
     *                    instantiated
     * @param transmitter the {@link HttpProtocolStackEndpoint} transmitting
     *                    the requests build by the HttpDiscoveryTask to instantiate
     * @param requestType the extended {@link HttpRequest} type handled
     *                    by this HttpDiscoveryConnectionConfiguration
     * @param parameters   the Objects array parameterizing the remote discovery call
     */
    public HttpDiscoveryTask(Mediator mediator, CommandType command, HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType, Object[] parameters) {
        super(mediator, command, transmitter, requestType, null, null, null, parameters);
    }
}
