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

import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.ExtResourceImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.RequirementBuilder;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.SensorDataResource;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.StateVariableResource;
import org.eclipse.sensinact.gateway.core.TypeConfig;
import org.eclipse.sensinact.gateway.core.AttributeBuilder.Requirement;
import org.eclipse.sensinact.gateway.util.xml.AbstractContentHandler;

/**
 * Extended {@link AbstractContentHandler} implementation for xml file
 * describing {@link ExtResourceConfig} objects
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class XmlResourceConfigHandler extends
        AbstractContentHandler<ExtResourceConfig>
{
    static final String LOCATION_KEY_WORD = "LOCATION";
    
    static final String BINARY_COMMAND_TYPE = "binaryHexContent";
    static final String STRING_COMMAND_TYPE = "stringContent";

    private static final String UPDATE_ATTRIBUTE = "update";
    
    private static final String RESOURCE_INFO_TYPE_PROPERTY = 
    		"resourceInfoProperty".intern();
    private static final String RESOURCE_INFO_TYPE_SENSOR = 
    		"resourceInfoSensor".intern();
    private static final String RESOURCE_INFO_TYPE_VARIABLE = 
    		"resourceInfoVariable".intern();
    private static final String RESOURCE_INFO_TYPE_ACTION = 
    		"resourceInfoAction".intern();

    private static final String DEFAULT_POLICY_IMPLEMENTATION = 
    		ExtResourceImpl.class.getCanonicalName();

    public static final Class<StateVariableResource> DEFAULT_VARIABLE_INTERFACE = StateVariableResource.class;
    public static final Class<PropertyResource> DEFAULT_PROPERTY_INTERFACE = PropertyResource.class;
    public static final Class<SensorDataResource> DEFAULT_SENSOR_INTERFACE = SensorDataResource.class;
    public static final Class<ActionResource> DEFAULT_ACTION_INTERFACE = ActionResource.class;

    public static final String DEFAULT_VARIABLE_POLICY_INTERFACE = 
    		DEFAULT_VARIABLE_INTERFACE.getCanonicalName().intern();
    public static final String DEFAULT_PROPERTY_POLICY_INTERFACE = 
    		DEFAULT_PROPERTY_INTERFACE.getCanonicalName().intern();
    public static final String DEFAULT_SENSOR_POLICY_INTERFACE = 
    		DEFAULT_SENSOR_INTERFACE.getCanonicalName().intern();
    public static final String DEFAULT_ACTION_POLICY_INTERFACE = 
    		DEFAULT_ACTION_INTERFACE.getCanonicalName().intern();

    private Map<String,List<String>> profiles;
    private LinkedList<PolicyDefinition> policies;
    private Stack<CommandDefinition> commands;
    private Stack<DeviceDefinition> devices;

    private AttributeDefinition attributeDefinition = null;
    private IdentifierDefinition identifierDefinition = null;
    private PolicyDefinition policyDefinition = null;
    private ResourceClassDefinition resourceClassDefinition = null;
    private ResourceInterfaceDefinition resourceInterfaceDefinition = null;
    private ValueDefinition valueDefinition = null;
    private NameTypeValueDefinition nameTypeValue = null;
    private MethodDefinition methodDefinition = null;
    private BuilderDefinition builder = null;

	private Mediator mediator;

	private boolean handleNoProfile;

    /**
     * Constructor
     * 
     * @param mediator
     */
    public XmlResourceConfigHandler(Mediator mediator)
    {
        super();
        this.mediator = mediator;
        this.commands = new Stack<CommandDefinition>();
        this.devices = new Stack<DeviceDefinition>();        
        this.policies = new LinkedList<PolicyDefinition>();
        this.profiles = new HashMap<String,List<String>>();

        ResourceClassDefinition resourceClassDefinition = new ResourceClassDefinition(mediator, null);
        resourceClassDefinition.setResourceClassType(DEFAULT_POLICY_IMPLEMENTATION);

        ResourceInterfaceDefinition resourceInterfaceDefinition = new ResourceInterfaceDefinition(
        		mediator, null);
        resourceInterfaceDefinition.setResourceInterfaceType(DEFAULT_VARIABLE_POLICY_INTERFACE);
        
        PolicyDefinition policyDefinition = new PolicyDefinition(this.mediator,null);
        policyDefinition.setPolicy(TypeConfig.Type.STATE_VARIABLE.name());
        policyDefinition.setResourceClassDefinition(resourceClassDefinition);
        policyDefinition.setResourceInterfaceDefinition(resourceInterfaceDefinition);
        policyDefinition.setUpdatePolicy(Resource.UpdatePolicy.NONE.name());           
        this.policies.add(policyDefinition);
        
        resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, null);
        resourceInterfaceDefinition.setResourceInterfaceType(DEFAULT_PROPERTY_POLICY_INTERFACE);
        
        policyDefinition = new PolicyDefinition(this.mediator,null);
        policyDefinition.setPolicy(TypeConfig.Type.PROPERTY.name());
        policyDefinition.setResourceClassDefinition(resourceClassDefinition);
        policyDefinition.setResourceInterfaceDefinition(resourceInterfaceDefinition);
        policyDefinition.setUpdatePolicy(Resource.UpdatePolicy.NONE.name());           
        this.policies.add(policyDefinition);

        resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, null);
        resourceInterfaceDefinition.setResourceInterfaceType(DEFAULT_SENSOR_POLICY_INTERFACE);
        
        policyDefinition = new PolicyDefinition(this.mediator,null);
        policyDefinition.setPolicy(TypeConfig.Type.SENSOR.name());
        policyDefinition.setResourceClassDefinition(resourceClassDefinition);
        policyDefinition.setResourceInterfaceDefinition(resourceInterfaceDefinition);
        policyDefinition.setUpdatePolicy(Resource.UpdatePolicy.NONE.name());           
        this.policies.add(policyDefinition);
        
        resourceInterfaceDefinition = new ResourceInterfaceDefinition(mediator, null);
        resourceInterfaceDefinition.setResourceInterfaceType(DEFAULT_ACTION_POLICY_INTERFACE);
        
        policyDefinition = new PolicyDefinition(this.mediator,null);
        policyDefinition.setPolicy(TypeConfig.Type.ACTION.name());
        policyDefinition.setResourceClassDefinition(resourceClassDefinition);
        policyDefinition.setResourceInterfaceDefinition(resourceInterfaceDefinition);
        policyDefinition.setUpdatePolicy(Resource.UpdatePolicy.NONE.name());           
        this.policies.add(policyDefinition);
    }
    
    /**
     * Returns the {@link PolicyDefinition} instance wrapping the
     * {@link ResourceTypeConf} whose name is passed as parameter
     * 
     * @param policyName
     *      the name of the searched {@link ResourceTypeConf}
     * @return 
     *      the {@link PolicyDefinition} instance wrapping the
     *      {@link ResourceTypeConf} whose name is passed as 
     *      parameter
     */
    private PolicyDefinition getPolicy(String policyName)
    {
        if (policyName != null)
        {
            for (PolicyDefinition policyDefinition : this.policies)
            {
                if (policyName.equals(policyDefinition.getPolicy().name()))
                {
                    return policyDefinition;
                }
            }
        }
        return null;
    }

    /**
     * Start of a "policy" Element parsing
     */
    public void policyStart(Attributes atts)
    {
        if(super.stack.isEmpty())
        {
            String policyName = atts.getValue(Resource.NAME);
            this.policyDefinition = this.getPolicy(policyName);
            this.policyDefinition.setUpdatePolicy(atts.getValue(UPDATE_ATTRIBUTE));  
            
        } else
        {
            this.policyDefinition = new PolicyDefinition(
                   this.mediator,atts);
        }
    }

    /**
     * End of a "policy" Element parsing
     */
    public ExtResourceConfig policyEnd()
    {
        ExtResourceConfig resourceConfig = null;
        this.policyDefinition.mapTag(super.textContent.toString());  
    
        if(!super.stack.isEmpty())
        {
           resourceConfig = super.stack.pop();
           
           TypeConfig.Type policy = this.policyDefinition.getPolicy();           
           PolicyDefinition registered = this.getPolicy(policy.name());
                                 
           resourceConfig.setUpdatePolicy(this.policyDefinition.getUpdatePolicy());
                      
           TypeConfig  resourceTypeConfig = new TypeConfig(
        		   this.policyDefinition.getPolicy()); 
           
           Class<? extends ExtResourceImpl> implementationClass = 
        		   this.policyDefinition.getPolicyImplementationClass();
           if(implementationClass == null)
           {
        	   implementationClass = registered.getPolicyImplementationClass();
           }
           Class<? extends Resource> implementationInterface = 
        		   this.policyDefinition.getPolicyImplementationInterface();
           if(implementationInterface == null)
           {
        	   implementationInterface = registered.getPolicyImplementationInterface();
           }
           resourceTypeConfig.setImplementationClass(implementationClass);
           resourceTypeConfig.setResourceImplementedInterface(implementationInterface);
           resourceConfig.setTypeConfig(resourceTypeConfig);
        }
        this.policyDefinition = null;
        return resourceConfig;
    }

    /**
     * Start of "classname" Element parsing
     */
    public void classnameStart(Attributes atts)
    {
    	this.resourceClassDefinition = new ResourceClassDefinition(
    			mediator, atts);
    }
    
    /**
     * End of a "classname" Element parsing
     */
    public void classnameEnd()
    {
        this.resourceClassDefinition.mapTag(super.textContent.toString());
        this.policyDefinition.setResourceClassDefinition(this.resourceClassDefinition);        
        this.resourceClassDefinition = null;
    }

    /**
     * Start of "interfacename" Element parsing
     */
    public void interfacenameStart(Attributes atts)
    {
    	this.resourceInterfaceDefinition = new ResourceInterfaceDefinition(
    			mediator, atts);
    }
    
    /**
     * End of a "interfacename" Element parsing
     */
    public void interfacenameEnd()
    {
        this.resourceInterfaceDefinition.mapTag(super.textContent.toString());
        this.policyDefinition.setResourceInterfaceDefinition(this.resourceInterfaceDefinition);        
        this.resourceInterfaceDefinition = null;
    }
    
    /**
     * Start of an "identifierDefinition" Element parsing
     */
    public void identifierStart(Attributes atts)
    {
        this.identifierDefinition = new IdentifierDefinition(
                this.mediator, atts);
    }

    /**
     * End of an "identifierDefinition" Element parsing
     */
    public void identifierEnd()
    {
        this.identifierDefinition.mapTag(super.textContent.toString());
    }
    
    /**
     * Start of a "command" Element parsing
     */
    public void commandStart(Attributes atts)
    {
       this.commands.push(new CommandDefinition(
               this.mediator,atts));
    }

    /**
     * End of a "command" Element parsing
     */
    public void commandEnd()
    {
        CommandDefinition command = this.commands.peek();
        command.setIdentifier(this.identifierDefinition);
        this.identifierDefinition = null;
    }
    
    /**
     * Start of a "resourceConfig" Element parsing
     * @throws SAXException 
     */
    public void resourceInfosStart(Attributes atts) 
    		throws SAXException
    {
        this.handleNoProfile = Boolean.parseBoolean(
        		atts.getValue("empty_profile"));
    }
    
    /**
     * Start of a "resourceConfig" Element parsing
     * @throws SAXException 
     */
    public ExtResourceConfig resourceInfoStart(Attributes atts) 
    		throws SAXException
    {
        ExtResourceConfig resourceConfig = null;
        String type = atts.getValue("xsi:type");

        PolicyDefinition policyDefinition = null;

        if (type.intern() == RESOURCE_INFO_TYPE_ACTION.intern())
        {
            policyDefinition = this.getPolicy(TypeConfig.Type.ACTION.name());
        
        } else if (type.intern() == RESOURCE_INFO_TYPE_PROPERTY.intern())
        {
            policyDefinition = this.getPolicy(TypeConfig.Type.PROPERTY.name());

        } else if (type.intern() == RESOURCE_INFO_TYPE_SENSOR.intern())
        {
            policyDefinition = this.getPolicy(TypeConfig.Type.SENSOR.name());

        } else if (type.intern() == RESOURCE_INFO_TYPE_VARIABLE.intern())
        {
            policyDefinition = this.getPolicy(TypeConfig.Type.STATE_VARIABLE.name());
        }
        String name = atts.getValue(Resource.NAME);
        resourceConfig = new ExtResourceConfig();

        String target = atts.getValue("target");
        target = (target==null||"ANY_TARGET".equals(target))?
        		ResourceConfig.ALL_TARGETS:target;
        
        String profile = atts.getValue("profile");
        profile = (profile==null||"ANY_PROFILE".equals(profile))?
        		ResourceConfig.ALL_PROFILES:profile;
        
        RequirementBuilder requirementBuilder = new RequirementBuilder(
        		Requirement.VALUE, Resource.NAME);        
        requirementBuilder.put(ResourceConfig.ALL_TARGETS,name);
        
        resourceConfig.addRequirementBuilder(requirementBuilder);

        resourceConfig.setTarget(target);
        resourceConfig.setProfile(profile);
        
        this.registerProfile(profile, 
        		ResourceConfig.ALL_TARGETS.equals(target)
        		?null:target);

        resourceConfig.setUpdatePolicy(policyDefinition.getUpdatePolicy());
        
        TypeConfig resourceTypeConfig = new TypeConfig(
        		policyDefinition.getPolicy());
        resourceTypeConfig.setImplementationClass((Class<? extends ExtResourceImpl>)
                policyDefinition.getPolicyImplementationClass());
        
        resourceConfig.setTypeConfig(resourceTypeConfig);

        requirementBuilder = new RequirementBuilder(
        		Requirement.VALUE, Resource.TYPE);        
        requirementBuilder.put(ResourceConfig.ALL_TARGETS, 
        		resourceConfig.getTypeConfig().getResourceType());        
	    resourceConfig.addRequirementBuilder(requirementBuilder);
        return resourceConfig;
    }
    
    private void registerProfile(String profile, String target)
    {
    	String prfl = profile;
    	if(prfl == null || "ANY_PROFILE".equals(prfl)
    		|| (ResourceConfig.ALL_PROFILES.equals(prfl)
    				&& !this.handleNoProfile))
    	{
    		return;
    	}
    	String[] targetArray = null;
    	if(target!=null)
    	{
    		targetArray = target.split(",");    		
    	}
    	List<String> targets = this.profiles.get(prfl);
		if(targets == null)
		{
			targets = new ArrayList<String>();
			this.profiles.put(prfl, targets);
		}
		int targetIndex = 0;
		int targetLength = targetArray==null?0:targetArray.length;
		
		for(;targetIndex < targetLength;targetIndex++)
		{
			if(ServiceProvider.ADMINISTRATION_SERVICE_NAME.equals(
				targetArray[targetIndex]) || targets.contains(
						targetArray[targetIndex]))
			{
				continue;
			}
			targets.add(targetArray[targetIndex]);
		}
    }
    
    /**
     * Start of a "resourceConfig" Element parsing
     */
    public ExtResourceConfig resourceInfoEnd()
    {
        ExtResourceConfig resourceConfig = super.stack.pop();
        resourceConfig.setIdentifier(this.identifierDefinition.getIdentifier());  
        if(this.methodDefinition != null)
        {
        	resourceConfig.addMethodDefinition(methodDefinition);
        	this.methodDefinition = null;
        }
        this.identifierDefinition = null;
        this.attributeDefinition = null;
        this.nameTypeValue = null;
        this.valueDefinition = null;
        return resourceConfig;
    }
    
    /**
     * Start of an "value" Element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void valueStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
        TypeDefinition typeDefinition = null;
        
        if(this.nameTypeValue != null)
        {
            typeDefinition = this.nameTypeValue.getTypeDefinition();
            
        } else if(this.attributeDefinition != null)
        {
            typeDefinition = this.attributeDefinition.getTypeDefinition();
        }
        if(typeDefinition == null)
        {
            throw new InvalidXmlDefinitionException("No type defined");
        }
        this.valueDefinition = new ValueDefinition(this.mediator,
                atts,typeDefinition);
    }

    /**
     * End of an "type" Element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void valueEnd() throws InvalidXmlDefinitionException
    {
       this.valueDefinition.mapTag(super.textContent.toString());
       if(this.nameTypeValue != null)
       { 
            this.nameTypeValue.setValueDefinition(this.valueDefinition);
           
       } else if(this.attributeDefinition != null)
       {           
            this.attributeDefinition.setValueDefinition(
            		this.valueDefinition);
       }   
       this.valueDefinition = null;
    }
    
    /**
     * Start of a "parameters" Element parsing
     */
    public void methodStart(Attributes atts)
    {
    	this.methodDefinition = new MethodDefinition(mediator, atts);
    }
    
    /**
     * End of a "parameters" Element parsing
     */
    public ExtResourceConfig methodEnd()
    {
        this.methodDefinition.closeParameters();
        ExtResourceConfig resourceConfig = super.stack.pop();        
        resourceConfig.addMethodDefinition(this.methodDefinition);
        this.methodDefinition = null;
        return resourceConfig;
    }
    
    /**
     * Start of a "parameter" Element parsing
     */
    public void parameterStart(Attributes atts)
    {
        ParameterDefinition parameterDefinition = new ParameterDefinition(
                this.mediator, atts);        
        this.nameTypeValue = parameterDefinition;
    }

    /**
     * End of a "parameter" Element parsing
     * 
     * @throws InvalidValueException 
     */
    public void parameterEnd() throws InvalidValueException
    {
        this.methodDefinition.addParameter((
                (ParameterDefinition)this.nameTypeValue));        
        this.nameTypeValue = null;
    }

    /**
     * Start of a "reference" Element parsing
     */
    public void referenceStart(Attributes atts)
    {
        ReferenceDefinition referenceDefinition = 
        	new ReferenceDefinition(this.mediator, atts); 
        this.nameTypeValue = referenceDefinition;
    }

    /**
     * End of a "reference" Element parsing
     */
    public void referenceEnd()
    {
        this.methodDefinition.addReferenceDefinition(
        		(ReferenceDefinition) this.nameTypeValue);
        this.nameTypeValue = null;
    }

    /**
     * Start of a "constant" Element parsing
     */
    public void constantStart(Attributes atts)
    {
        this.nameTypeValue.setType(atts.getValue("type"));
    }
    
    /**
     * @param atts
     */
    public void builderStart(Attributes atts)
    {
    	 ParameterDefinition parameterDefinition = 
    			 (ParameterDefinition) this.nameTypeValue;
    	 this.builder = new BuilderDefinition(
    		mediator, parameterDefinition.getName() , atts);
    }
    
    /**
     * 
     */
    public void builderEnd()
    {
    	 ParameterDefinition parameterDefinition = 
    			 (ParameterDefinition) this.nameTypeValue;
    	 parameterDefinition.setBuilder(this.builder); 
    	 this.builder = null;
    }
    
    /**
     * Start of a "minExclusive" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void minExclusiveStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "minExclusive");
    }
    
    /**
     * Start of a "minInclusive" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void minInclusiveStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "minInclusive");
    }
    
    /**
     * Start of a "maxExclusive" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void maxExclusiveStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "maxExclusive");
    }
    
    /**
     * Start of a "maxInclusive" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void maxInclusiveStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "maxInclusive");
    }

    /**
     * Start of a "length" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void lengthStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "length");
    }

    /**
     * Start of a "minLength" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void minLengthStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "minLength");
    }

    /**
     * Start of a "maxLength" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void maxLengthStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "maxLength");
    }    

    /**
     * Start of a "enumeration" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void enumerationStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {       
    	this.startConstraint(atts, "enumeration");
    }
    
    /**
     * Start of a "fixed" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void fixedStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {       
    	this.startConstraint(atts, "fixed");
    }    

    /**
     * Start of a "pattern" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void patternStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "pattern");
    }

    /**
     * Start of a "delta" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void deltaStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "delta");
    }

    /**
     * Start of a "absolute" restriction element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void absoluteStart(Attributes atts) 
    		throws InvalidXmlDefinitionException
    {
    	this.startConstraint(atts, "absolute");
    }

    private void startConstraint(Attributes atts, String name) 
    		throws InvalidXmlDefinitionException
    {
    	ConstraintDefinition definition = new ConstraintDefinition(
    			mediator, name, atts);

    	if(this.builder != null)
    	{
    		this.builder.addConstraint(definition);		
    	} 
    	if(this.nameTypeValue != null)
    	{
    		((ConstrainableDefinition)this.nameTypeValue
    				).addConstraint(definition);		
    	} 
    	else if(this.attributeDefinition != null)
    	{
    		this.attributeDefinition.addConstraint(definition);
    	}
    	else
    	{
    	    throw new InvalidXmlDefinitionException(
    	        "No Attribute data structure found");
    	} 
    }    
    
    /**
     * Start of a "metadata" Element parsing
     */
    public void metaStart(Attributes atts)
    {
        MetadataDefinition metadataDefinition = new MetadataDefinition(
                this.mediator, atts);        
        this.nameTypeValue = metadataDefinition;
    }

    /**
     * End of a "metadata" Element parsing
     * @throws InvalidXmlDefinitionException 
     */
    public void metaEnd() throws InvalidXmlDefinitionException
    {
    	this.attributeDefinition.addMetadataBuilder(
    				(MetadataDefinition)this.nameTypeValue); 
    	this.nameTypeValue = null;
    }
    
    /**
     * Start of an "attribute" Element parsing
     */
    public ResourceConfig attributeStart(Attributes atts)
    {
        ExtResourceConfig resourceConfig = super.stack.pop();    	
        this.attributeDefinition = new AttributeDefinition(this.mediator, atts);
        String target = atts.getValue("target");
        
        if(target==null || "ANY_TARGET".equals(target))
        {
        	StringBuilder builder = new StringBuilder();        	
        	String[] targets = resourceConfig.getTargets();
        	
        	int index = 0;
        	int length =  (targets==null?0:targets.length);
        	
        	for(; index < length; index++)
        	{
        		if(index>0)
        		{
        			builder.append(",");
        		}
        		builder.append(targets[index]);
        	}
        	this.attributeDefinition.setTarget(builder.toString());
        	
        } else
        {
        	this.attributeDefinition.setTarget(target);
        }
        return resourceConfig;
    }

    /**
     * End of an "attribute" Element parsing
     */
    public ExtResourceConfig attributeEnd()
    {
        ExtResourceConfig resourceConfig = super.stack.pop();
        resourceConfig.addAttributeDefinition(this.attributeDefinition);
        
        this.attributeDefinition = null;        
        return resourceConfig;
    }
    
    /**
     * Start of a "service" Element parsing
     */
    public void serviceStart(Attributes atts)
    {
        String serviceName = atts.getValue("name");
        if(serviceName != null)
        {
            devices.peek().addService(serviceName);
        }
    }
    
    /**
     * Start of a "device" Element parsing
     */
    public void deviceStart(Attributes atts)
    {
        DeviceDefinition deviceDefinition = new DeviceDefinition(
                this.mediator,atts);
        devices.push(deviceDefinition);
    }

    /**
     * @inheritDoc
     * 
     * @see org.eclipse.sensinact.gateway.util.frame.AbstractContentHandler#
     *      end(java.lang.String)
     */
    @Override
    public ExtResourceConfig end(String tag, String qname)
    {      
    	Method endMethod = null; 
    	try
    	{
    		endMethod = getClass().getDeclaredMethod(
    				qname.concat("End"));
    		
    	} catch (Exception e) 
    	{
			return null;
		} 
        try
        {
            return (ExtResourceConfig) endMethod.invoke(this);

        } catch (Exception e)
        {
            this.mediator.error("ERROR END :" +qname);
            this.mediator.error("=========================>");
            this.mediator.error(e.getClass().getSimpleName() + ": " + e.getMessage());
            this.mediator.error("=========================>");
        	if(!super.stack.isEmpty())
        	{
        		this.mediator.error("RESOURCE INFO : "+super.stack.pop().getName());
        	}
        	this.mediator.error("ATTRIBUTE DEFINITION : " +this.attributeDefinition);
        	this.mediator.error("NAME-TYPE-VALUE  DEFINITION :" +this.nameTypeValue);
        }
        return null;
    }

    /**
     * @inheritDoc
     * 
     * @see org.eclipse.sensinact.gateway.util.frame.AbstractContentHandler#
     *      start(java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public ExtResourceConfig start(String tag, String qname, Attributes atts)
            throws SAXException
    {
    	Method startMethod = null; 
    	try
    	{
    		startMethod = getClass().getDeclaredMethod(
    				qname.concat("Start"), Attributes.class);
    		
    	} catch (Exception e) 
    	{
			return null;
		} 
        try
        {
            return (ExtResourceConfig) startMethod.invoke(this,atts);

        } catch (Exception e)
        {
            this.mediator.error("ERROR START :" +qname);
            this.mediator.error("=========================>");
            this.mediator.error(e.getClass().getSimpleName() + ": " + e.getMessage());
            this.mediator.error("=========================>");
        	if(!super.stack.isEmpty())
        	{
        		this.mediator.error("RESOURCE INFO : "+super.stack.pop().getName());
        	}
        	this.mediator.error("ATTRIBUTE DEFINITION : " +this.attributeDefinition);
        	this.mediator.error("NAME-TYPE-VALUE  DEFINITION :" +this.nameTypeValue);
        }
        return null;
    }

	/**
	 * Returns the Map where the key is a profile string name
	 * and the value is the list of service names associated 
	 * to the specified profile
	 * 
	 * @return the Map of profiles and their associated 
	 * service names
	 */
	public Map<String,List<String>> getProfiles() 
	{
		return Collections.unmodifiableMap(this.profiles);
	}
	
    /**
     * Returns an {@link Iterator} over {@link CommandDefinition}s 
     * referenced in the parsed xml file
     * 
     * @return
     *       an {@link Iterator} over {@link CommandDefinition}s 
     *       referenced in the parsed xml file
     */
    public Commands getCommandDefinitions()
    {
       return new Commands(this.commands);
    }
    
    /**
     * Returns an {@link Iterator} over {@link DeviceDefinition}s 
     * referenced in the parsed xml file
     * 
     * @return
     *       an {@link Iterator} over {@link DeviceDefinition}s 
     *       referenced in the parsed xml file
     */
    public FixedProviders getDeviceDefinitions()
    {
       return new FixedProviders(this.devices);
    }

    /**
     * Returns an {@link Iterator} over {@link ExtResourceConfig}s 
     * described in the parsed xml file
     * 
     * @return
     *       an {@link Iterator} over {@link ExtResourceConfig}s  
     *       described in the parsed xml file
     */
    public Iterator<ExtResourceConfig> getXmlResourceConfigs()
    {
        return super.stack.iterator();
    }
}
