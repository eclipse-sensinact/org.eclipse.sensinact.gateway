/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Session;

/**
 * {@link CallbackContext} dedicated to websocket connection
 */
public class WebSocketCallbackContext implements CallbackContext {

	private WebSocketRequestWrapper request;
	private WebSocketResponseWrapper response;
    private Session session;
	private Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the WebSocketCallbackContext to be instantiated 
     * to interact with the OSGi host environment
     * @param request {@link WebSocketRequestWrapper} held by the WebSocketCallbackContext
     * to be instantiated
     * @param response {@link WebSocketResponseWrapper} held by the WebSocketCallbackContext
     * to be instantiated
     */
    public WebSocketCallbackContext(Mediator mediator, WebSocketRequestWrapper request, WebSocketResponseWrapper response) {
        this.request = request;
        this.response = response;
        this.mediator = mediator;
    }

    @Override
    public WebSocketRequestWrapper getRequest() {
    	return this.request;
    }

    @Override
    public WebSocketResponseWrapper getResponse() {
    	return this.response;
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
