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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact;

import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.CommandID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.GoodbyeMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.HelloMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;


/**
 * Sensinact Packet
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Nascimento</a>
 */
public class MQTTPacket implements Packet
{
    private String processorId;
	private boolean isHelloMessage;
	private boolean isGoodByeMessage;
    private String payload;

    public MQTTPacket(String processorId, boolean learn){
        this.processorId=processorId;
        this.learn=learn;
    }

    public MQTTPacket(String processorId, boolean learn, String payload){
        this.processorId=processorId;
        this.learn=learn;
        this.payload=payload;

    }

    public MQTTPacket(String processorId, boolean learn, boolean goodbye){
        this(processorId, learn);
        this.isGoodByeMessage=goodbye;
    }

    private boolean learn = false;


    /**
    public MQTTBusClient getCurrentState() {
        return currentState;
    }

    public void setCurrentState(MQTTBusClient currentState) {
        this.currentState = currentState;
    }
     **/

    public String getPayload(){
        return payload;
    }
    
    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    public boolean isLearn() {
        return learn;
    }

    @CommandID
    public CommandType getCommand()
    {
    	 return CommandType.GET;
    }

    @ServiceProviderID
    public String getServiceProviderIdentifier()
    {
        return processorId;
    }

    @ServiceID
    public String getServiceId()
    {
   	 	return (getPayload()!=null)
   		?"info":ServiceProvider.ADMINISTRATION_SERVICE_NAME;
    }
    
    @ResourceID
    public String getResourceId()
    {
   	 	return (getPayload()!=null)
   		?"value":"signal";
    }

    @Data
    public Object getData()
    {
   	 	return (getPayload()!=null)?getPayload():0;
    }

    @HelloMessage
    public boolean isHello()
    {
    	return (getPayload()!=null)
    		?false:this.isHelloMessage;
    }
    
    @GoodbyeMessage
    public boolean isGoodbye()
    {
    	return (getPayload()!=null)
    		?false:this.isGoodByeMessage;
    }

    public void isHello(boolean isHelloMessage)
    {
    	this.isHelloMessage = isHelloMessage;
    }
    
    public void isGoodbye(boolean isGoodByeMessage)
    {
    	this.isGoodByeMessage = isGoodByeMessage;
    }    
}
