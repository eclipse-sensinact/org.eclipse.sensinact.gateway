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
