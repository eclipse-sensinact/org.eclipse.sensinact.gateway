/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.ttn.listener;

import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttPacket;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage;
import org.eclipse.sensinact.gateway.sthbnd.ttn.model.TtnSubPacket;
import org.eclipse.sensinact.gateway.sthbnd.ttn.model.TtnUplinkPayload;
import org.eclipse.sensinact.gateway.sthbnd.ttn.packet.TtnUplinkPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.JsonObject;

public class TtnUplinkListener extends MqttTopicMessage {
	
	private static final Logger LOG = LoggerFactory.getLogger(TtnUplinkListener.class);

	public static final String DOWNLINK_MARKER = "#DOWNLINK#";
	
    private final Mediator mediator;
    private final ProtocolStackEndpoint<MqttPacket> endpoint;
	private final TtnDownlinkListener dowlinkListener;
	private final ObjectMapper mapper;
	
    public TtnUplinkListener(Mediator mediator, TtnDownlinkListener downlinkListener, ProtocolStackEndpoint<MqttPacket> endpoint, ObjectMapper mapper) {
        this.mediator = mediator;
        this.endpoint = endpoint;
        this.dowlinkListener = downlinkListener;
        this.mapper = mapper;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage#messageReceived(java.lang.String, java.lang.String)
     */
    @Override
    public void messageReceived(String topic, String message) {

        if(LOG.isDebugEnabled()) 
            LOG.debug("Uplink message: " + message);
        
        String device = topic.split("/")[2];
        TtnUplinkPayload payload = null;

        try {
        	JsonObject json = mapper.readValue(message, JsonObject.class);
            payload = new TtnUplinkPayload(mediator, json);
        } catch (Exception e) {
            if(LOG.isErrorEnabled()) 
                LOG.error(e.getMessage(),e);
        }
        if (payload != null) {
        	List<TtnSubPacket<?>> subPackets = payload.getSubPackets();
        	if(subPackets.isEmpty())
        		return;
        	int i=0;
        	while(i<subPackets.size()) {
        		TtnSubPacket<?> subPacket = subPackets.get(i);
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
