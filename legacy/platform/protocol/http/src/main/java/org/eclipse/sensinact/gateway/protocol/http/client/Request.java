/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.client;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * An Http Request service signature
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Request<R extends Response> {
    /**
     * Create an appropriate {@link Response} instance for this Request
     * using the {@link HttpURLConnection} passed as parameter
     *
     * @param connection the {@link HttpURLConnection} to use to create the appropriate
     *                   response
     * @return the {@link Response} of this Request
     * @throws IOException
     */
    R createResponse(HttpURLConnection connection) throws IOException;

    /**
     * Sends this Request and returns its {@link Response}
     *
     * @return the {@link Response} of this Request
     * @throws IOException
     */
    R send() throws IOException;

}
