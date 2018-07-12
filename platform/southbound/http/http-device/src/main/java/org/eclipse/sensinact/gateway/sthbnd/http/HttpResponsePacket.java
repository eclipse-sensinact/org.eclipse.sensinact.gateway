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
package org.eclipse.sensinact.gateway.sthbnd.http;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;

/**
 * Extended {@link HttpPacket} wrapping an HTTP response message
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpResponsePacket extends HttpPacket {
    protected int statusCode = -1;
    protected String path;
    protected CommandType command;

    /**
     * @param topic
     * @param mqttMessage
     */
    public HttpResponsePacket(HttpResponse response) {
        super(response != null ? response.getContent() : new byte[0]);
        this.setStatusCode(statusCode);
        this.path = response.getPath();
        this.command = response.getCommand();
        super.addHeaders(response.getHeaders());
    }

    /**
     * @return
     */
    public CommandType getCommand() {
        return this.command;
    }

    /**
     * @return
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Defines the integer status of the wrapped HTTP message
     *
     * @param statusCode the integer status of the wrapped HTTP message
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Returns the integer status of the wrapped
     * HTTP message
     *
     * @return the integer status of the wrapped
     * HTTP message
     */
    public int getStatusCode() {
        return this.statusCode;
    }
}
