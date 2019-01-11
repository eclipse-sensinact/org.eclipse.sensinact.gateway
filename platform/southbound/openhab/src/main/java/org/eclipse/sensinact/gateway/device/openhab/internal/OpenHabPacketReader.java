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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.device.openhab.sensinact.OpenHabMediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.sensinact.gateway.device.openhab.sensinact.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenHab2 Packet
 *
 * @author <a href="mailto:chirstophe.munillaO@cea.fr">Christophe Munilla</a>
 * @author sb252289
 */
public class OpenHabPacketReader extends SimplePacketReader<HttpPacket> {
    
    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    
    private static final String OPENHAB_ZWAVE_PROVIDER_ID_PATTERN = 
        "{0}_node{1}";                                                          //zwave_device_07150a2a_node21
    private static final String OPENHAB_ZWAVE_DEVICE_ID_PATTERN = 
        OPENHAB_ZWAVE_PROVIDER_ID_PATTERN + "_{2}_{3}";                         //zwave_device_07150a2a_node21_alarm_general
    protected static final  MessageFormat OPENHAB_ZWAVE_PROVIDER_ID_FORMAT = 
        new MessageFormat(OPENHAB_ZWAVE_PROVIDER_ID_PATTERN);
    private static final  MessageFormat OPENHAB_ZWAVE_DEVICE_ID_FORMAT = 
        new MessageFormat(OPENHAB_ZWAVE_DEVICE_ID_PATTERN);
    private static final Map<String, Provider> providers = new HashMap<String, Provider>();
    private static final String DEFAULT_OPENHAB_SERVICE_ID = "info";
    private static final String DEFAULT_OPENHAB_RESOURCE_ID = "value";
    
    /**
     * @param mediator
     */
    public OpenHabPacketReader(Mediator mediator) {
        super(mediator);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.packet.PacketReader#parse(org.eclipse.sensinact.gateway.generic.packet.Packet)
     */
    @Override
    public void parse(HttpPacket packet) throws InvalidPacketException {
        byte[] content = packet.getBytes();
        String sb = new String(content);
        Set<String> devices = new HashSet<String>();
        //LOG.debug("Response from server: {}", sb);    	
        try {
            String openHabId = null;
            JSONArray itemsArray = new JSONArray(sb);
            for (int x = 0; x < itemsArray.length(); x++) {
                JSONObject jo = itemsArray.getJSONObject(x);
                try {
                    openHabId = parseItem(jo, openHabId, devices);
                } catch (Exception e1) {
                    try {
                        parseThing(jo);
                    } catch (Exception e2) {
                        LOG.warn("OpenHab device error", e2);
                    }
                }
            }
            if (openHabId != null) {
                Set<String> toBeRemoved = ((OpenHabMediator) super.mediator).updateBroker(openHabId, devices);
                for (String providerId : toBeRemoved) {
                    super.setServiceProviderId(providerId);
                    super.isGoodbyeMessage(true);
                    super.configure();
                    Provider toBeDestroyedProvider = providers.get(providerId);
                    if (toBeDestroyedProvider == null) {
                        LOG.error("cannot destroy removed openhab provider {}: is not referenced?", providerId);
                    } else {
                        toBeDestroyedProvider.clear();
                        toBeDestroyedProvider = null;
                        LOG.warn("destroyed removed openhab provider {}", providerId);
                    }
                }
            }
        } catch (JSONException e) {
            throw new InvalidPacketException(e);
        }
    }

    private String parseItem(JSONObject jo, String openHabId, Set<String> devices) throws JSONException {
        final String type = jo.getString("type");
        final String openhaDeviceId = jo.getString("name");
        final String link = jo.getString("link");
        final String value = jo.getString("state");
        if (openHabId == null) {
            try {
                final URL url = new URL(link);
                openHabId = "openHab".concat(String.valueOf((url.getHost() + url.getPort()).hashCode()));
            } catch (MalformedURLException e) {
            }
        }
        devices.add(openhaDeviceId);
        final Resource resource = getResource(openhaDeviceId);
        resource.process(this, type, value);
        return openHabId;
    }

    private void parseThing(JSONObject jo) throws JSONException {
        final String uuid = jo.getString("UID");
        final JSONObject statusInfo = jo.getJSONObject("statusInfo");
        final String label = jo.getString("label");
        final String status = statusInfo.getString("status");
        final String statusDetail = statusInfo.getString("statusDetail");
        final String openhaDeviceId = uuid.replaceAll(":", "_");
        final Resource resourceFriendlyName = getResource(openhaDeviceId, "admin", "friendlyName");
        resourceFriendlyName.process(this, label);
        final Resource resourceStatus = getResource(openhaDeviceId, "status", "connected");
        resourceStatus.process(this, status.equals("ONLINE"));
        final Resource resourceDetail = getResource(openhaDeviceId, "status", "detail");
        resourceDetail.process(this, statusDetail);
    }

    protected static String[] parseOpenhabPath(final String openhabDeviceId) throws ParseException {
        final String[] path = new String[4];
        Object[] parsedOpenhabPath = OPENHAB_ZWAVE_DEVICE_ID_FORMAT.parse(openhabDeviceId);
        for (int i = 0; i < 4; i++) {
            path[i] = parsedOpenhabPath[i].toString();
        }
        return path;
    }
    
    
    private Resource getResource(final String openhabDeviceId) {
        Resource resource = null;
        try {
            final String[] parsedOpenhabPath = parseOpenhabPath(openhabDeviceId);
            resource = getResource(parsedOpenhabPath);
        } catch (Exception ex) {
            LOG.warn("not a *node* openhab device id: creating {}/{}/{}", openhabDeviceId, DEFAULT_OPENHAB_SERVICE_ID, DEFAULT_OPENHAB_RESOURCE_ID);
            resource = getResource(openhabDeviceId, DEFAULT_OPENHAB_SERVICE_ID, DEFAULT_OPENHAB_RESOURCE_ID);
        }
        return resource;
    }

    private Resource getResource(final String[] openhabPath) {
        final String providerId = OPENHAB_ZWAVE_PROVIDER_ID_FORMAT.format(openhabPath);
        final Resource resource = getResource(providerId, openhabPath[2], openhabPath[3]);
        return resource;
    }

    protected static Provider createProvider(final String[] openhabPath) {
        final String providerId = OPENHAB_ZWAVE_PROVIDER_ID_FORMAT.format(openhabPath);
        final Provider provider = new Provider(providerId);
        provider.addResource(openhabPath[2], openhabPath[3]);
        return provider;
    }


    private Resource getResource(final String providerId, final String serviceId, final String resourceId) {
        Provider provider = providers.get(providerId);
        if (provider == null) {
//            System.out.println("do not find " + providerId);
            provider = new Provider(providerId);
            providers.put(providerId, provider);
        }
        return provider.addResource(serviceId, resourceId);
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

        Resource addResource(String serviceId, String resourceId) {
            boolean hasChanged = false;
            Service service = this.getService(serviceId);
            if (service == null) {
                service = new Service(this, serviceId);
                put(serviceId, service);
            }
            return service.addResource(resourceId);
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
                service = new Service(this, serviceId);
                put(serviceId, service);
            }
            hasChanged = service.setResourceValue(resourceId, value);
            return hasChanged;
        }
        
        @Override
        public String toString() {
            return name + super.toString();
        }
    }

