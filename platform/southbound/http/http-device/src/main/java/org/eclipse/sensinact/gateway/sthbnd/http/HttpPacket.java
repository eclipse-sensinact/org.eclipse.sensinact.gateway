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

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;

import java.util.List;
import java.util.Map;

/**
 * Extended {@link Packet} wrapping an HTTP message
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpPacket extends HeadersCollection implements Packet {
    /**
     * the bytes array content of the wrapped
     * HTTP message
     */
    protected byte[] content;

    /**
     * Constructor
     *
     * @param content the bytes array content of the wrapped
     *                HTTP message
     */
    public HttpPacket(Map<String, List<String>> headers, byte[] content) {
        super(headers);
        int length = content == null ? 0 : content.length;
        this.content = new byte[length];
        if (length > 0) {
            System.arraycopy(content, 0, this.content, 0, length);
        }
    }

    /**
     * Constructor
     *
     * @param content the bytes array content of the wrapped
     *                HTTP message
     */
    public HttpPacket(byte[] content) {
        this(null, content);
    }

    @Override
    public byte[] getBytes() {
        int length = this.content == null ? 0 : this.content.length;
        byte[] content = new byte[length];
        if (length > 0) {
            System.arraycopy(this.content, 0, content, 0, length);
        }
        return content;
    }
}
