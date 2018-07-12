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
package org.eclipse.sensinact.gateway.protocol.http.client;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 *
 */
public class SimpleRequest extends AbstractRequest<SimpleResponse> {
    /**
     * @param configuration
     */
    public SimpleRequest(ConnectionConfiguration<SimpleResponse, SimpleRequest> configuration) {
        super(configuration);
    }

    /**
     * @throws IOException
     * @inheritDoc
     * @see AbstractRequest#createResponse(java.net.HttpURLConnection)
     */
    @Override
    public SimpleResponse createResponse(HttpURLConnection connection) throws IOException {
        if (connection == null) {
            return null;
        }
        return new SimpleResponse(connection);
    }
}
