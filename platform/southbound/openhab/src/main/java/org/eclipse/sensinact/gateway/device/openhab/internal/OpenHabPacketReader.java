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
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.device.openhab.sensinact.Activator;
import org.eclipse.sensinact.gateway.device.openhab.sensinact.OpenHabMediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    private static final String DEFAULT_OPENHAB_SERVICE_ID = "info";
    private static final String DEFAULT_OPENHAB_RESOURCE_ID = "value";
    
    private static final Set<String> providers = new HashSet<String>();
    
	static String[] parseOpenhabPath(final String openhabDeviceId) throws ParseException {
        final String[] path = new String[4];
        Object[] parsedOpenhabPath = OPENHAB_ZWAVE_DEVICE_ID_FORMAT.parse(openhabDeviceId);
        for (int i = 0; i < 4; i++) {
            path[i] = parsedOpenhabPath[i].toString();
        }
        return path;
    }
    
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
                ((OpenHabMediator) super.mediator).updateBroker(openHabId, devices
                	).stream(
                	).forEach(providerId -> {this.processGoodbye(providerId);});
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
        processHello(openhaDeviceId);
        HierarchyDTO hierarchy = getHierarchy(openhaDeviceId);
        process(hierarchy.provider,hierarchy.service,hierarchy.resource, type, value);
        return openHabId;
    }

    private void parseThing(JSONObject jo) throws JSONException {
        final String uuid = jo.getString("UID");
        final JSONObject statusInfo = jo.getJSONObject("statusInfo");
        final String label = jo.getString("label");
        final String status = statusInfo.getString("status");
        final String statusDetail = statusInfo.getString("statusDetail");
        final String openhaDeviceId = uuid.replaceAll(":", "_");

        if (!providers.contains(openhaDeviceId)) {
        	providers.add(openhaDeviceId);
            processHello(openhaDeviceId);
        }
        process(openhaDeviceId, "admin", "friendlyName", null, label);
        process(openhaDeviceId, "status", "connected", null, status.equals("ONLINE"));
        process(openhaDeviceId, "status", "detail", null, statusDetail);
    }
    

	private void process(String providerId, String serviceId, String resource, String type, final Object value) {
		Object data = OpenhabType.parseValue(type==null?OpenhabType.Default.name():type, value);					
		setServiceProviderId(providerId);
		setServiceId(serviceId);
		setResourceId(resource);
		setTimestamp(System.currentTimeMillis());
		setData(data);		
		configure();
	}
	
	private void processHello(String providerId) {				
		setServiceProviderId(providerId);
		super.isHelloMessage(true);
		setTimestamp(System.currentTimeMillis());
		configure();
	}

	private void processGoodbye(String providerId) {				
		setServiceProviderId(providerId);
		super.isGoodbyeMessage(true);
		setTimestamp(System.currentTimeMillis());
		configure();
	}
        
    private HierarchyDTO getHierarchy(final String openhabDeviceId) {
        try {
            final String[] parsedOpenhabPath = parseOpenhabPath(openhabDeviceId);
            final String providerId = OPENHAB_ZWAVE_PROVIDER_ID_FORMAT.format(parsedOpenhabPath);
            if(!providers.contains(providerId)){
                providers.add(providerId);
                processHello(providerId);
            }
            return new HierarchyDTO(providerId,parsedOpenhabPath[2],parsedOpenhabPath[3]);
        } catch (Exception ex) {
            if(!providers.contains(openhabDeviceId)) {
                providers.add(openhabDeviceId);
                processHello(openhabDeviceId);
            }
            return new HierarchyDTO(openhabDeviceId,DEFAULT_OPENHAB_SERVICE_ID, DEFAULT_OPENHAB_RESOURCE_ID);
        }
    }
    
    private class HierarchyDTO {
    	public final String provider;
    	public final String service;
    	public final String resource;
    	
    	HierarchyDTO( String provider, String service, String resource){
    		this.provider = provider;
    		this.service = service;
    		this.resource = resource;
    	}
    }
}
