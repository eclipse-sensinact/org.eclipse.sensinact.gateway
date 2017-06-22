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
package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.device.openhab.OpenHabDevice;
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
public class OpenHabPacket implements Packet
{
    private String processorId;
	private boolean isHelloMessage;
	private boolean isGoodByeMessage;
    private OpenHabDevice currentState;

    public OpenHabPacket(String processorId, boolean learn){
        this.processorId=processorId;
        this.learn=learn;
    }

    public OpenHabPacket(String processorId, boolean learn, boolean goodbye){
        this(processorId, learn);
        this.isGoodByeMessage=goodbye;
    }

    private boolean learn = false;

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
    	 return (getCurrentState()!=null)
    		?CommandType.GET:CommandType.SET;
    }

    @ServiceProviderID
    public String getServiceProviderIdentifier()
    {
        return processorId;
    }

    @ServiceID
    public String getServiceId()
    {
   	 	return (getCurrentState()!=null)
   		?"power":ServiceProvider.ADMINISTRATION_SERVICE_NAME;
    }
    
    @ResourceID
    public String getResourceId()
    {
   	 	return (getCurrentState()!=null)
   		?"power":"signal";
    }

    @Data
    public Object getData()
    {
   	 	return (getCurrentState()!=null)
   		?getCurrentState().getValue().equals("ON")?true:false:0;
    }

    @HelloMessage
    public boolean isHello()
    {
    	return this.isHelloMessage;
    }
    
    @GoodbyeMessage
    public boolean isGoodbye()
    {
    	return this.isGoodByeMessage;
    }

    public OpenHabDevice getCurrentState() {
        return currentState;
    }

    public void setCurrentState(OpenHabDevice currentState) {
        this.currentState = currentState;
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
