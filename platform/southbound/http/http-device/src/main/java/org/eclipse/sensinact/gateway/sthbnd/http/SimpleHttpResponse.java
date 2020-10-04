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
package org.eclipse.sensinact.gateway.sthbnd.http;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
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
     * @param mediator
     * @param connection
     * @param configuration
     * @throws IOException
     */
    public SimpleHttpResponse(Mediator mediator, HttpURLConnection connection, HttpConnectionConfiguration<? extends HttpResponse, ? extends Request<? extends HttpResponse>> configuration) throws IOException {
        super(mediator, connection, configuration);
        this.packetType = configuration.getPacketType();
    }

    /**
     * @InheritedDoc
     * @see HttpResponse#createPacket()
     */
    @Override
    public HttpPacket createPacket() {
        if (this.packetType == null) {
            return new HttpResponsePacket(this);
        }
        try {
            if (HttpResponsePacket.class.isAssignableFrom(packetType)) 
                return packetType.getConstructor(HttpResponse.class).newInstance(this);
            else 
                return this.packetType.getConstructor(Map.class, byte[].class).newInstance(this.getHeaders(), this.content);
        } catch (Exception e) {
        	e.printStackTrace();
            return new HttpPacket(this.getHeaders(), this.content);
        }
    }
}
