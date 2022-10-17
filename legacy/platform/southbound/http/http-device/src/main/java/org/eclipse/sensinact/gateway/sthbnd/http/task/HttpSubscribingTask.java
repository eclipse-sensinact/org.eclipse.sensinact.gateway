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

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended {@link HttpTask} dedicated to discovery process
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpSubscribingTask<RESPONSE extends HttpResponse, REQUEST extends Request<RESPONSE>> 
extends HttpDiscoveryTask<RESPONSE, REQUEST> {
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpSubscribingTask.class);
    private Executable<Object, String> subscriptionIdExtractor;
	private String subscriptionId;
    
	/**
     * Constructor
     *
     * @param transmitter    
     * 		the {@link HttpProtocolStackEndpoint} transmitting the requests build by 
     * 		the HttpSubscribingTask to be instantiated
     * @param requestType    
     * 		the extended {@link Request} type of the Http request created by the
     * 		the HttpSubscribingTask to be instantiated
     */
    public HttpSubscribingTask(HttpProtocolStackEndpoint transmitter, Class<REQUEST> requestType, Object[] parameters) {
        super(CommandType.SUBSCRIBE, transmitter, requestType, parameters);
    }
    
    public boolean isDirect() {
        return true;
    }
	
	/**
	 * Returns the String subscription identifier resulting from 
	 * this HttpSubscribingTask execution
	 * 
	 * @return the global discovery subscription identifier
	 */
	public String getSubscriptionIdentitifer() {
		return this.subscriptionId;
	}

	private void setSubscriptionIdentitifer(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
	
    /**
     * Defines the {@link Executable} used to extract the
     * subscription identifier from this task's result object.
     * If no extractor is defined the result object is cast
     * into String and used as is.
     *
     * @param subscriptionIdExtractor the {@link Executable} in charge
     *   of extracting the string subscription identifier
     */
    public void registerSubscriptionIdExtractor(Executable<Object, String> subscriptionIdExtractor) {
        this.subscriptionIdExtractor = subscriptionIdExtractor;
    }

    public void setResult(Object result, long timestamp) {
        super.setResult(result, timestamp);
        if (super.result != null && super.result == AccessMethod.EMPTY) {
            return;
        }
        String value = null;
        try {
            value = this.subscriptionIdExtractor == null
            	?CastUtils.cast(String.class, result)
            			:this.subscriptionIdExtractor.execute(result);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        this.setSubscriptionIdentitifer(value);
    }
}
