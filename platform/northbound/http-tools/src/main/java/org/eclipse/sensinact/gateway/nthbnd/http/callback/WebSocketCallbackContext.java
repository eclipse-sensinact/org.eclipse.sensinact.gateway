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
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

/**
 * {@link CallbackContext} dedicated to websocket connection
 */
public class WebSocketCallbackContext implements CallbackContext {

	private WebSocketRequestWrapper request;
	private WebSocketResponseWrapper response;

    /**
     * Constructor
     *
     * @param request {@link WebSocketRequestWrapper} held by the WebSocketCallbackContext
     * to be instantiated
     * @param response {@link WebSocketResponseWrapper} held by the WebSocketCallbackContext
     * to be instantiated
     */
    public WebSocketCallbackContext(WebSocketRequestWrapper request, WebSocketResponseWrapper response) {
        this.request = request;
        this.response = response;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext#getRequest()
     */
    @Override
    public WebSocketRequestWrapper getRequest() {
    	return this.request;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext#getResponse()
     */
    @Override
    public WebSocketResponseWrapper getResponse() {
    	return this.response;
    }
}
