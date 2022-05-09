/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.xml.sax.Attributes;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class GenericNameTypeValueDefinition extends NameTypeValueDefinition<String> {
   
	/**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the GenericNameTypeValueDefinition 
     * to be instantiated to interact with the OSGi host environment
     * @param atts  the {@link Attributes} data structure of the associated XML node 
     */
    GenericNameTypeValueDefinition(Mediator mediator, Attributes atts) {
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
    	super.type = type;
    }
}
