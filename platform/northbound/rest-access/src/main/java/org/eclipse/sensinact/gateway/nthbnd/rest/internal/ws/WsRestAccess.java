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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws;

import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Extended {@link NorthboundAccess} dedicated to websocket connections
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class WsRestAccess extends NorthboundAccess<WsRestAccessRequest> {
    /**
     * The {@link WebSocketConnection} held by this WsRestAccess
     */
    private WebSocketConnection wsConnection;

    /**
     * Constructor
     *
     * @param wsConnection the {@link WebSocketConnection} held by
     *                     the WsRestAccess to be instantiated
     * @throws IOException
     * @throws InvalidCredentialException
     */
    public WsRestAccess(WsRestAccessRequest request, WebSocketConnection wsConnection) throws IOException, InvalidCredentialException {
        super(request);
        this.wsConnection = wsConnection;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.AbstractNorthboundRequestHandler#respond(org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder)
     */
    @Override
    protected boolean respond(NorthboundMediator mediator, NorthboundRequestBuilder builder) throws IOException {
        NorthboundRequest nthbndRequest = builder.build();
        if (nthbndRequest == null) {
            sendError(500, "Internal server error");
            return false;
        }
        AccessMethodResponse<?> cap = this.wsConnection.getEndpoint().execute(nthbndRequest);
        if (cap == null) {
            sendError(500, "Internal server error");
            return false;
        }
        String result = null;
        List<String> rawList = super.request.getQueryMap().get("rawDescribe");

        if (rawList != null && (rawList.contains("true") || rawList.contains("True") || rawList.contains("yes") || rawList.contains("Yes")) && DescribeResponse.class.isAssignableFrom(cap.getClass())) {
            result = ((DescribeResponse<?>) cap).getJSON(true);
        } else {
            result = cap.getJSON();
        }
        byte[] resultBytes;
        List<String> acceptEncoding = super.request.getQueryMap().get("Accept-Encoding");
        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            resultBytes = NorthboundAccess.compress(result);
            this.wsConnection.send(resultBytes);

        } else {
            resultBytes = result.getBytes("UTF-8");
            this.wsConnection.send(new String(resultBytes));
        }
        return true;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.AbstractNorthboundRequestHandler#sendError(int, java.lang.String)
     */
    @Override
    protected void sendError(int i, String message) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statusCode", i);
        jsonObject.put("message", message);
        this.wsConnection.send(new String(jsonObject.toString().getBytes("UTF-8")));
    }
}
