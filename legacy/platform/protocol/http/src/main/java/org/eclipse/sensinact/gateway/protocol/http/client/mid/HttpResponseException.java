/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.http.client.mid;

import java.util.List;
import java.util.Map;

/**
 *
 *
 */
@SuppressWarnings("serial")
public class HttpResponseException extends Exception {
    private int statusCode;
    private Map<String, List<String>> headers;

    /**
     * @param statusCode
     * @param content
     * @param map
     */
    public HttpResponseException(int statusCode, byte[] content, Map<String, List<String>> headers) {
        super(content != null ? new String(content) : "Http Error");
        this.statusCode = statusCode;
        this.headers = headers;
    }

    /**
     * @return
     */
    public int getErrorStatusCode() {
        return this.statusCode;
    }

    /**
     * @return
     */
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }
}
