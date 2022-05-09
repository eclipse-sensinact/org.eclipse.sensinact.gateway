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
 * Response received in unicast after a M-SEARCH request has been sent
 */
public class ResponseMessage extends SSDPReceivedMessage {
    private String st;
    private String ext;

    public ResponseMessage() {
        this.setEvent("");
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    void setEvent(String event) {
        super.event = SSDPEvent.DISCOVER;
    }
}
