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
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * The extended {@link Resource} interface type of a {@link PolicyDefinition}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlElement(tag = "className", field = "resourceClassType")
final class ResourceClassDefinition extends XmlModelParsingContext {
	
	private static final Logger LOG = LoggerFactory.getLogger(ResourceClassDefinition.class);
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
            LOG.error(e.getMessage(), e);
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
