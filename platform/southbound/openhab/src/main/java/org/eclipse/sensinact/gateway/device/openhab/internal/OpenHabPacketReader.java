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
import org.eclipse.sensinact.gateway.generic.model.Provider;
import org.eclipse.sensinact.gateway.generic.model.Resource;
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
    
    static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    static final MessageFormat TEMPERATURE_FORMAT = new MessageFormat("{0} °C");
    
    private static final String OPENHAB_ZWAVE_PROVIDER_ID_PATTERN = "{0}_node{1}";                                                          //zwave_device_07150a2a_node21
    private static final String OPENHAB_ZWAVE_DEVICE_ID_PATTERN = OPENHAB_ZWAVE_PROVIDER_ID_PATTERN + "_{2}_{3}";                         //zwave_device_07150a2a_node21_alarm_general
    protected static final MessageFormat OPENHAB_ZWAVE_PROVIDER_ID_FORMAT = new MessageFormat(OPENHAB_ZWAVE_PROVIDER_ID_PATTERN);
    private static final MessageFormat OPENHAB_ZWAVE_DEVICE_ID_FORMAT = new MessageFormat(OPENHAB_ZWAVE_DEVICE_ID_PATTERN);
    private static final Map<String, Provider> providers = new HashMap<String, Provider>();
    private static final String DEFAULT_OPENHAB_SERVICE_ID = "info";
    private static final String DEFAULT_OPENHAB_RESOURCE_ID = "value";
    
    public OpenHabPacketReader(Mediator mediator) {
        super(mediator);
    }
    
    @Override
    public void parse(HttpPacket packet) throws InvalidPacketException {
        byte[] content = packet.getBytes();
        String sb = new String(content);
        Set<String> devices = new HashSet<String>();
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
                    setServiceProviderId(providerId);
                    isGoodbyeMessage(true);
                    configure();
                    Provider toBeDestroyedProvider = providers.get(providerId);
                    if (toBeDestroyedProvider == null) {
                        LOG.error("cannot destroy removed openhab provider {}: is not referenced?", providerId);
                    } else {
                        toBeDestroyedProvider.clearServices();
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
        process(resource, type, value);
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
        process(resourceFriendlyName, label);
        final Resource resourceStatus = getResource(openhaDeviceId, "status", "connected");
        process(resourceStatus, status.equals("ONLINE"));
        final Resource resourceDetail = getResource(openhaDeviceId, "status", "detail");
        process(resourceDetail, statusDetail);
    }
    
	private void process(Resource resource, Object value) {
		process(resource, OpenhabType.Default.name(), value);
	}

	private void process(Resource resource, String type, final Object value) {
		if (resource.setValue(value)) {
			String providerId = resource.getService().getProvider().getId();
			String serviceId = resource.getService().getId();
			Object data = OpenhabType.parseValue(type, value);
					
			setServiceProviderId(providerId);
			setServiceId(serviceId);
			setResourceId(resource.getId());
			setTimestamp(System.currentTimeMillis());
			setData(data);		
			configure();
			OpenHabPacketReader.LOG.debug("processed {}/{}/{}={}:{}", providerId, serviceId, resource.getId(), data, type);
		}
	}

	static String[] parseOpenhabPath(final String openhabDeviceId) throws ParseException {
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

    static Provider createProvider(final String[] openhabPath) {
        final String providerId = OPENHAB_ZWAVE_PROVIDER_ID_FORMAT.format(openhabPath);
        final Provider provider = new Provider(providerId);
        provider.getOrCreateResource(openhabPath[2], openhabPath[3]);
        return provider;
    }

    private Resource getResource(final String providerId, final String serviceId, final String resourceId) {
        Provider provider = providers.get(providerId);
        if (provider == null) {
            provider = new Provider(providerId);
            providers.put(providerId, provider);
        }
        return provider.getOrCreateResource(serviceId, resourceId);
    }
}
