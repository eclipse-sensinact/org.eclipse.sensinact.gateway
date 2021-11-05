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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws;

import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper.QueryKey;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Extended {@link NorthboundAccess} dedicated to websocket connections
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class WsRestAccess extends NorthboundAccess<WsRestAccessRequest> {
    /**
     * The {@link WebSocketConnection} held by this WsRestAccess
     */
    private WebSocketConnection wsConnection;

    /**
     * Constructor
     * 
     * @param request 
     * 
     * @param wsConnection the {@link WebSocketConnection} held by the 
     * WsRestAccess to be instantiated
     * 
     * @throws IOException
     * @throws InvalidCredentialException
     */
    public WsRestAccess(WsRestAccessRequest request, WebSocketConnection wsConnection) throws IOException, InvalidCredentialException {
        super(request);
        this.wsConnection = wsConnection;
    }

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
        List<String> rawList = null;
        List<String> acceptEncoding = null;
        
        Map<QueryKey, List<String>> queryMap = super.request.getQueryMap();
        Iterator<QueryKey> iterator = queryMap.keySet().iterator();
        while(iterator.hasNext()) {
        	QueryKey queryKey = iterator.next();
        	if("rawDescribe".equals(queryKey.name)) 
        		rawList = queryMap.get(queryKey);        	
        	if("Accept-Encoding".equals(queryKey.name)) 
        		acceptEncoding= queryMap.get(queryKey);        	
        }
        if (rawList != null && (rawList.contains("true") 
        		|| rawList.contains("True") 
        		|| rawList.contains("yes") 
        		|| rawList.contains("Yes")) 
        	&& DescribeResponse.class.isAssignableFrom(cap.getClass()))
            result = ((DescribeResponse<?>) cap).getJSON(true);
        else
            result = cap.getJSON();
        
        byte[] resultBytes;
        
        try {
	        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
	            resultBytes = NorthboundAccess.compress(result);
	            this.wsConnection.send(resultBytes);
	        } else {
	            resultBytes = result.getBytes("UTF-8");
	            this.wsConnection.send(new String(resultBytes));
	        }
        } catch(Exception e) {
        	throw new IOException(e);
        }
        return true;
    }

    @Override
    protected void sendError(int i, String message) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statusCode", i);
        jsonObject.put("message", message);
        try {
			this.wsConnection.send(new String(jsonObject.toString().getBytes("UTF-8")));
		} catch (Exception e) {
			throw new IOException(e);
		}
    }
}
