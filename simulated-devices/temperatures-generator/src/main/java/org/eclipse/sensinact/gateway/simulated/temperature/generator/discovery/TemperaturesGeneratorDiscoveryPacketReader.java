/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.temperature.generator.discovery;

import java.util.Random;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacket;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacketReader;

public class TemperaturesGeneratorDiscoveryPacketReader extends TemperaturesGeneratorAbstractPacketReader {
    
	class Generated {
		boolean isHello;
		boolean isGoodbye;		
		String serviceProviderId;
        String serviceId;
        String resourceId;
        String attributeId;
        Object data;
        Generated(boolean isHello, boolean isGoodbye,		
		String serviceProviderId, String serviceId, String resourceId,
        String attributeId, Object data){
        	this.isHello = isHello;
        	this.isGoodbye = isGoodbye;
        	this.serviceProviderId = serviceProviderId;
        	this.serviceId = serviceId;
        	this.resourceId = resourceId;
        	this.attributeId = attributeId;
        	this.data = data;
        }
	}
	
	Generated[] generateds;
	TemperaturesGeneratorDiscoveryPacket packet;
	Random random;
	int pos=0;
	
	/**
     * @param mediator the mediator
     */
    public TemperaturesGeneratorDiscoveryPacketReader() {
        super();
        this.random = new Random();
    }
    
    @Override
    public void load(TemperaturesGeneratorAbstractPacket packet) throws InvalidPacketException {
        this.packet = (TemperaturesGeneratorDiscoveryPacket) packet;
        this.generateds = new Generated[] {
            new Generated(true,false,this.packet.getServiceProvider(),null,null,null,null),	
        	new Generated(false,false,this.packet.getServiceProvider(),"admin","location",null,this.packet.getLocation()),
        	new Generated(false,false,this.packet.getServiceProvider(),"sensor","temperature",null,this.packet.getValue()),
        	new Generated(false,false,this.packet.getServiceProvider(),"sensor","temperature","category",random.nextInt(3)+1)
        };
    }
    
    @Override
    public void parse() throws InvalidPacketException {
    	if(this.packet == null)
    		return;
    	if(pos==this.generateds.length) {
    		this.packet = null;
    		super.configureEOF();
    		return;
    	}
        super.isHelloMessage(generateds[pos].isHello);
        super.isGoodbyeMessage(generateds[pos].isGoodbye);
        super.setServiceProviderId(generateds[pos].serviceProviderId);
        super.setServiceId(generateds[pos].serviceId);
        super.setResourceId(generateds[pos].resourceId);
        super.setAttributeId(generateds[pos].attributeId);
        super.setData(generateds[pos].data);
        pos+=1;
        super.configure();
    }
}
