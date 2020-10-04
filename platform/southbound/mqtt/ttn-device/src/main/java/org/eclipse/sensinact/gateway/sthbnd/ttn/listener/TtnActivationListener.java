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
package org.eclipse.sensinact.gateway.sthbnd.ttn.listener;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttPacket;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage;
import org.eclipse.sensinact.gateway.sthbnd.ttn.model.TtnActivationPayload;
import org.eclipse.sensinact.gateway.sthbnd.ttn.packet.TtnActivationPacket;
import org.json.JSONException;
import org.json.JSONObject;

public class TtnActivationListener extends MqttTopicMessage {

    private final Mediator mediator;
    private final MqttProtocolStackEndpoint endpoint;

    public TtnActivationListener(Mediator mediator, MqttProtocolStackEndpoint endpoint) {
        this.mediator = mediator;
        this.endpoint = endpoint;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage#messageReceived(java.lang.String, java.lang.String)
     */
    @Override
    public void messageReceived(String topic, String message) {

        if(mediator.isDebugLoggable()) {
            mediator.debug("Activation message: " + message);
        }
        String device = topic.split("/")[2];
        JSONObject json = new JSONObject(message);
        TtnActivationPayload payload = null;

        try {
            payload = new TtnActivationPayload(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (payload != null) {
            TtnActivationPacket packet = new TtnActivationPacket(device, payload.getSubPackets());

            try {
                endpoint.process(packet);
            } catch (InvalidPacketException e) {
                e.printStackTrace();
            }
        }
    }
}
