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
import org.eclipse.sensinact.gateway.core.Resource;
import org.xml.sax.Attributes;

/**
 * The extended {@link Resource} interface type of a {@link PolicyDefinition}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlElement(tag = "interfaceName", field = "resourceInterfaceType")
final class ResourceInterfaceDefinition extends XmlDefinition {
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
            if (super.mediator.isErrorLoggable()) {
                super.mediator.error(e, e.getMessage());
            }
        }
    }

    private Class resolveInterfaceType(String interfaceName) {
        if (XmlResourceConfigHandler.DEFAULT_VARIABLE_POLICY_INTERFACE == interfaceName.intern()) {
            return XmlResourceConfigHandler.DEFAULT_VARIABLE_INTERFACE;
        }
        if (XmlResourceConfigHandler.DEFAULT_SENSOR_POLICY_INTERFACE == interfaceName.intern()) {
            return XmlResourceConfigHandler.DEFAULT_SENSOR_INTERFACE;
        }
        if (XmlResourceConfigHandler.DEFAULT_PROPERTY_POLICY_INTERFACE == interfaceName.intern()) {
            return XmlResourceConfigHandler.DEFAULT_PROPERTY_INTERFACE;
        }
        if (XmlResourceConfigHandler.DEFAULT_ACTION_POLICY_INTERFACE == interfaceName.intern()) {
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
