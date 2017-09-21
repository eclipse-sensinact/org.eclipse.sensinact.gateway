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

import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;


/**
 * Sensinact Packet
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MQTTPacket implements Packet
{

    @ResourceID
    private String resourceId;

    @ServiceID
    private String serviceId;

    @ServiceProviderID
    private String processorId;

    @HelloMessage
    private Boolean isHelloMessage=false;

    @GoodbyeMessage
    private Boolean isGoodByeMessage=false;

    @Data
    private String payload;

    private Provider currentState;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public MQTTPacket(String processorId){
        this.processorId=processorId;
    }

    public MQTTPacket(String processorId,Boolean hello){
        this.processorId=processorId;
        this.isHelloMessage=hello;
    }

    public void setInfo(String service,String resourceId,String data){
        this.serviceId=service;
        this.resourceId=resourceId;
        this.payload=data;
    }

    public MQTTPacket(String processorId, String serviceId, String resourceId, String data){
        this.processorId=processorId;
        this.serviceId=serviceId;
        this.resourceId=resourceId;
        this.payload=data;
    }

    public MQTTPacket(String processorId, String serviceId, String resourceId, String data,Boolean hello,Boolean goodbye){
        this(serviceId,resourceId,processorId,data);
        this.isHelloMessage=hello;
        this.isGoodByeMessage=goodbye;
    }
    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    public String getServiceProviderIdentifier() {
        return processorId;
    }

    public void isHello(boolean isHelloMessage)
    {
    	this.isHelloMessage = isHelloMessage;
    }

    public boolean isHello()
    {
    	return this.isHelloMessage;
    }
    
    public void isGoodbye(boolean isGoodByeMessage)
    {
    	this.isGoodByeMessage = isGoodByeMessage;
    }    
    
    public boolean isGoodbye()
    {
    	return this.isGoodByeMessage;
    }

    public Provider getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Provider currentState) {
        this.currentState = currentState;
    }

    public String getPayload(){
        return payload;
    }
}
