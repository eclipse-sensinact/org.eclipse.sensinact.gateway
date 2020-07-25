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

package org.eclipse.sensinact.gateway.sthbnd.ttn.model;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.sthbnd.ttn.packet.PayloadDecoder;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TtnUplinkPayload extends TtnPacketPayload {

    private static final String PAYLOAD_DECODER = "(objectClass=" + PayloadDecoder.class.getCanonicalName() + ")";

    private static final byte PADDING = 127;
    private static final byte[] decodeMap = new byte[128]; 

    static {
	    String RAW = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
	    byte[] map = new byte[128];
	    int i;
	    for (i = 0; i < 128; i++) {
	      	map[i] = (byte) RAW.indexOf(i);
	    }
	    map['='] = PADDING;
	    System.arraycopy(map,0,decodeMap,0,128);
	 }
    
    private static int length(String text) {
        final int len = text.length();
        int j = len - 1;
        for (; j >= 0;) {
            byte code = decodeMap[text.charAt(j)];
            if (code == PADDING){
            	j-=1;
                continue;
        	}
            if (code == -1)
                return text.length() / 4 * 3;
            break;
        }
        j++;
        int padSize = len - j;
        if (padSize > 2)
            return text.length() / 4 * 3;
        return text.length() / 4 * 3 - padSize;
    }
    
    private static byte[] parseBase64Binary(String text) {
        final int buflen = length(text);
        final byte[] out = new byte[buflen];
        int o = 0;

        final int len = text.length();
        int i;

        final byte[] quadruplet = new byte[4];
        int q = 0;

        // convert each quadruplet to three bytes.
        for (i = 0; i < len; i++) {
            char ch = text.charAt(i);
            byte v = decodeMap[ch];

            if (v != -1)            	
                quadruplet[q++] = v;

            if (q == 4) {
                // quadruplet is now filled.
                out[o++] = (byte) ((quadruplet[0] << 2) | (quadruplet[1] >> 4));
                if (quadruplet[2] != PADDING) {
                    out[o++] = (byte) ((quadruplet[1] << 4) | (quadruplet[2] >> 2));
                }
                if (quadruplet[3] != PADDING) {
                    out[o++] = (byte) ((quadruplet[2] << 6) | (quadruplet[3]));
                }
                q = 0;
            }
        }
        if (buflen == o)
            return out;
        byte[] nb = new byte[o];
        System.arraycopy(out, 0, nb, 0, o);
        return nb;
    }
    
    private final Mediator mediator;
    private final String applicationId;
    private final String deviceId;
    private final String hardwareSerial;
    private final int port;
    private final int counter;
    private final boolean confirmed;
    private final boolean isRetry;
    private final byte[] payloadRaw;
    private final TtnMetadata metadata;

    public TtnUplinkPayload(Mediator mediator, String applicationId, String deviceId, String hardwareSerial, 
        int port, int counter,  boolean confirmed, boolean isRetry, String payloadRaw, TtnMetadata metadata) {
        this.mediator = mediator;
        this.applicationId = applicationId;
        this.deviceId = deviceId;
        this.hardwareSerial = hardwareSerial;
        this.port = port;
        this.counter = counter;
        this.confirmed = confirmed;
        this.isRetry = isRetry;
        this.metadata = metadata;
        if(payloadRaw != null) {
	        Object parse64BinaryObj = mediator.getProperty("parseBase64Binary");	        
	        boolean parse64Binary = parse64BinaryObj==null?false:Boolean.valueOf(String.valueOf(parse64BinaryObj));
	        if(parse64Binary)
	        	this.payloadRaw =  parseBase64Binary(payloadRaw);
	        else {
	        	this.payloadRaw = payloadRaw.getBytes();
	        }  
        } else
        	this.payloadRaw = null;
    }

    public TtnUplinkPayload(Mediator mediator, JSONObject json) throws JSONException {
        this.mediator = mediator;
        this.applicationId = json.getString("app_id");
        this.deviceId = json.getString("dev_id");
        this.hardwareSerial = json.getString("hardware_serial");
        this.port = json.getInt("port");
        this.counter = json.getInt("counter");
        this.confirmed = json.optBoolean("confirmed");
        this.isRetry = json.optBoolean("is_retry");
        this.metadata = new TtnMetadata(json.getJSONObject("metadata"));
        
        String payload = json.optString("payload_raw");
        if(payload != null) {
	        Object parse64BinaryObj = mediator.getProperty("parseBase64Binary");
	        boolean parse64Binary = parse64BinaryObj==null?false:Boolean.valueOf(String.valueOf(parse64BinaryObj));
	        if(parse64Binary)
	        	this.payloadRaw =  parseBase64Binary(json.optString("payload_raw"));
	        else 
	        	this.payloadRaw = payload.getBytes();
        } else 
        	this.payloadRaw = null;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getHardwareSerial() {
        return hardwareSerial;
    }

    public int getPort() {
        return port;
    }

    public int getCounter() {
        return counter;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public byte[] getPayloadRaw() {
        return payloadRaw;
    }

    public TtnMetadata getMetadata() {
        return metadata;
    }
    

    @Override
    public List<TtnSubPacket> getSubPackets() {
        List<TtnSubPacket> subPackets = new ArrayList<>();

        subPackets.add(new TtnSubPacket<>("system", "frequency", null,null,metadata.getFrequency()));
        subPackets.add(new TtnSubPacket<>("system", "modulation", null,null,metadata.getModulation()));
        subPackets.add(new TtnSubPacket<>("system", "data_rate", null,null,metadata.getDataRate()));
        subPackets.add(new TtnSubPacket<>("system", "coding_rate", null,null,metadata.getCodingRate()));
        subPackets.add(new TtnSubPacket<>("system", "data", null, null, payloadRaw));

        if (payloadRaw != null) {
            try {
                ServiceReference[] serviceReferences = this.mediator.getContext().getServiceReferences((String) null, PAYLOAD_DECODER);

                if (serviceReferences != null) {
                    for (ServiceReference serviceReference : serviceReferences) {
                        Map<String, Object> decodedPayload = ((PayloadDecoder) mediator.getContext().getService(serviceReference)).decodeRawPayload(payloadRaw);

                        if(!decodedPayload.isEmpty()) {
                            for(Map.Entry<String, Object> payloadMap : decodedPayload.entrySet()) {
                                if(payloadMap.getKey().equals("position")){
                                    subPackets.add(new TtnSubPacket<>("admin", "location", null,null,String.valueOf(payloadMap.getValue())));
                                }
                                String key = payloadMap.getKey();
                                String[] keyElements = UriUtils.getUriElements(key);
                                subPackets.add(new TtnSubPacket<>("content", keyElements[0],keyElements.length>1?keyElements[1]:null,
                                	keyElements.length>2?keyElements[2]:null,String.valueOf(payloadMap.getValue())));
                            }
                            break;
                        }
                    }
                }
            } catch (InvalidSyntaxException e) {
                e.printStackTrace();
            }
        }
        return subPackets;
    }
}
