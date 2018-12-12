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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.SensorDataResource;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.TypeConfig;
import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.eclipse.sensinact.gateway.util.xml.AbstractContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Extended {@link AbstractContentHandler} implementation for xml file
 * describing a collection of {@link ResourceInfoDefinition}s
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class XmlResourceConfigHandler extends AbstractContentHandler<ResourceInfoDefinition> implements 
RootXmlParsingContext{
    static final String LOCATION_KEY_WORD = "LOCATION";

    static final String BINARY_COMMAND_TYPE = "binaryHexContent";
    static final String STRING_COMMAND_TYPE = "stringContent";
    //private static final String UPDATE_ATTRIBUTE = "update";

    private static final String DEFAULT_POLICY_IMPLEMENTATION = ExtResourceImpl.class.getCanonicalName();
    public static final Class<StateVariableResource> DEFAULT_VARIABLE_INTERFACE = StateVariableResource.class;
    public static final Class<PropertyResource> DEFAULT_PROPERTY_INTERFACE = PropertyResource.class;
    public static final Class<SensorDataResource> DEFAULT_SENSOR_INTERFACE = SensorDataResource.class;
    public static final Class<ActionResource> DEFAULT_ACTION_INTERFACE = ActionResource.class;
    public static final String DEFAULT_VARIABLE_POLICY_INTERFACE = DEFAULT_VARIABLE_INTERFACE.getCanonicalName().intern();
    public static final String DEFAULT_PROPERTY_POLICY_INTERFACE = DEFAULT_PROPERTY_INTERFACE.getCanonicalName().intern();
    public static final String DEFAULT_SENSOR_POLICY_INTERFACE = DEFAULT_SENSOR_INTERFACE.getCanonicalName().intern();
    public static final String DEFAULT_ACTION_POLICY_INTERFACE = DEFAULT_ACTION_INTERFACE.getCanonicalName().intern();
    private Map<String, List<String>> profiles;
    private LinkedList<PolicyDefinition> policies;
    private Stack<CommandDefinition> commands;
    private Stack<DeviceDefinition> devices;
    private List<ExtResourceConfig> configs;
    private Mediator mediator;
    private boolean handleNoProfile;

    XmlModelParsingContext parsingContext;
    
    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the XmlResourceConfigHandler
     * to be instantiated to interact with the OSGi host environment
     */
    public XmlResourceConfigHandler(Mediator mediator) {
        super();
        this.mediator = mediator;
        //this.mediator.setProperty("offset",new Integer(0));
        this.commands = new Stack<CommandDefinition>();
        this.devices = new Stack<DeviceDefinition>();
        this.policies = new LinkedList<PolicyDefinition>();
        this.profiles = new HashMap<String, List<String>>();
        
        ResourceClassDefinition resourceClassDefinition = new ResourceClassDefinition(mediator, null);
        resourceClassDefinition.end();
        resourceClassDefinition.setResourceClassType(DEFAULT_POLICY_IMPLEMENTATION);
        
        ResourceInterfaceDefinition resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, null);
        resourceInterfaceDefinition.setResourceInterfaceType(DEFAULT_VARIABLE_POLICY_INTERFACE);
        PolicyDefinition policyDefinition = new PolicyDefinition(this.mediator, null);
        policyDefinition.setPolicy(TypeConfig.Type.STATE_VARIABLE.name());
        policyDefinition.setResourceClassDefinition(resourceClassDefinition);
        policyDefinition.setResourceInterfaceDefinition(resourceInterfaceDefinition);
        policyDefinition.setUpdatePolicy(Resource.UpdatePolicy.NONE.name());
        policyDefinition.end();
        this.policies.add(policyDefinition);

        resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, null);
        resourceInterfaceDefinition.setResourceInterfaceType(DEFAULT_PROPERTY_POLICY_INTERFACE);
        resourceInterfaceDefinition.end();
        policyDefinition = new PolicyDefinition(this.mediator, null);
        policyDefinition.setPolicy(TypeConfig.Type.PROPERTY.name());
        policyDefinition.setResourceClassDefinition(resourceClassDefinition);
        policyDefinition.setResourceInterfaceDefinition(resourceInterfaceDefinition);
        policyDefinition.setUpdatePolicy(Resource.UpdatePolicy.NONE.name());
        policyDefinition.end();
        this.policies.add(policyDefinition);
        
        resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, null);
        resourceInterfaceDefinition.setResourceInterfaceType(DEFAULT_SENSOR_POLICY_INTERFACE);
        policyDefinition = new PolicyDefinition(this.mediator, null);
        policyDefinition.setPolicy(TypeConfig.Type.SENSOR.name());
        policyDefinition.setResourceClassDefinition(resourceClassDefinition);
        policyDefinition.setResourceInterfaceDefinition(resourceInterfaceDefinition);
        policyDefinition.setUpdatePolicy(Resource.UpdatePolicy.NONE.name());
        policyDefinition.end();
        this.policies.add(policyDefinition);

        resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, null);
        resourceInterfaceDefinition.setResourceInterfaceType(DEFAULT_ACTION_POLICY_INTERFACE);
        policyDefinition = new PolicyDefinition(this.mediator, null);
        policyDefinition.setPolicy(TypeConfig.Type.ACTION.name());
        policyDefinition.setResourceClassDefinition(resourceClassDefinition);
        policyDefinition.setResourceInterfaceDefinition(resourceInterfaceDefinition);
        policyDefinition.setUpdatePolicy(Resource.UpdatePolicy.NONE.name());
        policyDefinition.end();
        this.policies.add(policyDefinition);
    }

    /**
     * Start of a "policy" XML node parsing
     * 
     * @param atts the {@link Attributes} of the parsed XML node */
    public void policyStart(Attributes atts) {
        this.parsingContext = new PolicyDefinition(mediator,atts);
    }

    /**
     * End of a "policy" XML node parsing
     */
    public void policyEnd() {
    	PolicyDefinition policyDefinition = (PolicyDefinition)this.parsingContext; 
        this.parsingContext = null;
        policyDefinition.mapTag(super.textContent.toString());
        PolicyDefinition registered = this.removePolicy(policyDefinition.getPolicy().name());

        Class<? extends ExtResourceImpl> implementationClass = policyDefinition.getPolicyImplementationClass();
        if (implementationClass == null) {
        	policyDefinition.setPolicyImplementationClass(registered.getPolicyImplementationClass().getCanonicalName());
        }
        Class<? extends Resource> implementationInterface = policyDefinition.getPolicyImplementationInterface();
        if (implementationInterface == null) {
        	policyDefinition.setPolicyImplementationInterface(registered.getPolicyImplementationInterface().getCanonicalName());
        }
        this.policies.add(policyDefinition);
    }
    
    /**
     * Start of a "command" XML node parsing
     * 
     * @param atts the {@link Attributes} of the parsed XML node */
    public void commandStart(Attributes atts) {
    	CommandDefinition commandDefinition = new CommandDefinition(this.mediator, atts);
        this.commands.push(commandDefinition);
        this.parsingContext = commandDefinition;
    }

    /**
     * Start of a "resourceInfos" XML node parsing
     * 
     * @param atts the {@link Attributes} of the parsed XML node */
    public void resourceInfosStart(Attributes atts) throws SAXException {
        this.handleNoProfile = Boolean.parseBoolean(atts.getValue("empty_profile"));
    }


    /**
     * End of a "resourceInfos" XML node parsing
     */
    public void resourceInfosEnd() {
    	this.configs = new ArrayList<ExtResourceConfig>();
    	Iterator<ResourceInfoDefinition> iterator = super.stack.iterator();
    	while(iterator.hasNext()) {
    		ResourceInfoDefinition definition = iterator.next();
    		configs.add((ExtResourceConfig) definition.asResourceConfig());
    	}
    }

    /**
     * Start of a "resourceInfo" XML node parsing
     * 
     * @param atts the {@link Attributes} of the parsed XML node */
    public ResourceInfoDefinition resourceInfoStart(Attributes atts) throws SAXException {
       ResourceInfoDefinition definition = new ResourceInfoDefinition(mediator,this,atts); 
       this.parsingContext = definition;       
       return definition;
    }

    /**
     * Start of a "service" XML node parsing
     * 
     * @param atts the {@link Attributes} of the parsed XML node */
    public void serviceStart(Attributes atts) {
        String serviceName = atts.getValue("name");
        if (serviceName != null) {
            devices.peek().addService(serviceName);
        }
    }

    /**
     * Start of a "device" XML node parsing
     * 
     * @param atts the {@link Attributes} of the parsed XML node */
    public void deviceStart(Attributes atts) {
        DeviceDefinition deviceDefinition = new DeviceDefinition(this.mediator, atts);
        devices.push(deviceDefinition);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.frame.AbstractContentHandler#
     * end(java.lang.String)
     */
    @Override
    public ResourceInfoDefinition end(String tag, String qname) {
    	ResourceInfoDefinition def = null;
        Method endMethod = null;
        Class<?> clazz = null;
        Object subject = null;
        XmlModelParsingContext parent = null;
        XmlModelParsingContext context = getContext();
    	if(context != null) {
    		for(String escape :context.escaped()) {
    			if(tag.equals(escape)) {
    				return def;
    			}
    		}
    		context.end();
	    	parent = getParentContext();
	        if(parent!= null) {  
	    		clazz = parent.getClass();
	    		subject = parent;
	        }	    	
    	}
    	if(subject==null) {
    		clazz = getClass();
    		subject = this;
    	}
        try {
        	endMethod = clazz.getDeclaredMethod(qname.concat("End"));
        	def = (ResourceInfoDefinition) endMethod.invoke(subject);
        } catch(Exception e) {
        	this.mediator.debug(e.getMessage());
        }
        if(parent!= null) {  	
    		parent.setNext(null);  	
    	} else {
    		this.parsingContext = null;
    	}
        return def;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.util.frame.AbstractContentHandler#
     * start(java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public ResourceInfoDefinition start(String tag, String qname, Attributes atts) throws SAXException {
    	ResourceInfoDefinition def = null;
        Method startMethod = null;
        Class<?> clazz = null;
        Object subject = null;
        XmlModelParsingContext context = getContext();
    	if(context != null) {    
    		for(String escape :context.escaped()) {
    			if(tag.equals(escape)) {
    				return def;
    			}
    		}		
    		clazz = context.getClass();
    		subject = context;
    	} else {
    		clazz = getClass();
    		subject = this;
    	}
        try {
        	startMethod = clazz.getMethod(qname.concat("Start"), Attributes.class);
        	def = (ResourceInfoDefinition) startMethod.invoke(subject, atts);
        } catch(Exception e) {
        	this.mediator.debug(e.getMessage());
        }
        return def;
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] chrs, int start, int length) throws SAXException {
    	XmlModelParsingContext context = getContext();
    	if(context != null) {
    		context.characters(chrs, start, length);
    	}
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] chrs, int start, int length) throws SAXException {
    	XmlModelParsingContext context = getContext();
    	if(context != null) {
    		context.ignorableWhitespace(chrs, start, length);
    	}
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
    	XmlModelParsingContext context = getContext();
    	if(context != null) {
    		context.processingInstruction(target, data);
    	}
    }

    /**
     * @inheritDoc
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
    	XmlModelParsingContext context = getContext();
    	if(context != null) {
    		context.skippedEntity(name);
    	}
    }

    /**
     * Returns the {@link PolicyDefinition} instance wrapping the
     * {@link ResourceTypeConf} whose name is passed as parameter
     *
     * @param policyName the name of the searched {@link ResourceTypeConf}
     * @return the {@link PolicyDefinition} instance wrapping the
     * {@link ResourceTypeConf} whose name is passed as
     * parameter
     */
    public PolicyDefinition getPolicy(String policyName) {
        if (policyName != null) {
            for (PolicyDefinition policyDefinition : this.policies) {
                if (policyName.equals(policyDefinition.getPolicy().name())) {
                    return policyDefinition;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the {@link PolicyDefinition} instance wrapping the
     * {@link ResourceTypeConf} whose name is passed as parameter
     *
     * @param policyName the name of the searched {@link ResourceTypeConf}
     * @return the {@link PolicyDefinition} instance wrapping the
     * {@link ResourceTypeConf} whose name is passed as
     * parameter
     */
    private PolicyDefinition removePolicy(String policyName) {
        if (policyName != null) {
        	int index = 0;
        	for(;index < policies.size();index++){
                PolicyDefinition policyDefinition = this.policies.get(index);                
                if (policyName.equals(policyDefinition.getPolicy().name())) {
                	return this.policies.remove(index);
                }
            }
        }
        return null;
    }

    XmlModelParsingContext getContext() {
    	if(this.parsingContext == null) {
    		return null;
    	} 
        XmlModelParsingContext context = this.parsingContext;
        while(context.next()!=null){
            context = context.next();
        }
    	return context;
    }

    XmlModelParsingContext getParentContext() {
    	if(this.parsingContext == null || this.parsingContext.next()== null) {
    		return null;
    	}
    	XmlModelParsingContext parent = null;
        XmlModelParsingContext context = this.parsingContext;
        while(context.next()!=null){
        	parent = context;
        	context = context.next();
        }
        return parent;
    }    

    public void registerProfile(String profile, String target) {
        String prfl = profile;
        if (prfl == null || "ANY_PROFILE".equals(prfl) 
        	|| (ResourceConfig.ALL_PROFILES.equals(prfl) && !this.handleNoProfile)) {
            return;
        }
        String[] targetArray = null;
        if (target != null) {
            targetArray = target.split(",");
        }
        List<String> targets = this.profiles.get(prfl);
        if (targets == null) {
            targets = new ArrayList<String>();
            this.profiles.put(prfl, targets);
        }
        int targetIndex = 0;
        int targetLength = targetArray == null ? 0 : targetArray.length;

        for (; targetIndex < targetLength; targetIndex++) {
            if (ServiceProvider.ADMINISTRATION_SERVICE_NAME.equals(targetArray[targetIndex]) 
            		|| targets.contains(targetArray[targetIndex])) {
                continue;
            }
            targets.add(targetArray[targetIndex]);
        }
    }
    
    /**
     * Returns the Map where the key is a profile string name
     * and the value is the list of service names associated
     * to the specified profile
     *
     * @return the Map of profiles and their associated
     * service names
     */
    public Map<String, List<String>> getProfiles() {
        return Collections.unmodifiableMap(this.profiles);
    }

    /**
     * Returns an {@link Iterator} over {@link CommandDefinition}s
     * referenced in the parsed xml file
     *
     * @return an {@link Iterator} over {@link CommandDefinition}s
     * referenced in the parsed xml file
     */
    public Commands getCommandDefinitions() {
        return new Commands(this.commands);
    }

    /**
     * Returns an {@link Iterator} over {@link DeviceDefinition}s
     * referenced in the parsed xml file
     *
     * @return an {@link Iterator} over {@link DeviceDefinition}s
     * referenced in the parsed xml file
     */
    public FixedProviders getDeviceDefinitions() {
        return new FixedProviders(this.devices);
    }

    /**
     * Returns an {@link Iterator} over {@link ExtResourceConfig}s
     * described in the parsed xml file
     *
     * @return an {@link Iterator} over {@link ExtResourceConfig}s
     * described in the parsed xml file
     */
    public Iterator<ExtResourceConfig> getXmlResourceConfigs() {    	
        return this.configs.iterator();
    }
}
