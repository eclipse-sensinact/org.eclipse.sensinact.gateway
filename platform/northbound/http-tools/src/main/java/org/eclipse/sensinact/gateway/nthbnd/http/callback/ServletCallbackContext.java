/*********************************************************************
* Copyright (c) 2020 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Session;

/**
 * {@link CallbackContext} dedicated to http connection
 */
public class ServletCallbackContext implements CallbackContext {

    private HttpRequestWrapper request;
    private HttpResponseWrapper response;
    private Session session;
    private Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the ServletCallbackContext to be instantiated 
     * to interact with the OSGi host environment
     * @param request {@link HttpRequestWrapper} held by the ServletCallbackContext
     * to be instantiated
     * @param response {@link HttpResponseWrapper} held by the ServletCallbackContext
     * to be instantiated
     */
    public ServletCallbackContext(Mediator mediator, HttpRequestWrapper request, HttpResponseWrapper response) {
        this.request = request;
        this.response = response;
        this.mediator = mediator;
    }

    @Override
    public HttpRequestWrapper getRequest() {
    	return request;
    }

    @Override
    public HttpResponseWrapper getResponse() {
    	return response;
    }

	@Override
	public Session getSession() {
		//TODO: allow some how to authenticate
		if(this.session == null) {
			this.session = mediator.callService(Core.class, new Executable<Core,Session>(){
				@Override
				public Session execute(Core core) throws Exception {
					return core.getAnonymousSession();
				}
			});
		}
		return this.session;
	}
}
