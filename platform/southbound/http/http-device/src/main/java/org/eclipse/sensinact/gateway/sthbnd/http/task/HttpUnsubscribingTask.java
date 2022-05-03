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

import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;

/**
 * Extended {@link HttpTask} dedicated to discovery process
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpUnsubscribingTask<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> 
extends HttpDiscoveryTask<RESPONSE, REQUEST> {
    
	/**
     * Constructor
     *

     * @param transmitter    
     * 		the {@link HttpProtocolStackEndpoint} transmitting the requests build by 
     * 		the HttpUnsubscribingTask to be instantiated
     * @param requestType    
     * 		the extended {@link Request} type of the Http request created by the
     * 		the HttpUnsubscribingTask to be instantiated
     * @param subscriptionId 
     * 		the String identifier of the subscription to be deleted
     */
    public HttpUnsubscribingTask(HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType, String subscriptionId) {
        super(CommandType.UNSUBSCRIBE, transmitter, requestType, new Object[] {subscriptionId});
    }

	/**
     * Constructor
     *

     * @param transmitter    
     * 		the {@link HttpProtocolStackEndpoint} transmitting the requests build by 
     * 		the HttpUnsubscribingTask to be instantiated
     * @param requestType    
     * 		the extended {@link Request} type of the Http request created by the
     * 		the HttpUnsubscribingTask to be instantiated
     * @param subscribingTask
     * 		the HttpSubscribingTask holding the string identifier of the subscription 
     * 		to be deleted
     */
    public HttpUnsubscribingTask( HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType, HttpSubscribingTask<?,?> subscribingTask) {
        super(CommandType.UNSUBSCRIBE, transmitter, requestType, new Object[] {subscribingTask});
    }

    /**
     * @inheritDoc
     * 
     * @see HttpTask#isDirect()
     */
    public boolean isDirect() {
        return true;
    }
}
