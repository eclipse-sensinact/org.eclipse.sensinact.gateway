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
import org.eclipse.sensinact.gateway.core.TypeConfig;
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.xml.sax.Attributes;

/**
 * Policy definition wrapping the policy's associated type and its automatic
 * update state
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "name", field = "policy"), @XmlAttribute(attribute = "update", field = "updatePolicy")})
public final class PolicyDefinition extends XmlDefinition {

    private TypeConfig.Type policy;
    private ResourceClassDefinition resourceClassDefinition;
    private ResourceInterfaceDefinition resourceInterfaceDefinition;

    private Resource.UpdatePolicy updatePolicy;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml policy element
     */
    public PolicyDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }

    /**
     * Sets the wrapped {@link ResourceTypeConf}
     *
     * @param policy the wrapped {@link ResourceTypeConf}
     */
    public void setPolicy(String policy) {
        this.policy = TypeConfig.Type.valueOf(policy);
    }

    /**
     * Returns the wrapped {@link ResourceTypeConf}
     *
     * @return the wrapped {@link ResourceTypeConf}
     */
    public TypeConfig.Type getPolicy() {
        return this.policy;
    }

    /**
     * Sets whether the {@link Resource} instance mapped to the wrapped
     * {@link ResourceTypeConf} will be automatically updated or not
     *
     * @param autoUpdatedStr string formated boolean value defining whether the
     *                       {@link Resource} instance mapped to the wrapped
     *                       {@link ResourceTypeConf} will be automatically updated
     *                       or not
     */
    void setUpdatePolicy(String updatePolicy) {
        if (updatePolicy == null) {
            this.updatePolicy = Resource.UpdatePolicy.NONE;
        } else {
            this.updatePolicy = Resource.UpdatePolicy.valueOf(updatePolicy);
        }
    }

    /**
     * Defines the {@link ResourceInterfaceDefinition} of this PolicyDefinition
     *
     * @param resourceInterfaceDefinition the {@link ResourceInterfaceDefinition} to set
     */
    void setResourceInterfaceDefinition(ResourceInterfaceDefinition resourceInterfaceDefinition) {
        this.resourceInterfaceDefinition = resourceInterfaceDefinition;
    }

    /**
     * Defines the {@link ResourceClassDefinition} of this PolicyDefinition
     *
     * @param resourceClassDefinition the {@link ResourceClassDefinition} to set
     */
    void setResourceClassDefinition(ResourceClassDefinition resourceClassDefinition) {
        this.resourceClassDefinition = resourceClassDefinition;
    }

    /**
     * @return
     */
    public Class<? extends ExtResourceImpl> getPolicyImplementationClass() {
        if (this.resourceClassDefinition != null) {
            return this.resourceClassDefinition.getResourceClassType();
        }
        return null;
    }

    /**
     * @return
     */
    public Class<? extends Resource> getPolicyImplementationInterface() {
        if (this.resourceInterfaceDefinition != null) {
            return this.resourceInterfaceDefinition.getResourceInterfaceType();
        }
        return null;
    }

    /**
     * @return
     */
    public Resource.UpdatePolicy getUpdatePolicy() {
        return this.updatePolicy;
    }
}
