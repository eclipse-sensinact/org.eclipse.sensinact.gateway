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
package ${package}.app.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.ResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Handles calls coming from a distinct remote endpoint
 */
public class CallbackHandler  {

	private static final Logger LOG = LoggerFactory.getLogger(CallbackHandler.class);
	
    private ResponseWrapper response;
    private boolean locked;
    
	/**
     * Constructor
     */
    public CallbackHandler() {
    	this.locked = false;
    }
    
    /**
     * Processes the request wrapped by the {@link CallbackContext} passed 
     * as parameter, to send back the response that is also wrapped by the 
     * {@link CallbackContext} argument
     *
     * @param context the {@link CallbackContext} wrapping the request to be
     * processed and the response to be sent back to the requirer
     */
	public void process(CallbackContext context) {
		this.response = context.getResponse();
		String content = context.getRequest().getContent();
		if(content != null) {
			try {
				JSONObject obj = new JSONObject(content);	
				this.locked = obj.optBoolean("locked");
			} catch(NullPointerException | JSONException e) {
				LOG.error(e.getMessage(),e);
			}
		}
	}
	
	 /**
     * Transmits the message passed as parameter to the connected remote endpoint 
     *
     * @param message {@link SnaMessage} to transmit
     */
	public void doRelay(SnaMessage<?> message) {
		if(locked)
			return;
		try {
            response.setContent(message.getJSON().getBytes());
            response.flush();
        } catch (Exception e) {
			LOG.error(e.getMessage(),e);
            try {
                response.setContent(String.format("Internal server error :%s", e.getMessage()).getBytes());
                response.setResponseStatus(520);
                response.flush();
            } catch(Exception ex) {
				LOG.error(ex.getMessage(),ex);
            }
        }
	}
}
