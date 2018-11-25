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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.AttributeBuilder.Requirement;
import org.eclipse.sensinact.gateway.core.RequirementBuilder;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.TypeConfig;
import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.xml.sax.Attributes;

/**
 * Extended {@link ResolvedNameTypeValueDefinition} for attribute
 * XML node *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes(value = {
		@XmlAttribute(attribute = "name", field = "name"),
		@XmlAttribute(attribute = "target", field = "target"),
		@XmlAttribute(attribute = "profile", field = "profile")})
@XmlEscaped(value = {"attributes","methods"})
public class ResourceInfoDefinition extends XmlModelParsingContext  {

	protected RootXmlParsingContext root;
    protected List<AttributeDefinition> attributeDefinitions;
    protected List<MethodDefinition> methodDefinitions;
	protected IdentifierDefinition identifierDefinition;
    protected PolicyDefinition type;
    protected String name;
    protected String[] target;
    protected String[] profile;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml attribute element
     */
    ResourceInfoDefinition(Mediator mediator, RootXmlParsingContext root, Attributes atts) {
        super(mediator, atts);
        this.attributeDefinitions = new ArrayList<AttributeDefinition>();
        this.methodDefinitions = new ArrayList<MethodDefinition>();
        this.root = root;
        this.setType(atts.getValue("xsi:type"));
    }

    /**
     * Sets the name of the target
     *
     * @param target the target name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        if (type.intern() == RootXmlParsingContext.RESOURCE_INFO_TYPE_ACTION.intern()) {
            this.type = this.root.getPolicy(TypeConfig.Type.ACTION.name());
        } else if (type.intern() == RootXmlParsingContext.RESOURCE_INFO_TYPE_PROPERTY.intern()) {
            this.type = this.root.getPolicy(TypeConfig.Type.PROPERTY.name());
        } else if (type.intern() == RootXmlParsingContext.RESOURCE_INFO_TYPE_SENSOR.intern()) {
            this.type = this.root.getPolicy(TypeConfig.Type.SENSOR.name());
        } else if (type.intern() == RootXmlParsingContext.RESOURCE_INFO_TYPE_VARIABLE.intern()) {
            this.type = this.root.getPolicy(TypeConfig.Type.STATE_VARIABLE.name());
        }
    }
    
    /**
     * Sets the name of the target
     *
     * @param target the target name to set
     */
    public void setTarget(String target) {
        String[] targets = target == null ? new String[0] : target.split(",");
        this.target = new String[targets.length];
        if (this.target.length > 0) {
            for (int index = 0; index < this.target.length; index++) {
                if ("ANY_TARGET".equals(targets[index])) {
                    this.target[index] = ResourceConfig.ALL_TARGETS;
                } else {
                    this.target[index] = targets[index];
                }
            }
        }
    }

    /**
     * Returns the name of the target
     *
     * @return the target name
     */
    public String getTarget() {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        int length = this.target == null ? 0 : this.target.length;
        for (; index < length; index++) {
            if (index > 0) {
                builder.append(",");
            }
            builder.append(this.target[index].trim());
        }
        return builder.toString();
    }

    public String[] getTargets() {
        return this.target;
    }

    /**
     * Returns the name of the target
     *
     * @return the target name
     */
    public boolean isTargeted(String serviceId) {
        for (int index = 0; index < this.target.length; index++) {
            if (target[index].equals(ResourceConfig.ALL_TARGETS) || target[index].equals(serviceId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sets the name of the target
     *
     * @param target the target name to set
     */
    public void setProfile(String profile) {
        String[] profiles = profile == null ? new String[0] : profile.split(",");
        this.profile = new String[profiles.length];
        if (this.profile.length > 0) {
            for (int index = 0; index < this.profile.length; index++) {
                if ("ANY_PROFILE".equals(profiles[index])) {
                    this.profile[index] = ResourceConfig.ALL_PROFILES;
                } else {
                    this.profile[index] = profiles[index];
                }
            }
        }
    }

    /**
     * Returns the name of the target
     *
     * @return the target name
     */
    public String getProfile() {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        int length = this.profile == null ? 0 : this.profile.length;
        for (; index < length; index++) {
            if (index > 0) {
                builder.append(",");
            }
            builder.append(this.profile[index].trim());
        }
        return builder.toString();
    }

    /**
     * Returns the name of the target
     *
     * @return the target name
     */
    public boolean isProfiled(String profileName) {
        for (int index = 0; index < this.profile.length; index++) {
            if (profile[index].equals(ResourceConfig.ALL_PROFILES) || profile[index].equals(profileName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Start of an "attribute" Element parsing
     */
    public void attributeStart(Attributes atts) {
        AttributeDefinition attributeDefinition = new AttributeDefinition(this.mediator, atts);
        String target = attributeDefinition.getTarget();

        if (target == null || "ANY_TARGET".equals(target)) {
            attributeDefinition.setTarget(this.getTarget());
        }
        this.attributeDefinitions.add(attributeDefinition);
        super.setNext(attributeDefinition);
    }

    /**
     * Start of an "identifierDefinition" Element parsing
     */
    public void identifierStart(Attributes atts) {
    	IdentifierDefinition identifierDefinition = new IdentifierDefinition(this.mediator, atts);
    	this.identifierDefinition = identifierDefinition;
    	super.setNext(identifierDefinition);
    }
    
    /**
     * Start of a "parameters" Element parsing
     */
    public void methodStart(Attributes atts) {
    	MethodDefinition methodDefinition = null;    	
    	String name = atts.getValue("type");
    	for(MethodDefinition def : methodDefinitions) {
    		if(def.getType().name().equals(name)) {
    			methodDefinition = def;
    			break;
    		}
    	}
    	if(methodDefinition == null) {
    		methodDefinition = new MethodDefinition(mediator, atts);
    		this.methodDefinitions.add(methodDefinition);
    	}
        super.setNext(methodDefinition);
    }

    /**
     * Start of a "policy" Element parsing
     */
    public void policyStart(Attributes atts) {
        PolicyDefinition policyDefinition = new PolicyDefinition(this.mediator, atts);
        this.type = policyDefinition;
        super.setNext(policyDefinition);
    }    
    
    public ResourceConfig asResourceConfig() {
    	ExtResourceConfig resourceConfig = new ExtResourceConfig(attributeDefinitions,methodDefinitions);
    	String target = getTarget();
        target = (target == null || target.length() == 0 || "ANY_TARGET".equals(target)) ? ResourceConfig.ALL_TARGETS:target;

        String profile = getProfile();
        profile = (profile == null || profile.length() == 0 || "ANY_PROFILE".equals(profile)) ? ResourceConfig.ALL_PROFILES:profile;

        RequirementBuilder requirementBuilder = new RequirementBuilder(Requirement.VALUE, Resource.NAME);
        requirementBuilder.put(ResourceConfig.ALL_TARGETS, name);
        resourceConfig.addRequirementBuilder(requirementBuilder);
        
        resourceConfig.setTarget(target);
        resourceConfig.setProfile(profile); 
        resourceConfig.setIdentifier(this.identifierDefinition.getIdentifier());
        
    	PolicyDefinition registered = this.root.getPolicy(type.getPolicy().name());
    	
        this.root.registerProfile(profile, ResourceConfig.ALL_TARGETS.equals(target)?null:target);       
        resourceConfig.setUpdatePolicy(type.getUpdatePolicy());

        TypeConfig resourceTypeConfig = new TypeConfig(type.getPolicy());

        Class<? extends ExtResourceImpl> implementationClass = this.type.getPolicyImplementationClass();
        if (implementationClass == null) {
            implementationClass = registered.getPolicyImplementationClass();
        }
        Class<? extends Resource> implementationInterface = this.type.getPolicyImplementationInterface();
        if (implementationInterface == null) {
            implementationInterface = registered.getPolicyImplementationInterface();
        }
        resourceTypeConfig.setImplementationClass(implementationClass);
        resourceTypeConfig.setResourceImplementedInterface(implementationInterface);
        resourceConfig.setTypeConfig(resourceTypeConfig);
        
        requirementBuilder = new RequirementBuilder(Requirement.VALUE, Resource.TYPE);
        requirementBuilder.put(ResourceConfig.ALL_TARGETS, resourceConfig.getTypeConfig().getResourceType());
        resourceConfig.addRequirementBuilder(requirementBuilder);
        return resourceConfig;
    }
}
