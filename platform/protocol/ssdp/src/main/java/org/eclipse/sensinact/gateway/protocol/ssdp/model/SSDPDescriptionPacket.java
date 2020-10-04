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

public class SSDPDescriptionPacket {
    private String friendlyName;
    private String url;

    public SSDPDescriptionPacket() {
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @inheritDoc
     */
    public String toString() {
        return "SSDPDescriptionPacket{" + "friendlyName='" + friendlyName + '\'' + ", url='" + url + '\'' + '}';
    }
}
