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
