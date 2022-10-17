/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.server;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpRequestContent extends AbstractContent implements RequestContent {
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
    private URI uri;
    private String method;

    /**
     *
     */
    public HttpRequestContent(URI uri, String method) {
        this(uri, method, Collections.<String, List<String>>emptyMap());
    }

    /**
     * @param headers
     */
    public HttpRequestContent(URI uri, String method, Map<String, List<String>> headers) {
        super(headers);
        this.uri = uri;
        this.method = method;
    }

    /**
     * @inheritDoc
     * @see RequestContent#getRequestURI()
     */
    @Override
    public String getRequestURI() {
        return this.uri.getPath();
    }

    /**
     * @inheritDoc
     * @see RequestContent#getQueryString()
     */
    @Override
    public String getQueryString() {
        return this.uri.getQuery();
    }

    /**
     * @inheritDoc
     * @see RequestContent#getHttpMethod()
     */
    @Override
    public String getHttpMethod() {
        return this.method;
    }
}
