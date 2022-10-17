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
import org.eclipse.sensinact.gateway.core.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * The extended {@link Resource} interface type of a {@link PolicyDefinition}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlElement(tag = "interfaceName", field = "resourceInterfaceType")
final class ResourceInterfaceDefinition extends XmlModelParsingContext {
	
	private static final Logger LOG = LoggerFactory.getLogger(ResourceInterfaceDefinition.class);
    private Class<? extends Resource> resourceInterfaceType;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml identifier element
     */
    public ResourceInterfaceDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * Defines the name of the extended {@link Resource} interface
     * type wrapped by this ResourceInterfaceDefinition
     *
     * @param interfaceName the extended {@link Resource} interface type
     *                      name
     */
    @SuppressWarnings("unchecked")
    void setResourceInterfaceType(String interfaceName) {
        if(interfaceName == null || interfaceName.length()==0) {
        	return;
        }
    	this.resourceInterfaceType = (Class<? extends Resource>) this.resolveInterfaceType(interfaceName);
        if (resourceInterfaceType != null) {
            return;
        }        
        try {
            this.resourceInterfaceType = (Class<? extends Resource>) super.mediator.getContext().getBundle().loadClass(interfaceName);

            if (!this.resourceInterfaceType.isInterface()) {
                this.resourceInterfaceType = null;
            }
        } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Class<?> resolveInterfaceType(String interfaceName) {
		switch(interfaceName) {
    		case "org.eclipse.sensinact.gateway.core.StateVariableResource" :
    			return XmlResourceConfigHandler.DEFAULT_VARIABLE_INTERFACE;
    		case "org.eclipse.sensinact.gateway.core.SensorDataResource" :
    			return XmlResourceConfigHandler.DEFAULT_SENSOR_INTERFACE;
    		case "org.eclipse.sensinact.gateway.core.PropertyResource":
    			return XmlResourceConfigHandler.DEFAULT_PROPERTY_INTERFACE;
    		case "org.eclipse.sensinact.gateway.core.ActionResource":
    			return XmlResourceConfigHandler.DEFAULT_ACTION_INTERFACE;    		
	    }
        return null;
    }

    /**
     * Returns the extended {@link Resource} interface type
     * wrapped by this ResourceInterfaceDefinition
     */
    public Class<? extends Resource> getResourceInterfaceType() {
        return this.resourceInterfaceType;
    }
}
