package org.eclipse.sensinact.gateway.device.mosquitto.lite.model;

import java.util.ArrayList;
import java.util.List;

public class Service {

    private String name;

    private List<Resource> resources=new ArrayList<Resource>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
