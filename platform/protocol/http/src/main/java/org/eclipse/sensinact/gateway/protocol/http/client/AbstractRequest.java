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

/**
 * Abstract implementation of a {@link Request} service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractRequest<RESPONSE extends Response> implements Request<RESPONSE> {
    /**
     * the {@link ConnectionConfiguration}
     * configuring this request
     */
    protected ConnectionConfiguration<RESPONSE, ? extends AbstractRequest<RESPONSE>> configuration;

    /**
     * Constructor
     *
     * @param configuration the {@link ConnectionConfiguration}
     *                      configuring the request to be instantiated
     */
    public AbstractRequest(ConnectionConfiguration<RESPONSE, ? extends AbstractRequest<RESPONSE>> configuration) {
        this.configuration = configuration;
    }

    /**
     * @inheritDoc
     * @see Request#send()
     */
    @Override
    public RESPONSE send() throws IOException {
        return createResponse(this.configuration.connect());
    }

}
