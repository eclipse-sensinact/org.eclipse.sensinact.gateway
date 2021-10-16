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
import org.eclipse.sensinact.gateway.common.primitive.PathElement;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * A response to an {@link HttpRequest}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class HttpResponse extends SimpleResponse implements PathElement {
    /**
     * @return
     */
    public abstract HttpPacket createPacket();

    protected String path = null;
    protected boolean isDirect;
    protected Mediator mediator;
    protected CommandType command;

    /**
     * @param mediator
     * @param connection
     * @param configuration
     * @throws IOException
     */
    public HttpResponse(Mediator mediator, HttpURLConnection connection, 
    		HttpConnectionConfiguration<? extends HttpResponse, ? extends Request<? extends HttpResponse>> configuration) 
    				throws IOException {
        super(connection);
        this.mediator = mediator;
        this.path = configuration.getPath();
        this.command = configuration.getCommand();
        this.isDirect = configuration.isDirect();
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public CommandType getCommand() {
        return this.command;
    }

    public boolean isDirect() {
        return this.isDirect;
    }
}
