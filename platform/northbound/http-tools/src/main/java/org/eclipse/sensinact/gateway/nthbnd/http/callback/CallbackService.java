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

import java.util.Dictionary;

import org.eclipse.sensinact.gateway.nthbnd.http.callback.internal.CallbackServlet;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.internal.CallbackWebSocketServlet;

/**
 * A CallbackService provides the information allowing to create 
 * a {@link CallbackServlet} and/or a {@link CallbackWebSocketServlet} 
 * to be registered and whose invocation triggers its processing
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface CallbackService {

	public static final int CALLBACK_SERVLET = 1;
	public static final int CALLBACK_WEBSOCKET = 2;

    /**
     * Returns the int value defining whether this CallbackService requires
     * a {@link CallbackServlet} and/or a {@link CallbackWebSocketServlet} 
     * registration
     *
     * @return the int value defining the type of servlet(s) to be registered
     */
    int getCallbackType();
    
    /**
     * Returns the String path pattern of the servlet(s) to be registered
     *
     * @return the String path pattern
     */
    String getPattern();

    /**
     * Returns the initial set of properties of the {@link CallbackServlet}
     * and/or a {@link CallbackWebSocketServlet} to be registered
     *
     * @return the initial set of properties
     */
    Dictionary<String, ?> getProperties();

    /**
     * Processes the request wrapped by the {@link CallbackContext} passed 
     * as parameter, to send back the response that is also wrapped by the 
     * {@link CallbackContext} argument
     *
     * @param context the {@link CallbackContext} wrapping the request to be
     * processed and the response to be sent back to the requirer
     */
    void process(CallbackContext context);
        
//    /**
//     * Returns this CallbackService's String identifier allowing to attach
//     * an authenticated sensiNact's Session instance
//     * 
//     * @return this CallbackService's String identifier - Null by default 
//     */
//    default String getCallbackServiceIdentifier() {
//    	return null;
//    }
    
}
