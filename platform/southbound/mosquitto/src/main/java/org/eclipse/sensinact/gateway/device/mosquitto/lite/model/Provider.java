package org.eclipse.sensinact.gateway.device.mosquitto.lite.model;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTProvider;

import java.util.ArrayList;
import java.util.List;

public class Provider implements MQTTProvider {

    private String name;
    private String host;
    private Long port;
    private List<Service> services=new ArrayList<>();
    private Boolean discoveryOnFirstMessage;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    @Override
    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public Boolean isDiscoveryOnFirstMessage() {
        return discoveryOnFirstMessage;
    }

    public void setIsDiscoveryOnFirstMessage(Boolean discoveryOnFirstMessage) {
        this.discoveryOnFirstMessage = discoveryOnFirstMessage;
    }
}
