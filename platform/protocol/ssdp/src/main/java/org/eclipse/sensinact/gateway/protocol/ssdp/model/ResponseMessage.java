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
