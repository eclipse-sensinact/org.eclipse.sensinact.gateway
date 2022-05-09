/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http;

import org.eclipse.sensinact.gateway.protocol.http.client.Request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 *
 */
public class SimpleHttpResponse extends HttpResponse {
    private Class<? extends HttpPacket> packetType;

    /**
     * @param connection
     * @param configuration
     * @throws IOException
     */
    public SimpleHttpResponse(HttpURLConnection connection, 
    		HttpConnectionConfiguration<? extends HttpResponse, ? extends Request<? extends HttpResponse>> configuration) 
    				throws IOException {
        super(connection, configuration);
        this.packetType = configuration.getPacketType();
    }

    @Override
    public HttpPacket createPacket() {
        if (this.packetType == null) {
            return new HttpResponsePacket(this);
        }
        try {
            if (HttpResponsePacket.class.isAssignableFrom(packetType)) 
                return packetType.getConstructor(HttpResponse.class
                		).newInstance(this);
            else 
                return this.packetType.getConstructor(Map.class, byte[].class
                		).newInstance(super.getHeaders(), super.getContent());
        } catch (Exception e) {
            return new HttpPacket(this.getHeaders(), super.getContent());
        }
    }
}
