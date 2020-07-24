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
package org.eclipse.sensinact.gateway.sthbnd.ttn.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    public TtnActivationPayload(JSONObject json) throws JSONException {
        this.applicationEui = json.getString("app_eui");
        this.deviceEui = json.getString("dev_eui");
        this.deviceAddress = json.getString("dev_addr");
        this.metadata = new TtnMetadata(json.getJSONObject("metadata"));
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

    public List<TtnSubPacket> getSubPackets() {
        List<TtnSubPacket> subPackets = new ArrayList<>();

        subPackets.add(new TtnSubPacket<>("system", "frequency", metadata.getFrequency()));
        subPackets.add(new TtnSubPacket<>("system", "modulation", metadata.getModulation()));
        subPackets.add(new TtnSubPacket<>("system", "data_rate", metadata.getDataRate()));
        subPackets.add(new TtnSubPacket<>("system", "coding_rate", metadata.getCodingRate()));

        return subPackets;
    }
}
