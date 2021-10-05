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
package org.eclipse.sensinact.gateway.protocol.http.client;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Simplest {@link AbstractRequest} concrete implementation
 */
public class SimpleRequest extends AbstractRequest<SimpleResponse> {
    /**
     * Constructor
     * 
     * @param configuration the {@link ConnectionConfiguration} used be the SimpleRequest to be 
     * instantiated
     */
    public SimpleRequest(ConnectionConfiguration<SimpleResponse, SimpleRequest> configuration) {
        super(configuration);
    }

    @Override
    public SimpleResponse createResponse(HttpURLConnection connection) throws IOException {
        if (connection == null) {
            return null;
        }
        return new SimpleResponse(connection);
    }
}
