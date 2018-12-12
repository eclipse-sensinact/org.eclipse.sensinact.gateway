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
 * A {@link GenericTypeDefinition} defines a simple xml container element
 * potentially holding a target attribute
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class GenericTypeDefinition extends TypedDefinition<String> {
   
	/**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the GenericTypeDefinition 
     * to be instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the typed 
     * XML node 
     */
    public GenericTypeDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * Sets the canonical name of the Type specified
     * by the associated xml tag element
     *
     * @param type the canonical name of the Type specified
     *             by the associated xml tag element
     */
    public void setType(String type) {
        this.type = type;
    }
}
