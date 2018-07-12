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
import org.xml.sax.Attributes;

/**
 * Identifier definition wrapping the identifier array of bytes as well as
 * its encoding allowing to extract it from the its definition
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlElement(tag = "identifier", field = "identifier")
@XmlAttributes({@XmlAttribute(attribute = "xsi:type", field = "encoding")})
final class IdentifierDefinition extends XmlDefinition {
    private String encoding;
    private byte[] identifier;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml identifier element
     */
    public IdentifierDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * Sets the encoding type of the xml identifier element
     *
     * @param encoding the encoding type of the xml identifier element
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Sets the string representation of the identifier bytes array
     *
     * @param identifier the string representation of the identifier bytes array
     */
    public void setIdentifier(String identifier) {
        char[] identifierChars = identifier.toCharArray();

        if (XmlResourceConfigHandler.STRING_COMMAND_TYPE.equals(this.encoding)) {
            this.identifier = new byte[identifierChars.length];
            for (int i = 0; i < identifierChars.length; i++) {
                this.identifier[i] = (byte) Character.codePointAt(identifierChars, i);
            }
        } else if (XmlResourceConfigHandler.BINARY_COMMAND_TYPE.equals(this.encoding)) {
            this.identifier = new byte[identifierChars.length / 2];
            int pos = 0;
            for (int i = 0; i < identifierChars.length; i += 2) {
                this.identifier[pos++] = Byte.parseByte(new String(new char[]{identifierChars[i], identifierChars[i + 1]}), 16);
            }
        } else {
            if (super.mediator.isWarningLoggable()) {
                super.mediator.warn("Invalid command encoding");
            }
        }
    }

    /**
     * Returns a copy of the wrapped identifier bytes array
     */
    protected byte[] getIdentifier() {
        if (this.identifier == null) {
            if (super.mediator.isWarningLoggable()) {
                super.mediator.warn("No IdentifierDefinition defined");
            }
            return null;
        }
        byte[] newIdentifierBytesArray = new byte[this.identifier.length];
        for (int n = 0; n < this.identifier.length; n++) {
            newIdentifierBytesArray[n] = this.identifier[n];
        }
        return newIdentifierBytesArray;
    }
}
