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
 * Command definition wrapping its type and its bytes array identifier
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "type", field = "commandType")})
public final class CommandDefinition extends XmlDefinition {
    private Task.CommandType commandType;
    private IdentifierDefinition identifierDefinition;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml command element
     */
    public CommandDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * @param commandType
     */
    public void setCommandType(String commandType) {
        this.commandType = Task.CommandType.valueOf(commandType);
    }

    /**
     * Sets the {@link IdentifierDefinition} wrapping the bytes array
     * identifier of this CommandDefinition
     *
     * @param identifierDefinition the {@link IdentifierDefinition} wrapping the bytes array
     *                             identifier of this CommandDefinition
     */
    public void setIdentifier(IdentifierDefinition identifierDefinition) {
        this.identifierDefinition = identifierDefinition;
    }

    /**
     * Returns the {@link Task.CommandType} of this
     * CommandDefinition
     *
     * @return the {@link Task.CommandType} of this
     * CommandDefinition
     */
    public Task.CommandType getCommandType() {
        return this.commandType;
    }

    /**
     * Returns this CommandDefinition's bytes array
     * identifier
     *
     * @return this CommandDefinition's bytes array
     * identifier
     */
    public byte[] getIdentifier() {
        return this.identifierDefinition.getIdentifier();
    }
}
