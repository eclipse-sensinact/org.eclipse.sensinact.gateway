/*
* Copyright (c) 2020-2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.ttn.listener;

import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage;
import org.eclipse.sensinact.gateway.sthbnd.ttn.model.TtnSubPacket;
import org.eclipse.sensinact.gateway.sthbnd.ttn.model.TtnUplinkPayload;
import org.eclipse.sensinact.gateway.sthbnd.ttn.packet.TtnUplinkPacket;
import org.json.JSONException;
import org.json.JSONObject;

public class TtnUplinkListener extends MqttTopicMessage {

	public static final String DOWNLINK_MARKER = "#DOWNLINK#";
	
    private final Mediator mediator;
    private final MqttProtocolStackEndpoint endpoint;
	private TtnDownlinkListener dowlinkListener;

    public TtnUplinkListener(Mediator mediator, TtnDownlinkListener downlinkListener, MqttProtocolStackEndpoint endpoint) {
        this.mediator = mediator;
        this.endpoint = endpoint;
        this.dowlinkListener = downlinkListener;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage#messageReceived(java.lang.String, java.lang.String)
     */
    @Override
    public void messageReceived(String topic, String message) {

        if(mediator.isDebugLoggable()) 
            mediator.debug("Uplink message: " + message);
        
        String device = topic.split("/")[2];
        JSONObject json = new JSONObject(message);
        TtnUplinkPayload payload = null;

        try {
            payload = new TtnUplinkPayload(mediator, json);
        } catch (JSONException e) {
            if(mediator.isErrorLoggable()) 
                mediator.error(e.getMessage(),e);
        }
        if (payload != null) {
        	List<TtnSubPacket> subPackets = payload.getSubPackets();
        	if(subPackets.isEmpty())
        		return;
        	int i=0;
        	while(i<subPackets.size()) {
        		TtnSubPacket subPacket = subPackets.get(i);
        		if(DOWNLINK_MARKER.equals(subPacket.getMetadata())) {
        			this.dowlinkListener.messageReceived(payload.getApplicationId(),payload.getDeviceId(), subPacket.getValue());
        			subPackets.remove(i);
        			continue;
        		}
        		i++;
        	}       	
            TtnUplinkPacket packet = new TtnUplinkPacket(device, subPackets);
            try {
                endpoint.process(packet);
            } catch (InvalidPacketException e) {
                e.printStackTrace();
            }
        }
    }
}
