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
package org.eclipse.sensinact.gateway.device.openhab.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.device.openhab.sensinact.OpenHabMediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OpenHab2 Packet
 *
 * @author <a href="mailto:chirstophe.munillaO@cea.fr">Christophe Munilla</a>
 * @author sb252289
 */
public class OpenHabPacketReader extends SimplePacketReader<HttpPacket> {

    private static final Map<String, Provider> providers = new HashMap<String, Provider>();

    /**
     * @param mediator
     */
    public OpenHabPacketReader(Mediator mediator) {
        super(mediator);
    }

    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.generic.packet.PacketReader#parse(org.eclipse.sensinact.gateway.generic.packet.Packet)
     */
    @Override
    public void parse(HttpPacket packet) throws InvalidPacketException {
        byte[] content = packet.getBytes();
        String sb = new String(content);

        Set<String> devices = new HashSet<String>();

        //mediator.debug("Response from server: %s", sb);    	
        try {
            String openHabId = null;
            JSONArray itemsArray = new JSONArray(sb);

            for (int x = 0; x < itemsArray.length(); x++) {
                String serviceId = null;
                String resourceId = null;
                try {
                    JSONObject jo = itemsArray.getJSONObject(x);
                    String type = jo.getString("type");
                    String providerId = jo.getString("name");
                    String link = jo.getString("link");
                    String state = jo.getString("state");
                    if (openHabId == null) {
                        try {
                            URL url = new URL(link);
                            openHabId = "openHab".concat(
                                    String.valueOf((url.getHost() + url.getPort()).hashCode()));

                        } catch (MalformedURLException e) {
                        }
                    }

                    devices.add(providerId);

                    switch (type) {
                        case "SwitchItem":
                        case "Switch":
                            serviceId = "power";
                            resourceId = "power";
                            break;
                        case "NumberItem":
                        case "Number":
                        case "Rollershutter":
                        case "Dimmer":
                        case "Contact":
                        case "Color":
                        case "DateTime":
                        case "Location":
                            serviceId = "info";
                            resourceId = "value";
                            break;
                        default:
                            String msg = String.format(
                                    "Type %s is not supported by this Openhab2 parser", type);
                            throw new InvalidPacketException(msg);
                    }
//                        mediator.debug("Processing item %s", jo.toString());
//                    super.setServiceProviderId(name);
//                    super.configure();
                    if (hasValueChanged(providerId, serviceId, resourceId, state)) {
                        super.setServiceProviderId(providerId);
                        super.setServiceId(serviceId);
                        super.setResourceId(resourceId);
                        super.setTimestamp(System.currentTimeMillis());
                        super.setData(state);
                        super.configure();
                        mediator.info("updated value " + providerId + "/" + serviceId + "/" + resourceId + "=" + state);
                    } else {
//                        mediator.info("avoided " + providerId + "/" + serviceId +"/"+ resourceId +"="+ state);
                    }
                } catch (Exception e) {
                    mediator.warn("OpenHab device type %s not supported", e);
                }
            }
            Set<String> toBeRemoved = ((OpenHabMediator) super.mediator).updateBroker(openHabId, devices);
            for (String providerId : toBeRemoved) {
                super.setServiceProviderId(providerId);
                super.isGoodbyeMessage(true);
                super.configure();
                Provider toBeDestroyedProvider = providers.get(providerId);
                if (toBeDestroyedProvider == null) {
                    mediator.error("cannot destroy removed openhab provider %s: is not referenced?", providerId);
                } else {
                    toBeDestroyedProvider.clear();
                    toBeDestroyedProvider = null;
                    mediator.warn("destroyed removed openhab provider %s", providerId);
                }
            }
        } catch (JSONException e) {
            throw new InvalidPacketException(e);
        }
    }

    private boolean hasValueChanged(String providerId, String serviceId, String resourceId, Object value) {
        boolean hasChanged = true;
        Provider provider = providers.get(providerId);
        if (provider == null) {
//            System.out.println("do not find " + providerId);
            provider = new Provider(providerId);
            providers.put(providerId, provider);
        }
        hasChanged = provider.setValue(serviceId, resourceId, value);
        return hasChanged;
    }

    private static class Provider extends HashMap<String, Service> {

        private final String name;

        private Provider(String name) {
            this.name = name;
        }

        @Override
        public void clear() {
            for (Service service : values()) {
                service.clear();
                service = null;
            }
            super.clear();
        }

        private Service getService(String serviceId) {
            return get(serviceId);
        }

        private Object getValue(String serviceId, String resourceId) {
            Object value = null;
            Service service = this.getService(serviceId);

            if (service != null) {
                Resource resource = service.getResource(resourceId);
                if (resource != null) {
                    value = resource.getValue();
                }
            }
            return value;
        }

        boolean setValue(String serviceId, String resourceId, final Object value) {
            boolean hasChanged = false;
            Service service = this.getService(serviceId);
            if (service == null) {
                service = new Service(serviceId);
                put(serviceId, service);
            }
            hasChanged = service.setResourceValue(resourceId, value);
            return hasChanged;
        }
    }

    private static class Service extends HashMap<String, Resource> {

        private final String name;

        Service(String name) {
            this.name = name;
        }

        Resource getResource(String resourceId) {
            return get(resourceId);
        }

        boolean setResourceValue(String resourceId, Object value) {
            boolean hasChanged = false;
            Resource resource = get(resourceId);
            if (resource == null) {
                resource = new Resource(resourceId, value);
                put(resourceId, resource);
                hasChanged = true;
            } else {
                hasChanged = resource.setValue(value);
            }
            return hasChanged;
        }
    }

    private static class Resource {

        private final String name;
        private Object value;

        Resource(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        private Object getValue() {
            return value;
        }

        boolean setValue(Object value) {
            boolean hasChanged = false;
            Object previous = this.value;
            if (!this.value.equals(value)) {
                this.value = value;
                hasChanged = true;
//                System.out.println("CANNOT avoid " + previous + "!=" + value);
            } else {
//                System.out.println("CAN avoid!");
            }
            return hasChanged;
        }
    }
}
