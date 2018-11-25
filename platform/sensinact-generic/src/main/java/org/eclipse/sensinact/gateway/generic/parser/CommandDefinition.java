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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task;
import org.xml.sax.Attributes;

/**
 * Extended {@link XmlModelParsingContext}  dedicated to "command" 
 * XML node parsing context
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "type", field = "commandType")})
public final class CommandDefinition extends XmlModelParsingContext {
    private Task.CommandType commandType;
    private IdentifierDefinition identifierDefinition;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the CommandDefinition 
     * to be instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the "command" 
     * XML node 
     */
    public CommandDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * Defines the {@link Task.CommandType} of this CommandDefinition
     *
     * @param the {@link Task.CommandType} to be set
     */
    public void setCommandType(String commandType) {
        this.commandType = Task.CommandType.valueOf(commandType);
    }

    /**
     * Returns the {@link Task.CommandType} of this CommandDefinition
     *
     * @return the {@link Task.CommandType} of this CommandDefinition
     */
    public Task.CommandType getCommandType() {
        return this.commandType;
    }

    /**
     * Sets the {@link IdentifierDefinition} wrapping the bytes array
     * identifier of this CommandDefinition
     *
     * @param identifierDefinition the {@link IdentifierDefinition} wrapping the 
     * bytes array identifier of this CommandDefinition
     */
    public void setIdentifier(IdentifierDefinition identifierDefinition) {
        this.identifierDefinition = identifierDefinition;
    }

    /**
     * Returns this CommandDefinition's bytes array identifier
     *
     * @return this CommandDefinition's bytes array identifier
     */
    public byte[] getIdentifier() {
        return this.identifierDefinition.getIdentifier();
    }

    /**
     * Start of a "identifier" XML node parsing
     * 
     * @param atts the {@link Attributes} of the parsed XML node */
    public void identifierStart(Attributes atts) {
    	IdentifierDefinition identifierDefinition = new IdentifierDefinition(this.mediator, atts);
    	this.identifierDefinition = identifierDefinition;
    	super.setNext(identifierDefinition);
    }
}
