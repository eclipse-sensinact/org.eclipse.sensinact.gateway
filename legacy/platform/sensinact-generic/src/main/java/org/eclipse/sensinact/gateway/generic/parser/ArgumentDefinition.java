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
 * Extended {@link ResolvedNameTypeValueDefinition} dedicated to "argument" 
 * XML node parsing context
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes(value = {})
public class ArgumentDefinition extends ResolvedNameTypeValueDefinition  {

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the ArgumentDefinition to be
     * instantiated to interact with the OSGi host environment
     * @param atts     the {@link Attributes} data structure of the "argument" 
     * XML node
     */
    ArgumentDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }
}
