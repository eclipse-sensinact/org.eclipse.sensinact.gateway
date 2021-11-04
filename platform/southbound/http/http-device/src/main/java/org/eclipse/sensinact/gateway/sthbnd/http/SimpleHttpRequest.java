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
package org.eclipse.sensinact.gateway.sthbnd.http;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.eclipse.sensinact.gateway.protocol.http.client.AbstractRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;

/**
 *
 */
public class SimpleHttpRequest extends AbstractRequest<SimpleHttpResponse> {

    /**
     * @param configuration
     */
    public SimpleHttpRequest(HttpConnectionConfiguration<SimpleHttpResponse, ? extends AbstractRequest<SimpleHttpResponse>> configuration) {
        super(configuration);
    }

    @Override
    public SimpleHttpResponse createResponse(HttpURLConnection connection) throws IOException {
        if (connection == null) 
            return null;        
        SimpleHttpResponse response = new SimpleHttpResponse( connection, 
        		(HttpConnectionConfiguration<? extends HttpResponse, ? extends Request<? extends HttpResponse>>) 
        		super.configuration);
        return response;
    }
}
