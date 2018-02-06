package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model;

import org.eclipse.sensinact.gateway.generic.packet.annotation.*;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttPacket;

public class SmartTopicPacket extends MqttPacket {

    @ServiceProviderID
    private String providerId;

    @ServiceID
    private String serviceId;

    @ResourceID
    private String resourceId;

    @Data
    private String data;

    @HelloMessage
    private boolean helloMessage;

    @GoodbyeMessage
    private boolean goodbyeMessage;

    public SmartTopicPacket(String providerId) {
        super(providerId);

        this.providerId = providerId;
        this.helloMessage = false;
        this.goodbyeMessage = false;
    }

    public SmartTopicPacket(String providerId, String serviceId, String resourceId, String data) {
        super(providerId, serviceId, resourceId, data);

        this.providerId = providerId;
        this.serviceId = serviceId;
        this.resourceId = resourceId;
        this.data = data;
        this.helloMessage = false;
        this.goodbyeMessage = false;
    }

    public void setHelloMessage(boolean helloMessage) {
        this.helloMessage = helloMessage;
    }

    public void setGoodbyeMessage(boolean goodbyeMessage) {
        this.goodbyeMessage = goodbyeMessage;
    }
}
