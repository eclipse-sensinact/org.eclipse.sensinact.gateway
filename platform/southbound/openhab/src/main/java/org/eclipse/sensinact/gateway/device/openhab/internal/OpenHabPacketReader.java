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
package org.eclipse.sensinact.gateway.device.openhab.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenHab2 Packet
 *
 * @author <a href="mailto:chirstophe.munillaO@cea.fr">Christophe Munilla</a>
 * @author sb252289
 */
public class OpenHabPacketReader extends SimplePacketReader<HttpPacket> {
	
	static final Logger LOG = LoggerFactory.getLogger(OpenHabPacketReader.class);
	
    static final MessageFormat TEMPERATURE_FORMAT = new MessageFormat("{0} °C");
    
    private static final String OPENHAB_ZWAVE_PROVIDER_ID_PATTERN = "{0}_node{1}";                                                          //zwave_device_07150a2a_node21
    private static final String OPENHAB_ZWAVE_DEVICE_ID_PATTERN = OPENHAB_ZWAVE_PROVIDER_ID_PATTERN + "_{2}_{3}";                         //zwave_device_07150a2a_node21_alarm_general
    protected static final MessageFormat OPENHAB_ZWAVE_PROVIDER_ID_FORMAT = new MessageFormat(OPENHAB_ZWAVE_PROVIDER_ID_PATTERN);
    private static final MessageFormat OPENHAB_ZWAVE_DEVICE_ID_FORMAT = new MessageFormat(OPENHAB_ZWAVE_DEVICE_ID_PATTERN);
    private static final String DEFAULT_OPENHAB_SERVICE_ID = "info";
    private static final String DEFAULT_OPENHAB_RESOURCE_ID = "value";
    
	static String[] parseOpenhabPath(final String openhabDeviceId) throws ParseException {
        final String[] path = new String[4];
        Object[] parsedOpenhabPath = OPENHAB_ZWAVE_DEVICE_ID_FORMAT.parse(openhabDeviceId);
        for (int i = 0; i < 4; i++) {
            path[i] = parsedOpenhabPath[i].toString();
        }
        return path;
    }
	
	private List<Processable> processables;
    private Map<String,Set<String>> devices;
	private JSONArray itemsArray = null;
	private JSONObject item = null;
	private int pos = 0;

	private Mediator mediator;
    
    public OpenHabPacketReader(Mediator mediator) {
        super();
        this.mediator=mediator;
    }
    
    @Override
    public void load(HttpPacket packet) throws InvalidPacketException {
    	this.item = null;
    	this.itemsArray = null;
    	this.processables = new ArrayList<>();
    	this.devices = new HashMap<>();
        byte[] content = packet.getBytes();
        if(content != null 	&& content.length > 0) {
        	String sb = new String(content);
        	try {
        		this.itemsArray = new JSONArray(sb);
        	} catch(Exception e) {
        		LOG.error(e.getMessage(),e);
        	}
    	}            
    }

