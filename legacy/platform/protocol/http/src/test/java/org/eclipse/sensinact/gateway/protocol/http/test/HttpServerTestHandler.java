/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.test;

import org.eclipse.sensinact.gateway.protocol.http.server.RequestContent;
import org.eclipse.sensinact.gateway.protocol.http.server.RequestHandler;
import org.eclipse.sensinact.gateway.protocol.http.server.ResponseContent;

import java.util.List;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpServerTestHandler implements RequestHandler {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    private CallbackCollection callbacks;

    /**
     *
     */
    public HttpServerTestHandler(CallbackCollection callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public ResponseContent handle(RequestContent request) {
        List<Callback> callbackList = null;

        if (request.getHttpMethod().equals("GET"))
            callbackList = this.callbacks.getdoGetCallbacks();
        else if (request.getHttpMethod().equals("POST"))
            callbackList = this.callbacks.getdoPostCallbacks();

        int index = 0;
        int length = callbackList == null ? 0 : callbackList.size();

        for (; index < length; index++) {
            Callback callback = callbackList.get(index);
            try {
                return (ResponseContent) callback.invoke(new Object[]{request});
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        return null;
    }
}
