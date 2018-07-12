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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import java.io.IOException;

/**
 * Northbound request handler service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface NorthboundRequestHandler {
    /**
     * Data structure gathering the error's String message
     * and int status (HTML response status based)
     */
    class NorthboundResponseBuildError {
        public final int status;
        public final String message;

        public NorthboundResponseBuildError(int status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    /**
     * Initializes this handler using the request wrapper passed as
     * parameter to set the appropriate fields
     *
     * @param request the request wrapper allowing to initialize
     *                this handler
     * @throws IOException if an error occurred while initializing
     */
    void init(NorthboundRequestWrapper request) throws IOException;

    /**
     * Checks whether the URI field value of the request handled by
     * this NorthboundRequestHandler can be treated or not. Returns
     * true if it is the case, returns false otherwise
     *
     * @return <ul>
     * <li>true if the String URI of the handled request can be
     * treated by this handler</li>
     * <li>false otherwise</li>
     * </ul>
     * @throws IOException
     */
    boolean processRequestURI() throws IOException;

    /**
     * Treats the handled request and returns the {@link NorthboundRequestBuilder}
     * allowing to build the appropriate response.
     *
     * @return the {@link NorthboundRequestBuilder} allowing to build the
     * response of the handled request.
     * @throws IOException
     */
    NorthboundRequestBuilder handle() throws IOException;

    /**
     * Returns the {@link NorthboundResponseBuildError} resulting from
     * the treatment of the handled request
     *
     * @return the {@link NorthboundResponseBuildError} resulting from
     * the treatment of the handled request
     */
    NorthboundResponseBuildError getBuildError();

}
