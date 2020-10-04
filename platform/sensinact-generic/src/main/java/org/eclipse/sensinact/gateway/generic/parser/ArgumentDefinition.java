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