    @Override
    public void parse() throws InvalidPacketException {
    	if(this.processables != null && !this.processables.isEmpty()) {
    		this.processables.remove(0).process();
    		return;
    	}
    	if(this.itemsArray == null || this.pos == this.itemsArray.length()) {
    		if(this.devices != null && this.devices.size() > 0) {
    			this.devices.entrySet().stream().forEach(e -> {
    				String openHabId = e.getKey(); 
    				Set<String> devices = e.getValue();
    				((OpenHabMediator) mediator).updateBroker(openHabId, devices
		            	).stream().forEach(s-> { this.processables.add(
		            	    new Processable(Mode.DELETE, new HierarchyDTO(s,null,null), 
		            	    		null, null));});
    			});
    			this.devices.clear();
    			this.parse();
    			return;
    		}    		
    		super.configureEOF();
    		return;
    	}
        try {
	        this.item = itemsArray.getJSONObject(pos);
	        String openhaDeviceId = this.item.getString("name");
            String type = this.item.getString("type");
            String link = this.item.getString("link");
            String value = this.item.getString("state");            
            try {
                parseItem(this.item.getString("name"), type, value);
                try {
                    URL url = new URL(link);
                    String openHabId = "openHab".concat(String.valueOf(
                    	(url.getHost() + url.getPort()).hashCode()));
                    Set<String> s = this.devices.get(openHabId);
                    if(s == null) {
                    	s = new HashSet<>();
                    	this.devices.put(openHabId, s);
                    }
                    s.add(openhaDeviceId);
                } catch (MalformedURLException e) {
                }
            } catch (Exception e1) {
                try {
                    parseThing(this.item);
                } catch (Exception e2) {
                    LOG.warn("OpenHab device error", e2);
                }
            } finally {
            	this.item = null;
            	this.pos+=1;
            }
            if(!this.processables.isEmpty()){
            	parse();
            	return;
            }            	
        } catch (JSONException e) {
        	super.configureEOF();
            throw new InvalidPacketException(e);   
        }
        //it should not happen
    	super.configureEOF();
    }
    
    private void parseItem(String openhaDeviceId, String type, String value) 
    		throws JSONException {
        HierarchyDTO hierarchy = getHierarchy(openhaDeviceId);
        this.processables.add(new Processable(Mode.UPDATE,hierarchy,type,value));
    }

    private void parseThing(JSONObject jo) throws JSONException {
        final String uuid = jo.getString("UID");
        final JSONObject statusInfo = jo.getJSONObject("statusInfo");
        final String label = jo.getString("label");
        final String status = statusInfo.getString("status");
        final String statusDetail = statusInfo.getString("statusDetail");
        final String openhaDeviceId = uuid.replaceAll(":", "_");

        this.processables.add(new Processable(Mode.UPDATE,new HierarchyDTO(
        		openhaDeviceId, "admin", "friendlyName"),null,label));
        this.processables.add(new Processable(Mode.UPDATE,new HierarchyDTO(
        		openhaDeviceId, "status", "connected"),null,status.equals("ONLINE")));
        this.processables.add(new Processable(Mode.UPDATE,new HierarchyDTO(
        		openhaDeviceId, "status", "detail"),null,statusDetail));
    }
        
    private HierarchyDTO getHierarchy(final String openhabDeviceId) {
        try {
            final String[] parsedOpenhabPath = parseOpenhabPath(openhabDeviceId);
            final String providerId = OPENHAB_ZWAVE_PROVIDER_ID_FORMAT.format(parsedOpenhabPath);
            return new HierarchyDTO(providerId,parsedOpenhabPath[2],parsedOpenhabPath[3]);
        } catch (Exception ex) {
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

    enum Mode {
        UPDATE,
        DELETE;
    }
    
    private class Processable {
    	
    	public final Mode mode;
    	public final HierarchyDTO hierarchyDTO;
    	public final String type;
    	public final Object value;

    	Processable(Mode mode, HierarchyDTO hierarchyDTO, String type, Object value){
    		this.mode = mode;
    		this.hierarchyDTO = hierarchyDTO;
    		this.type = type;
    		this.value = value;
    	}
    	
    	void process() {
    		switch(mode) {
			case DELETE:					
	    		setServiceProviderId(hierarchyDTO.provider);
	    		isGoodbyeMessage(true);
	    		setTimestamp(System.currentTimeMillis());
	    		configure();
				break;
			case UPDATE:
	    		Object data = OpenhabType.parseValue(type==null?
	    		    OpenhabType.Default.name():type, value);
	    		setServiceId(hierarchyDTO.service);
	    		setResourceId(hierarchyDTO.resource);
	    		setTimestamp(System.currentTimeMillis());
	    		setData(data);		
	    		configure();
				break;
			default:
				break;    		
    		}
    	}
    }
}
