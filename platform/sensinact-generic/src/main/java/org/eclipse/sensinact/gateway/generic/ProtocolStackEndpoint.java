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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.parser.Commands;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ProtocolStackEndpoint<P extends Packet> implements TaskTranslator {
    /**
     * the {@link Mediator} that will be used by the
     * ProtocolStackConnector to instantiate to interact with
     * the OSGi host environment
     */
    protected final Mediator mediator;

    /**
     * the {@link Connector} connected to this
     * ProtocolStackConnector instance
     */
    protected Connector<P> connector;
    /**
     * the set of available commands for the
     * connected {@link Connector}
     */
    protected Commands commands;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} that will be used by the
     *                 ProtocolStackConnector to instantiate
     *                 to interact with the OSGi host environment
     */
    public ProtocolStackEndpoint(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Connects this ProtocolStackConnector to the
     * {@link ExtModelConfiguration} passed as parameter.
     *
     * @param manager the {@link ExtModelConfiguration} to
     *                connect to
     * @throws InvalidProtocolStackException
     */
    public void connect(ExtModelConfiguration manager) throws InvalidProtocolStackException {
        this.commands = manager.getCommands();
        if ((this.connector = manager.<P>connect(this)) != null) {
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
     * Processes the {@link Packet} passed as
     * parameter
     *
     * @param packet the {@link Packet} to process
     * @throws InvalidPacketException
     * @throws InvalidServiceProviderException
     */
    public void process(P packet) throws InvalidPacketException {
        if (connector == null) {
            this.mediator.debug("No processor connected");
            return;
        }
        connector.process(packet);
    }

    /**
     * Returns the bytes array command for the {@link
     * CommandType} passed as parameter
     *
     * @param commandType the {@link CommandType} for which
     *                    to retrieve the bytes array command
     * @return the bytes array command for the specified {@link
     * CommandType}
     */
    public byte[] getCommand(CommandType commandType) {
        if (this.commands == null) {
            return new byte[0];
        }
        return this.commands.getCommand(commandType);
    }

    /**
     * Joins each bytes array contained by the arrays argument
     * into a single one delimiting each others by the delimiter
     * argument bytes array
     *
     * @param arrays    the array of bytes arrays to join
     * @param delimiter the delimiters bytes array
     * @return the bytes array resulting of the junction
     * of the bytes arrays contained by the arrays
     * argument
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
     *                  of the parameter argument if this last one is of an
     *                  {@link Array} type
     * @return the parameter argument object converted into a bytes array
     */
    public static byte[] formatParameter(Object parameter, byte[] delimiter) {
        if (parameter == null) {
            return null;
        }
        byte[] valueBytes = new byte[0];

        if (String.class.isAssignableFrom(parameter.getClass())) {
            valueBytes = ((String) parameter).getBytes();

        } else if (JSONObject.class.isAssignableFrom(parameter.getClass())) {
            valueBytes = ((JSONObject) parameter).toString().getBytes();

        } else if (JSONArray.class.isAssignableFrom(parameter.getClass())) {
            valueBytes = ((JSONArray) parameter).toString().getBytes();
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
}
