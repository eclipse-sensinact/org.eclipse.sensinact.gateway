/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.xml.sax.Attributes;

/**
 * Method definition gathering the signatures of the
 * {@link AccessMethod} based on it
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "type", field = "type")})
@XmlEscaped(value = {"references"})
public class MethodDefinition extends XmlModelParsingContext implements Iterable<SignatureDefinition> {
    private List<SignatureDefinition> signatureDefinitions;
    private AccessMethod.Type type;

    SignatureDefinition current = null;

    /**
     * @param atts.getValue(qName)
     */
    public MethodDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
        this.signatureDefinitions = new ArrayList<SignatureDefinition>();
    }

    /**
     *
     */
    public void closeParameters() {
        if (current == null) {
            current = new SignatureDefinition(this.type);
        }
        int index = (this.signatureDefinitions.size() - 1);
        for (; index >= 0 && current.compareTo(this.signatureDefinitions.get(index)) < 0; index--) ;
        this.signatureDefinitions.add(index + 1, current);
        current = null;
    }

    /**
     * @param parameterDefinition
     */
    public void addParameter(ParameterDefinition parameterDefinition) {
        if (parameterDefinition == null) {
            return;
        }
        if (current == null) {
            current = new SignatureDefinition(this.type);
        }
        current.addParameter(parameterDefinition);
    }

    /**
     * @inheritDoc
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<SignatureDefinition> iterator() {
        return signatureDefinitions.iterator();
    }

    /**
     * Adds the {@link ReferenceDefinition} to the current {@link
     * SignatureDefinition} of this ActMethodDefinition
     *
     * @param referenceDefinition the {@link ReferenceDefinition} to add to the current
     *                            {@link SignatureDefinition}
     */
    public void addReferenceDefinition(ReferenceDefinition referenceDefinition) {
        if (current == null) {
            current = new SignatureDefinition(this.type);
        }
        this.current.addReferenceDefinition(referenceDefinition);
    }

    /**
     * Returns the {@link AccessMethod.Type} of the {@link AccessMethod}
     * described by this MethodDefinition
     *
     * @return the {@link AccessMethod.Type} of the described {@link
     * AccessMethod}
     */
    public AccessMethod.Type getType() {
        return this.type;
    }

    /**
     * Sets the {@link AccessMethod.Type} of the {@link AccessMethod}
     * described by this MethodDefinition
     *
     * @param type the {@link AccessMethod.Type} of the described {@link
     *             AccessMethod}
     */
    public void setType(String type) {
        this.type = AccessMethod.Type.valueOf(type);
    }

    /**
     * End of a "parameters" Element parsing
     */
    @Override
    public void end() {
        this.closeParameters();
        super.end();
    }

    /**
     * Start of a "parameter" Element parsing
     */
    public void parameterStart(Attributes atts) {
        ParameterDefinition parameterDefinition = new ParameterDefinition(this.mediator, atts);
        addParameter(parameterDefinition);
        super.setNext(parameterDefinition);
    }
    
    /**
     * Start of a "reference" Element parsing
     */
    public void referenceStart(Attributes atts) {
        ReferenceDefinition referenceDefinition = new ReferenceDefinition(this.mediator, atts);
        this.addReferenceDefinition(referenceDefinition);
        super.setNext(referenceDefinition);
    }   
}
