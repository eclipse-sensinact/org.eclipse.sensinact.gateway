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

    protected Mediator mediator;
    protected HttpConnectionConfiguration<? extends HttpResponse, ? extends Request<? extends HttpResponse>> configuration;

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
        this.configuration = configuration;
    }

    @Override
    public String getPath() {
        return configuration.getPath();
    }

    public CommandType getCommand() {
        return configuration.getCommand();
    }

    public boolean isDirect() {
        return configuration.isDirect();
    }

    public HttpConnectionConfiguration<? extends HttpResponse, ? extends Request<? extends HttpResponse>> getConfiguration() {
    	return configuration;
    }
}
