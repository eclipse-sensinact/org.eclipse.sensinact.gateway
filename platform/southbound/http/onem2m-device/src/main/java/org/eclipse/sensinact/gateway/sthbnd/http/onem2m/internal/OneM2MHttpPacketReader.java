/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.onem2m.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneM2MHttpPacketReader extends SimplePacketReader<HttpPacket> {
	private static final Logger LOG = LoggerFactory.getLogger(PacketReader.class);

    public static final String DEFAULT_SERVICE_NAME = "container";
    
    class OneM2MHttpSubPacket {
		String serviceProvider;
		String service;
		String resource;
		Object data;
	}
    
    private List<OneM2MHttpSubPacket> subPackets;
    private HttpPacket packet;
    /**
     */
    public OneM2MHttpPacketReader() {
        super();
    }

    @Override
    public void load(HttpPacket packet) throws InvalidPacketException {
    	this.packet = packet;
    	this.subPackets = new ArrayList<OneM2MHttpSubPacket>();
    }

    @Override
    public void parse() throws InvalidPacketException {
    	if(this.packet == null) {
    		super.configureEOF();
    		return;
    	}
    	if(this.subPackets.isEmpty()) {
	        try {
	            JSONObject content = new JSONObject(new String(packet.getBytes()));
	            if (LOG.isDebugEnabled()) {
	                LOG.debug(content.toString());
	            }
	            if (content.has("m2m:uril")) {
	                String[] uris = content.getString("m2m:uril").split(" ");
	                for (String uri : uris) {
	                    String[] elements = uri.split("/");
	                    if (elements.length >= 3) {
	                    	OneM2MHttpSubPacket sub = new OneM2MHttpSubPacket();
;	                        if (elements.length >= 5 && elements.length < 6) {
	                            sub.resource = elements[4];
	                            sub.service = elements[3];
	                        } else if ("admin".equalsIgnoreCase(elements[3])) {
	                        	sub.service = elements[3];
	                        } else {
	                        	sub.resource = elements[3];
	                            sub.service = DEFAULT_SERVICE_NAME;
	                        }
							sub.serviceProvider = elements[2];
	                        this.subPackets.add(sub);
	                    }
	                }
	            }
	        } catch (Exception e) {
                LOG.error(e.getMessage(), e);
        		super.configureEOF();
                throw new InvalidPacketException(e);
	        }
	        if(!this.subPackets.isEmpty())
	        	parse();
	     } else {
        	OneM2MHttpSubPacket sub = this.subPackets.remove(0);
        	super.setServiceProviderId(sub.serviceProvider);
            super.setServiceId(sub.service);
            super.setResourceId(sub.resource);
            super.setData(sub.data);
            if(this.subPackets.isEmpty())
            	this.packet = null;
            super.configure();
            return;
	    } 
    	super.configureEOF();
    }
}