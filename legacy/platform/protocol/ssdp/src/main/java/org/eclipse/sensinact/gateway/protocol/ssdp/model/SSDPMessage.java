/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.ssdp.model;

import org.eclipse.sensinact.gateway.protocol.ssdp.api.SSDPEvent;

/**
 * Abstract class for SSDP messages
 */
public abstract class SSDPMessage {
    protected SSDPEvent event;

    public enum RequestLine {
        MSEARCH("M-SEARCH * " + HTTP_VERSION), NOTIFY("NOTIFY * " + HTTP_VERSION), RESPONSE(HTTP_VERSION + " 200 OK");
        private String header;

        RequestLine(String header) {
            this.header = header;
        }

        public String getRequestLine() {
            return header;
        }
    }

    public static final String HTTP_VERSION = "HTTP/1.1";
    public static final String HOST = SSDPConstant.MULTICAST_IP + ":" + SSDPConstant.MULTICAST_PORT;

    abstract void setEvent(String event);

    public SSDPEvent getEvent() {
        return event;
    }
}