    private static class Service extends HashMap<String, Resource> {
        private final String name;
        private final Provider provider;
        
        Service(final Provider provider, final String name) {
            this.name = name;
            this.provider = provider;
        }

        Resource getResource(String resourceId) {
            return get(resourceId);
        }

        Resource addResource(String resourceId) {
            Resource resource = get(resourceId);
            if (resource == null) {
                resource = new Resource(this, resourceId);
                put(resourceId, resource);
            }
            return resource;
        }
        
        boolean setResourceValue(String resourceId, Object value) {
            boolean hasChanged = false;
            Resource resource = get(resourceId);
            if (resource == null) {
                resource = new Resource(this, resourceId, value);
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
        private final Service service;

        Resource(final Service service, final String name) {
            this(service, name, "");
        }

        Resource(final Service service, final String name, final Object value) {
            this.service = service;
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
        
        private void process(final OpenHabPacketReader reader, final Object value) {
            process(reader, OpenhabType.Default.name(), value);
        }

        private void process(final OpenHabPacketReader reader, final String type, final Object value) {
            if (setValue(value)) {
                reader.setServiceProviderId(service.provider.name);
                reader.setServiceId(service.name);
                reader.setResourceId(name);
                reader.setTimestamp(System.currentTimeMillis());
                Object parsedValue = setData(reader, type, value);
                reader.configure();
                LOG.debug("processed {}/{}/{}={}:{}", service.provider.name, service.name, name, parsedValue, type);
            } else {
//                LOG.debug("avoided to process {}/{}/{}={}:{}", service.provider.name, service.name, name, value, type);
            }
        }
        
        private Object setData(final OpenHabPacketReader reader, final String type, final Object value) {
            final Object parsedValue = OpenhabType.parseValue(type, value);
            reader.setData(parsedValue);
            return parsedValue;
        }
        
        @Override
        public String toString() {
            return "value=" + value;
        }
    }
    
    private static final MessageFormat TEMPERATURE_FORMAT = new MessageFormat("{0} °C");
    private static enum OpenhabType {
        SwitchItem,
        Switch,
        String,
        NumberItem,
        Number,
        Number_Temperature {
            @Override
            protected Object parseValue(final Object value) {
                Object parsedValue = null;
                if (value != null && !value.equals("NULL")) {
                    try {
                        final Object[] parsedArray = TEMPERATURE_FORMAT.parse(value.toString());
                        parsedValue = parsedArray[0];
                    } catch (ParseException ex) {
                        LOG.error("unexpected format for {}: not a {} format?", value, this);
                    }
                }
                return parsedValue;
            }
        },
        Player,
        Rollershutter,
        Dimmer,
        Contact,
        Color,
        DateTime,
        Group,
        Image,
        Location,
        Default;
        
        
        private static OpenhabType getType(final String type$) {
            final String openhabType$ = type$.replaceAll(":", "_");
            OpenhabType openhabType = Default;
            try {
                openhabType = OpenhabType.valueOf(openhabType$);
            } catch (Exception e) {
                LOG.error("unsupported openhab type {}. Using {}...", type$, openhabType, e);
            }
            return openhabType;
        }
        
        private static Object parseValue(final String type, final Object value) {
            OpenhabType openhabType = getType(type);
            return openhabType.parseValue(value);
        }

        protected Object parseValue(final Object value) {
            return value;
        }
    }
}
