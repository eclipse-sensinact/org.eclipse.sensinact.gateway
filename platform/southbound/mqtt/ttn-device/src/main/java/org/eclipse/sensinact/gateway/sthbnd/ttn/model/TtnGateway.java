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

import org.json.JSONException;
import org.json.JSONObject;

public class TtnGateway {

    private final String gatewayId;
    private final long timestamp;
    private final String time;
    private final int channel;
    private final double rssi;
    private final long snr;
    private final long rfChain;

    public TtnGateway(String gatewayId, long timestamp, String time, int channel, double rssi, long snr, long rfChain) {
        this.gatewayId = gatewayId;
        this.timestamp = timestamp;
        this.time = time;
        this.channel = channel;
        this.rssi = rssi;
        this.snr = snr;
        this.rfChain = rfChain;
    }

    public TtnGateway(JSONObject json) throws JSONException {
        this.gatewayId = json.getString("gtw_id");
        this.timestamp = json.getLong("timestamp");
        this.time = json.getString("time");
        this.channel = json.getInt("channel");
        this.rssi = json.getDouble("rssi");
        this.snr = json.getLong("snr");
        this.rfChain= json.getLong("rf_chain");
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTime() {
        return time;
    }

    public int getChannel() {
        return channel;
    }

    public double getRssi() {
        return rssi;
    }

    public long getSnr() {
        return snr;
    }

    public long getRfChain() {
        return rfChain;
    }
}
