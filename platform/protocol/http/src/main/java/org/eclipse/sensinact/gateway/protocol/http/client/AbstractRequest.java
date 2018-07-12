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
