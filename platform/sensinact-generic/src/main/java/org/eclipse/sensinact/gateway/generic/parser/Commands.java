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
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.Task;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Wrapper of the set of {@link Task.CommandType}s of an {@link ExtModelConfiguration} 
 * mapped to their bytes array definition  
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Commands {
	
    final Map<Task.CommandType, byte[]> commands;

    /**
     * Constructor
     * 
     * @param commands the list of {@link CommandDefinition} describing
     * the {@link Task.CommandType}s and their bytes array definition
     */
    public Commands(List<CommandDefinition> commands) {
        Map<Task.CommandType, byte[]> preCommands = new HashMap<Task.CommandType, byte[]>();

        if (commands != null) {
            Iterator<CommandDefinition> iterator = commands.iterator();
            while (iterator.hasNext()) {
                CommandDefinition commandDefinition = iterator.next();
                preCommands.put(commandDefinition.getCommandType(), commandDefinition.getIdentifier());
            }
        }
        this.commands = Collections.unmodifiableMap(preCommands);
    }

    /**
     * Constructor
     * 
     * @param commands the Map of {@link Task.CommandType}s related to 
     * their bytes array definition
     */
    protected Commands(Map<Task.CommandType, byte[]> commands) {
        this.commands = Collections.unmodifiableMap(commands);
    }

    /**
     * Returns the bytes array identifier of the command whose
     * {@link Task.CommandType} is passed as parameter
     *
     * @param command the {@link Task.CommandType} for which to
     *                retrieve the bytes array identifier
     * @return
     */
    public byte[] getCommand(Task.CommandType command) {
        return this.commands.get(command);
    }

    /**
     * Returns the registered {@link Task.CommandType} whose
     * bytes array identifier is passed as parameter
     *
     * @param command the bytes array identifier for which to retrieve
     *                the {@link Task.CommandType}
     * @return the registered {@link Task.CommandType}
     * for the specified identifier
     */
    public Task.CommandType getCommand(byte[] command) {
        Task.CommandType commandType = null;
        Iterator<Task.CommandType> iterator = this.commands.keySet().iterator();
        while (iterator.hasNext()) {
            commandType = iterator.next();

            if (ExtModelConfiguration.compareBytesArrays(this.commands.get(commandType), command)) {
                break;
            }
            commandType = null;
        }
        return commandType;
    }
}
