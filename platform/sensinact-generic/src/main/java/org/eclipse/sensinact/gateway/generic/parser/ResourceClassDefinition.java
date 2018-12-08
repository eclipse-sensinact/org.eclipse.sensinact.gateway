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
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.xml.sax.Attributes;

/**
 * The extended {@link Resource} interface type of a {@link PolicyDefinition}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlElement(tag = "className", field = "resourceClassType")
final class ResourceClassDefinition extends XmlModelParsingContext {
    private Class<? extends ExtResourceImpl> resourceClassType;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml identifier element
     */
    public ResourceClassDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * Defines the name of the extended {@link ResourceImpl}
     * type wrapped by this ResourceClassDefinition
     *
     * @param className the extended {@link ResourceImpl} type name
     */
    void setResourceClassType(String className) {
    	if(className == null || className.length()==0) {
    		return;
    	}
        try {
            this.resourceClassType = (Class<? extends ExtResourceImpl>) 
            		super.mediator.getClassLoader().loadClass(className);

            if (this.resourceClassType.isInterface()) {
                this.resourceClassType = null;
            }
        } catch (ClassNotFoundException e) {
            super.mediator.error(e);
        }
    }

    /**
     * Returns the extended {@link ResourceImpl} type
     * wrapped by this ResourceClassDefinition
     */
    public Class<? extends ExtResourceImpl> getResourceClassType() {
        return this.resourceClassType;
    }
}
