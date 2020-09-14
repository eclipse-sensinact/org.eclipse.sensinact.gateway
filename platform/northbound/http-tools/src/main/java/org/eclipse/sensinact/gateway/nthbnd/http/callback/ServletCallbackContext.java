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
 * {@link CallbackContext} dedicated to http connection
 */
public class ServletCallbackContext implements CallbackContext {

    private HttpRequestWrapper request;
    private HttpResponseWrapper response;

    /**
     * Constructor
     *
     * @param request {@link HttpRequestWrapper} held by the ServletCallbackContext
     * to be instantiated
     * @param response {@link HttpResponseWrapper} held by the ServletCallbackContext
     * to be instantiated
     */
    public ServletCallbackContext(HttpRequestWrapper request, HttpResponseWrapper response) {
        this.request = request;
        this.response = response;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext#getRequest()
     */
    @Override
    public HttpRequestWrapper getRequest() {
    	return request;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext#getResponse()
     */
    @Override
    public HttpResponseWrapper getResponse() {
    	return response;
    }
}
