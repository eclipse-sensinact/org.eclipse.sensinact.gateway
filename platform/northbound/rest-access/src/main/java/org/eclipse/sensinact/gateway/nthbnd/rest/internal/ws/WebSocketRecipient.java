/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * This class is a wrapper for simple callback subscription
 */
public class WebSocketRecipient extends NorthboundRecipient {
    private WebSocketConnection wsConnection;

    public WebSocketRecipient(Mediator mediator, WebSocketConnection wsConnection) {
        super(mediator);
        this.wsConnection = wsConnection;
    }

    @Override
    public void callback(String callbackId, SnaMessage<?>[] messages) throws Exception {
        int index = 0;
        int length = messages == null ? 0 : messages.length;
        StringBuilder builder = new StringBuilder();
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append("\"type\":\"CALLBACK\",");
        builder.append("\"callbackId\":");
        if (callbackId == null) {
            builder.append("null");
        } else {
            builder.append("\"");
            builder.append(callbackId);
            builder.append("\"");
        }
        builder.append(",\"messages\":");
        builder.append(JSONUtils.OPEN_BRACKET);
        for (; index < length; index++) {
            builder.append(index == 0 ? "" : ",");
            builder.append(messages[index].getJSON());
        }
        builder.append(JSONUtils.CLOSE_BRACKET);
        builder.append(JSONUtils.CLOSE_BRACE);
        this.wsConnection.send(builder.toString());
    }

}
