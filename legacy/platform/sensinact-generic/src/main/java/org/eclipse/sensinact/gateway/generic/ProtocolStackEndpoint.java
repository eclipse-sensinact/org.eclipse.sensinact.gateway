/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.parser.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ProtocolStackEndpoint<P extends Packet> implements TaskTranslator {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProtocolStackEndpoint.class);

    /**
     * the {@link Connector} connected to this ProtocolStackConnector instance
     */
    protected Connector<P> connector;
    
    /**
     * the set of available commands for the
     * connected {@link Connector}
     */
    protected Commands commands;
    
	/**
	 * Map of the subscription identifiers to the subscriber ones  
	 */
	protected Map<String,String> subscriptions;
	
	/**
	 * {@link SubscriptionHandlerDelegate} in charge of providing the appropriate
	 * and extended {@link AbstractSubscribeTaskWrapper} and {@link AbstractUnsubscribeTaskWrapper} types
	 */
	protected SubscriptionHandlerDelegate subscriptionHandlerDelegate;
	

    public ProtocolStackEndpoint() {
    	this.subscriptions = new HashMap<>();
    }
    
    /**
     * Connects this ProtocolStackConnector to the {@link ExtModelConfiguration} passed 
     * as parameter.
     *
     * @param manager the {@link ExtModelConfiguration} to connect to
     * 
     * @throws InvalidProtocolStackException
     */
	public void connect(ExtModelConfiguration<P> manager) throws InvalidProtocolStackException {
        this.commands = manager.getCommands();
        if ((this.connector = manager.connect(this)) != null) {
            Iterator<Map.Entry<String, String>> iterator = manager.getFixedProviders().entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                try {
                    this.connector.addModelInstance(entry.getValue(), entry.getKey());
                } catch (InvalidServiceProviderException e) {
                    throw new InvalidProtocolStackException(e);
                }
            }
        }
    }

	/**
	 * Defines the {@link SubscriptionHandlerDelegate} in charge of providing wrapper types for subscribe and
	 * unsubscribe tasks
	 * 
	 * @param subscriptionHandlerDelegate the {@link SubscriptionHandlerDelegate} to be used
	 */
	public void setSubscriptionHandlerDelegate(SubscriptionHandlerDelegate subscriptionHandlerDelegate) {
		this.subscriptionHandlerDelegate = subscriptionHandlerDelegate;
	}
	
    /**
     * Defines the String subscription identifier of the {@link AbstractUnsubscribeTaskWrapper} 
     * passed as parameter
     * 
     * @param task the {@link AbstractUnsubscribeTaskWrapper} to define the subscription identifier
     * of
     */
    public void setSubsciptionIdentifier(UnsubscribeTaskWrapper task) {
    	task.setSubscriptionId(this.subscriptions.get(task.getTargetId()));
    }
    
    /**
     * Returns the {@link AbstractUnsubscribeTaskWrapper} type to be used to wrapped unsubscribe 
     * {@link Task}
     * 
     * @return the {@link AbstractUnsubscribeTaskWrapper} type to be used
     */
    protected Class<? extends UnsubscribeTaskWrapper> getUnsubscribeTaskWrapperType(){
    	if(this.subscriptionHandlerDelegate!=null) 
    		return this.subscriptionHandlerDelegate.getUnsubscribeTaskWrapperType();
    	return null;
    }
    
    /**
     * Maps the String subscription identifier to the String subscriber one, both provided
     * by the {@link AbstractSubscribeTaskWrapper} wrapping a subscribe {@link Task}
     * 
     * @param task the subscribe {@link Task} wrapped into an {@link AbstractSubscribeTaskWrapper}
     */
    public void registerSubsciptionIdentifier(SubscribeTaskWrapper task) {
    	this.subscriptions.put(task.getTargetId(), task.getSubscriptionId());
    }

    /**
     * Returns the {@link AbstractSubscribeTaskWrapper} type to be used to wrapped subscribe 
     * {@link Task}
     * 
     * @return the {@link AbstractSubscribeTaskWrapper} type to be used
     */
    protected Class<? extends SubscribeTaskWrapper> getSubscribeTaskWrapperType(){
    	if(this.subscriptionHandlerDelegate!=null) 
    		return this.subscriptionHandlerDelegate.getSubscribeTaskWrapperType();
    	return null;
    }
    
    /**
     * Returns the {@link Task} passed as parameter wrapped into the appropriate
     * {@link TaskWrapper} if it holds an SUBSCRIBE or UNSUBSCRIBE {@link CommandType}
     * and if it is not already an {@link TaskWrapper} instance
     * 
     * @param <T> the handled {@link Task} type
     * 
     * @param type the handled {@link Task} type
     * @param task the {@link Task} to be wrapped 
     * 
     * @return the {@link Task} passed as parameter wrapped into the appropriate
     * {@link TaskWrapper}
     */
    protected <T extends Task> T wrap(Class<T> type , T task) {
    	if(task == null)
    		return null;
    	if(task instanceof TaskWrapper)
    		return task;
    	T _task = null;
        if(task.getCommand().equals(CommandType.SUBSCRIBE)) {
	       	Class<? extends SubscribeTaskWrapper> wrapperType = this.getSubscribeTaskWrapperType();
	       	if(wrapperType != null) { 
		         	try {
		 				_task =  (T) wrapperType.getDeclaredConstructor(new Class<?>[]{Task.class, ProtocolStackEndpoint.class}
		 				).newInstance(new Object[] {task,this});
		 			} catch (Exception e) {
		 				e.printStackTrace();
		 				_task = null;
		 			}
	       	}
       } else if(task.getCommand().equals(CommandType.UNSUBSCRIBE)) {
	       	Class<? extends UnsubscribeTaskWrapper> wrapperType = this.getUnsubscribeTaskWrapperType();
	       	if(wrapperType != null) { 
		         	try {
		 				_task =  (T) wrapperType.getDeclaredConstructor(new Class<?>[]{Task.class, ProtocolStackEndpoint.class}
		 				).newInstance(new Object[] {task,this});
		 			} catch (Exception e) {
		 				e.printStackTrace();
		 				_task = null;
		 			}
	       	}
       } 
       if(_task==null) 
       	_task = task;
       return _task;
    }
    
    
    /**
     * Processes the {@link Packet} passed as parameter
     *
     * @param packet the {@link Packet} to process
     * 
     * @throws InvalidPacketException
     */
    public void process(P packet) throws InvalidPacketException {
        if (connector == null) {
        	ProtocolStackEndpoint.LOG.debug("No processor connected");
            return;
        }
        connector.process(packet);
    }

    /**
     * Returns the bytes array command for the {@link CommandType} passed as parameter
     *
     * @param commandType the {@link CommandType} for which to retrieve the bytes array command
     * 
     * @return the bytes array command for the specified {@link CommandType}
     */
    public byte[] getCommand(CommandType commandType) {
        if (this.commands == null) {
            return new byte[0];
        }
        return this.commands.getCommand(commandType);
    }

    /**
     * Joins each bytes array contained by the arrays argument into a single one delimiting 
     * each others by the delimiter argument bytes array
     *
     * @param arrays    the array of bytes arrays to join
     * @param delimiter the delimiters bytes array
     * 
     * @return the bytes array resulting of the junction of the bytes arrays contained by the 
     * arrays argument
     */
    public static byte[] join(byte[][] arrays, byte[] delimiter) {
        byte[] joined = new byte[0];

        int index = 0;
        int length = 0;
        int delimiterLength = delimiter == null ? 0 : delimiter.length;

        for (; index < arrays.length; index++) {
            if (arrays[index] != null && arrays[index].length > 0) {
                if (delimiterLength > 0 && length > 0) {
                    joined = Arrays.copyOf(joined, (length + delimiterLength));
                    System.arraycopy(delimiter, 0, joined, length, delimiterLength);
                    length += delimiterLength;
                }
                joined = Arrays.copyOf(joined, (length + arrays[index].length));
                System.arraycopy(arrays[index], 0, joined, length, arrays[index].length);
                length += arrays[index].length;
            }
        }
        return joined;
    }

    /**
     * Converts the parameter object argument into a bytes array and returns
     * it. If the parameter object is of an {@link Array} type, its distinct
     * elements are separated using the delimiter bytes array argument
     *
     * @param parameter the parameter object to convert into a bytes array
     * @param delimiter the array of byte delimiting the distinct elements
     * of the parameter argument if this last one is of an {@link Array} type
     * 
     * @return the parameter argument object converted into a bytes array
     */
    public static byte[] formatParameter(Object parameter, byte[] delimiter) {
        if (parameter == null) {
            return null;
        }
        byte[] valueBytes = new byte[0];

        if (String.class.isAssignableFrom(parameter.getClass())) {
            valueBytes = ((String) parameter).getBytes();

        } else if (JsonObject.class.isAssignableFrom(parameter.getClass())) {
            valueBytes = ((JsonObject) parameter).toString().getBytes();

        } else if (JsonArray.class.isAssignableFrom(parameter.getClass())) {
            valueBytes = ((JsonArray) parameter).toString().getBytes();
        } else if (Parameter.class.isAssignableFrom(parameter.getClass())) {
            valueBytes = formatParameter(((Parameter) parameter).getValue(), delimiter);

        } else if (parameter.getClass().isArray()) {
            for (int j = 0; j < Array.getLength(parameter); j++) {
                Object param = Array.get(parameter, j);
                byte[] formated = formatParameter(param, delimiter);

                if (formated != null && formated.length > 0) {
                    int length = valueBytes.length;

                    if (valueBytes.length > 0 && delimiter != null && delimiter.length > 0) {
                        valueBytes = Arrays.copyOfRange(valueBytes, 0, length + delimiter.length);
                        System.arraycopy(delimiter, 0, valueBytes, length, delimiter.length);
                        length += delimiter.length;
                    }
                    valueBytes = Arrays.copyOfRange(valueBytes, 0, length + formated.length);
                    System.arraycopy(formated, 0, valueBytes, length, formated.length);
                }
            }
        } else if (byte.class.equals(parameter.getClass()) || Byte.class.equals(parameter.getClass())) {
            valueBytes = new byte[]{((Byte) parameter).byteValue()};

        } else {
            valueBytes = String.valueOf(parameter).getBytes();
        }
        return valueBytes;
    }

    /**
     * Stops this ProtocolStackEndpoint and its associated {@link Connector}
     */
    public void stop() {
        if (this.connector != null) 
            this.connector.stop();
        else 
        	ProtocolStackEndpoint.LOG.debug("No processor connected");
    }
}
