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

    /**
     * @inheritDoc
     * @see RequestHandler#handle(RequestContent)
     */
    @Override
    public ResponseContent handle(RequestContent request) {
        List<Callback> callbackList = null;

        if (request.getHttpMethod().equals("GET")) {
            callbackList = this.callbacks.getdoGetCallbacks();
        } else if (request.getHttpMethod().equals("POST")) {
            callbackList = this.callbacks.getdoPostCallbacks();
        }

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
