/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import java.io.IOException;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.method.AccessMethod;

/**
 * Northbound request handler service
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
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
     * @param request the request wrapper allowing to initialize this handler
     * @param methods the Set of provided AccessMethods
     * 
     * @throws IOException if an error occurred while initializing
     */
    void init(NorthboundRequestWrapper request, Set<AccessMethod.Type> methods) throws IOException;

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
