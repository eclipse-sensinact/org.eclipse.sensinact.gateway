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

import java.util.List;
import java.util.Map;

/**
 * Extended {@link HttpPacket} wrapping an HTTP request message (
 * resulting of CallbackServer incoming request for example)
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpRequestPacket extends HttpPacket {
    /**
     * Constructor
     *
     * @param content the bytes array content of the wrapped
     *                HTTP message
     */
    public HttpRequestPacket(Map<String, List<String>> headers, byte[] content) {
        super(headers, content);
    }

    /**
     * Constructor
     *
     * @param content the bytes array content of the wrapped
     *                HTTP message
     */
    public HttpRequestPacket(byte[] content) {
        super(content);
    }
}
