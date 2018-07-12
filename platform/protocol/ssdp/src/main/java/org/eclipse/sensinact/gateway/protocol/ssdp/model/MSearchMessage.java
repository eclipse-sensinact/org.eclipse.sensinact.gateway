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
package org.eclipse.sensinact.gateway.protocol.ssdp.model;

import org.eclipse.sensinact.gateway.protocol.ssdp.api.SSDPEvent;
import org.eclipse.sensinact.gateway.protocol.ssdp.exception.InvalidParameterException;

/**
 * Message sent on multicast interface to search for new devices
 */
public class MSearchMessage extends SSDPMessage {
    private static final String NEW_LINE = "\r\n";
    private static final String MAN = "ssdp:discover";

    public void setEvent(String event) {
        super.event = SSDPEvent.DISCOVER;
    }

    /**
     * Create a M-SEARCH message (cf. UPnP Architecture specification)
     *
     * @param st search target, e.g., ssdp-all, upnp:rootdevice
     * @param mx maximum wait in seconds. Must be >= 1 and should be <= 5.
     * @return the created m-search message
     */
    public static String createMessage(String st, int mx) throws InvalidParameterException {
        StringBuilder builder = new StringBuilder();
        if (mx < 1) {
            throw new InvalidParameterException("Maximum wait time MUST be greater or equal to 1");
        }
        // First line
        builder.append(RequestLine.MSEARCH.getRequestLine());
        builder.append(NEW_LINE);
        // HOST header
        builder.append("HOST: ");
        builder.append(HOST);
        builder.append(NEW_LINE);
        // MAN header
        builder.append("MAN: ");
        builder.append("\"" + MAN + "\"");
        builder.append(NEW_LINE);
        // MX header
        builder.append("MX: ");
        builder.append(mx);
        builder.append(NEW_LINE);
        // ST header
        builder.append("ST: ");
        builder.append(st);
        builder.append(NEW_LINE);
        // New line (some servers need this)
        builder.append(NEW_LINE);
        return builder.toString();
    }
}
