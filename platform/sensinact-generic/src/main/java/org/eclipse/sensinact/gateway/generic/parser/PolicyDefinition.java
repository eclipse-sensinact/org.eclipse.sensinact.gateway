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
@XmlAttributes({
	@XmlAttribute(attribute = "name", field = "policy"), 
	@XmlAttribute(attribute = "update", field = "updatePolicy")})
public final class PolicyDefinition extends XmlModelParsingContext {

    private ResourceClassDefinition resourceClassDefinition;
    private ResourceInterfaceDefinition resourceInterfaceDefinition;

    private Resource.UpdatePolicy updatePolicy;
    private TypeConfig.Type policy;

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

	public void setResourceClassDefinition(ResourceClassDefinition resourceClassDefinition) {
		this.resourceClassDefinition=resourceClassDefinition;		
	}

	public void setResourceInterfaceDefinition(ResourceInterfaceDefinition resourceInterfaceDefinition) {
		this.resourceInterfaceDefinition=resourceInterfaceDefinition;
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

	protected void setPolicyImplementationClass(String policyImplementationClass) {
		this.resourceClassDefinition = new ResourceClassDefinition(mediator, null);
		this.resourceClassDefinition.setResourceClassType(policyImplementationClass);		
	}

	protected void setPolicyImplementationInterface(String policyImplementationInterface) {
		this.resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, null);
		this.resourceInterfaceDefinition.setResourceInterfaceType(policyImplementationInterface);
	}
	
    /**
     * @return
     */
    public Resource.UpdatePolicy getUpdatePolicy() {
        return this.updatePolicy;
    }
    
    /**
     * Start of "classname" Element parsing
     */
    public void classnameStart(Attributes atts) {
        this.resourceClassDefinition = new ResourceClassDefinition(mediator, atts);
        super.setNext(this.resourceClassDefinition);
    }

    /**
     * Start of "interfacename" Element parsing
     */
    public void interfacenameStart(Attributes atts) {
        this.resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, atts);
        super.setNext(this.resourceInterfaceDefinition);
    }
}
