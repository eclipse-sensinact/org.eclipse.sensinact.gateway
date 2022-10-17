/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import org.eclipse.sensinact.gateway.core.Session;

/**
 * {@link RequestWrapper} and {@link ResponseWrapper} holder
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface CallbackContext {

    /**
     * Returns an {@link Session} allowing to interact with the sensiNact instance
     *
     * @return an sensiNact {@link Session} instance - Null by default
     */
    Session getSession();
    
    /**
     * Returns an {@link RequestWrapper} wrapping the underlying 
     * request whatever it is an Http or a websocket one.
     *
     * @return an {@link RequestWrapper} wrapping the underlying 
     * request
     */
    RequestWrapper getRequest();
    
    /**
     * Returns an {@link ResponseWrapper} wrapping the underlying 
     * response to be sent back to the requirer.
     *
     * @return an {@link ResponseWrapper} wrapping the underlying 
     * response
     */
    ResponseWrapper getResponse();
}
