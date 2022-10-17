/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.ttn.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.json.JsonObject;

public class TtnActivationPayload extends TtnPacketPayload {

    private final String applicationEui;
    private final String deviceEui;
    private final String deviceAddress;
    private final TtnMetadata metadata;

    public TtnActivationPayload(String applicationEui, String deviceEui, String deviceAddress, TtnMetadata metadata) {
        this.applicationEui = applicationEui;
        this.deviceEui = deviceEui;
        this.deviceAddress = deviceAddress;
        this.metadata = metadata;
    }

    public TtnActivationPayload(JsonObject json) {
        this.applicationEui = json.getString("app_eui");
        this.deviceEui = json.getString("dev_eui");
        this.deviceAddress = json.getString("dev_addr");
        this.metadata = new TtnMetadata(json.getJsonObject("metadata"));
    }

    public String getApplicationEui() {
        return applicationEui;
    }

    public String getDeviceEui() {
        return deviceEui;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public TtnMetadata getMetadata() {
        return metadata;
    }

    @Override
    public List<TtnSubPacket<?>> getSubPackets() {
        List<TtnSubPacket<?>> subPackets = new ArrayList<>();

        subPackets.add(new TtnSubPacket<>("system", "frequency", null, null, metadata.getFrequency()));
        subPackets.add(new TtnSubPacket<>("system", "modulation",  null, null, metadata.getModulation()));
        subPackets.add(new TtnSubPacket<>("system", "data_rate",  null, null, metadata.getDataRate()));
        subPackets.add(new TtnSubPacket<>("system", "coding_rate",  null, null, metadata.getCodingRate()));

        return subPackets;
    }
}
