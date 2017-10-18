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
package org.eclipse.sensinact.gateway.generic;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sensinact.gateway.core.*;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.parser.FixedProviders;
import org.xml.sax.SAXException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.DefaultResourceConfigBuilder;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.security.AccessNodeImpl;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.generic.parser.Commands;
import org.eclipse.sensinact.gateway.generic.parser.XmlResourceConfigHandler;

/**
 * Manages IO for a set of {@link ServiceProvider}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ExtModelConfiguration extends ModelConfiguration
{   
    /**
     * Returns true if the two bytes arrays passed as parameters are equals
     * (same content)
     * 
     * @param base
     *            the first of the two bytes arrays to compare
     * @param compare
     *            the second of the two bytes arrays to compare
     * @return true if the two bytes arrays passed as parameters are equals;
     *         false otherwise
     */
    public static boolean compareBytesArrays(byte[] base, byte[] compare)
    {
        if(base == null)
        {
            return compare==null;
        }
        if(compare == null)
        {
            return false;
        }
        if (base.length != compare.length)
        {
            return false;
        }
        for (int index = 0; index < base.length; index++)
        {
            if (base[index] != compare[index])
            {
                return false;
            }
        }
        return true;
    }
    
	/**
	 * Defines whether the {@link Task}s management has to be 
	 * unsynchronous
	 */
	protected boolean isDesynchronized;

	/**
	 * Defines whether at  initialization time the registered 
	 * {@link Task}s have to be stored waiting for their processing
	 */
	protected boolean lockedAtInitializationTime;

	/**
	 * the handled {@link Packet} type
	 */
	private Class<? extends Packet> packetType;
	
	/**
	 * The {@link ConnectorCustomizer} to attach to the 
	 * connector to build
	 */
	private ConnectorCustomizer<? extends Packet> customizer ;

    /**
     * the set of {@link Task.CommandType}
     */
    protected Commands commands;

	/**
	 * the initial set of service providers map to their profile
	 */
	private Map<String, String> fixedProviders;
    	
    /**
	 * Constructor
	 * 
     * @param mediator the {@link Mediator} allowing the ExtModelConfiguration
     * to be instantiated to interact with the OSGi host environment
     * @param accessTree the {@link AccessTree} defining the secured access 
     * rights applying on sensiNact resource model instances configured
     * by the ExtModelConfiguration to be instantiated
	 * @param packetType
	 * @param configurationFile
	 * @param defaults
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public ExtModelConfiguration(Mediator mediator, 
			AccessTreeImpl<? extends AccessNodeImpl<?>> accessTree, 
			Class<? extends Packet> packetType, String configurationFile,
			Map<String,String> defaults) 
			throws ParserConfigurationException, SAXException, IOException
	{
		this(mediator, accessTree, packetType, new DefaultResourceConfigBuilder(), 
		configurationFile, defaults);
	}


	/**
	 * Constructor
	 * 
     * @param mediator the {@link Mediator} allowing the ExtModelConfiguration
     * to be instantiated to interact with the OSGi host environment
     * @param accessTree the {@link AccessTree} defining the secured access 
     * rights applying on sensiNact resource model instances configured
     * by the ExtModelConfiguration to be instantiated
	 * @param packetType
	 * @param defaultResourceConfigBuilder
	 * @param configurationFile
	 * @param defaults
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public ExtModelConfiguration(Mediator mediator,  
			AccessTreeImpl<? extends AccessNodeImpl<?>> accessTree, 
			Class<? extends Packet> packetType, 
			ResourceConfigBuilder defaultResourceConfigBuilder, 
			String configurationFile, Map<String,String> defaults) 
			throws ParserConfigurationException, SAXException, IOException
	{
		this(mediator, accessTree, packetType, defaultResourceConfigBuilder, 
			configurationFile, defaults, ExtServiceProviderImpl.class,  
			ExtServiceImpl.class, ExtResourceImpl.class);
	}
	
	/**
	 * Constructor
     * 
     * @param mediator the {@link Mediator} allowing the ExtModelConfiguration
     * to be instantiated to interact with the OSGi host environment
     * @param accessTree the {@link AccessTree} defining the secured access 
     * rights applying on sensiNact resource model instances configured
     * by the ExtModelConfiguration to be instantiated
	 * @param packetType
	 * @param defaultResourceConfigBuilder
	 * @param configurationFile
	 * @param defaults
	 * @param defaultServiceProviderType
	 * @param defaultServiceType
	 * @param defaultResourceType
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected ExtModelConfiguration(Mediator mediator,  
			AccessTreeImpl<?> accessTree, 
			Class<? extends Packet> packetType, 
			ResourceConfigBuilder defaultResourceConfigBuilder,
			String configurationFile,
			Map<String,String> defaults,
			Class<? extends ServiceProviderImpl> defaultServiceProviderType,
			Class<? extends ServiceImpl> defaultServiceType, 
			Class<? extends ResourceImpl> defaultResourceType) 
			throws ParserConfigurationException, SAXException, IOException
	{
		this(mediator,accessTree, packetType, defaultResourceConfigBuilder, 
		configurationFile!=null?mediator.getContext().getBundle().getResource(
			configurationFile):null,defaults, defaultServiceProviderType, 
				defaultServiceType, defaultResourceType);
	}
    
	/**
     * Constructor
     * 
     * @param mediator the {@link Mediator} allowing the ExtModelConfiguration
     * to be instantiated to interact with the OSGi host environment
     * @param accessTree the {@link AccessTree} defining the secured access 
     * rights applying on sensiNact resource model instances configured
     * by the ExtModelConfiguration to be instantiated
     * @param packetType
     * @param defaultResourceConfigBuilder
     * @param configurationFile
     * @param defaults
     * @param defaultServiceProviderType
     * @param defaultServiceType
     * @param defaultResourceType
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public ExtModelConfiguration(Mediator mediator,  
			AccessTreeImpl<?> accessTree, 
			Class<? extends Packet> packetType, 
			ResourceConfigBuilder defaultResourceConfigBuilder, 
			URL configurationFile,
			Map<String,String> defaults,
			Class<? extends ServiceProviderImpl> defaultServiceProviderType,
			Class<? extends ServiceImpl> defaultServiceType, 
			Class<? extends ResourceImpl> defaultResourceType) 
    		throws ParserConfigurationException, SAXException, 
    		IOException
    {         
    	super(mediator, accessTree, defaultResourceConfigBuilder, 
    		ExtServiceProviderImpl.class,  ExtServiceImpl.class, 
  			ExtResourceImpl.class);

    	this.packetType = packetType;
        super.setResourceConfigType(ExtResourceConfig.class);
        
    	XmlResourceConfigHandler handler = ExtResourceConfig.loadFromXml(
    			mediator, configurationFile);
    	
    	if(handler != null)
    	{
	        this.commands =  handler.getCommandDefinitions();
	        FixedProviders fixedProviders = handler.getDeviceDefinitions();
	        this.fixedProviders = fixedProviders.getProviderMap();
	        super.fixed = fixedProviders.getFixedMap();
	        super.profiles = handler.getProfiles();
	        
	        super.addResourceConfigCatalog(new ExtResourceConfigCatalog(
	        		handler, defaults));
    	} else
    	{
	        this.commands =  new Commands(null);
	        this.fixedProviders = Collections.<String,String>emptyMap(); 
	        super.fixed = Collections.<String,List<String>>emptyMap(); 
	        super.profiles = Collections.<String,List<String>>emptyMap();
    	}
	    this.packetType = packetType;	    
		this.mediator.info("ExtModelConfiguration initialized...");
    }
    
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
	 * setDefaultResourceType(java.lang.Class)
	 */
	@Override
	public ExtModelConfiguration setDefaultResourceType(
			Class<? extends Resource> defaultResourceType)
	{
		super.setDefaultResourceType(defaultResourceType);
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
	 * setDefaultDataType(java.lang.Class)
	 */
	@Override
	public ExtModelConfiguration setDefaultDataType(Class<?> defaultDataType)
	{
		super.setDefaultDataType(defaultDataType);
		return this;			
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
	 * setDefaultModifiable(org.eclipse.sensinact.gateway.common.primitive.Modifiable)
	 */
	@Override
	public ExtModelConfiguration setDefaultModifiable(Modifiable defaultModifiable)
	{
		super.setDefaultModifiable(defaultModifiable);
		return this;		
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
	 * setDefaultUpdatePolicy(org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy)
	 */
	@Override
	public ExtModelConfiguration setDefaultUpdatePolicy(UpdatePolicy defaultUpdatePolicy)
	{
		super.setDefaultUpdatePolicy(defaultUpdatePolicy);
		return this;			
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
	 * setProviderImplementationType(java.lang.Class)
	 */
	@Override
	public ExtModelConfiguration setProviderImplementationType(
			Class<? extends ServiceProviderImpl> serviceProviderType)
	{
		super.setProviderImplementationType(serviceProviderType);
		return this;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
	 * setServiceImplmentationType(java.lang.Class)
	 */
	@Override
	public ExtModelConfiguration setServiceImplmentationType(
			Class<? extends ServiceImpl> serviceType)
	{
		super.setServiceImplmentationType(serviceType);
		return this;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
	 * setResourceImplementationType(java.lang.Class)
	 */
	@Override
	public ExtModelConfiguration setResourceImplementationType(
			Class<? extends ResourceImpl> resourceType)
	{
		super.setResourceImplementationType(resourceType);
		return this;
	}

	/**
	 * @param lockedAtInitializationTime
	 * @return
	 */
	public ExtModelConfiguration setLockedAtInitializationTime(
			boolean lockedAtInitializationTime) 
	{
		this.lockedAtInitializationTime = lockedAtInitializationTime;
		return this;
	}
	
	/**
	 * @return
	 */
	public boolean isLockedAtInitializationTime() 
	{
		return this.lockedAtInitializationTime;
	}

	/**
	 * @param isDesynchronized
	 */
	public  ExtModelConfiguration setDesynchronized(
			boolean isDesynchronized)
	{
		this.isDesynchronized = isDesynchronized;
		return this;
	}
	
	/**
	 * @return
	 */
    public boolean isDesynchronized()
    {
	    return this.isDesynchronized;
    }

    /**
     * Defines the instance of {@link ConnectorCustomizer} to attach to
     * the {@link Connector}(s) instantiated by this ExtModelConfiguration
     * 
     * @param customizer the {@link ConnectorCustomizer} to attach to
     * the {@link Connector}(s) instantiated by this ExtModelConfiguration
     */
    public void setConnectorCustomizer(
    		ConnectorCustomizer<? extends Packet> customizer)
    {
    	this.customizer = customizer;
    }

    /**
     * Returns the command bytes array mapped to the 
     * {@link Task.CommandType} passed as parameter
     * 
     * @param commandType
     *     the {@link Task.CommandType}
     *     
     * @return 
     *      the command bytes array for the 
     *      specified {@link Task.CommandType}
     */
    public byte[] getCommandIdentifier(Task.CommandType commandType)
    {
        return this.commands.getCommand(commandType);
    }
	
	/**
     * the set of bytes array commands mapped to
     * existing {@link Task.CommandType}s
	 * 
	 * @return
	 *     the set of bytes array commands mapped to
	 *     existing {@link Task.CommandType}s
	 *          
	 */
	public Commands getCommands() 
	{
		return this.commands;
	}

	/**
	 * Returns the a Map in which keys are the name of pre-defined
	 * service providers and the values are their profile
	 *  
	 * @return the mapping between pre-defined service providers 
	 * and their profile
	 */
	public Map<String,String> getFixedProviders()
	{
		return Collections.unmodifiableMap(this.fixedProviders);
	}
	
	/**
	 * @return
	 */
	public Class<? extends Packet> getPacketType() 
	{
		return this.packetType;
	} 
	
	/**
	 * @param connector
	 * @return
	 * @throws InvalidProtocolStackException
	 */
	public <P extends Packet> Connector<P> connect(
		ProtocolStackEndpoint<P> connector)
	    throws InvalidProtocolStackException
	{
		Connector<P> processor = null;
		try
		{
			processor = this.newProcessor(connector);
		}
		catch (ClassCastException e)
		{
			throw new InvalidProtocolStackException(e);
		}		
		return processor;
	}
        
    /**
     * Creates and returns a new {@link Connector} instance 
     * to connect to the {@link ProtocolStackEndpoint} passed 
     * as parameter
     * 
     * @param endpoint
     * 		the {@link ProtocolStackEndpoint} to which the {@link 
     * 		Connector} to instantiate will be connected to
     * @return 
     * 		a new {@link Connector} instance
     */ 
    protected <P extends Packet> Connector<P> newProcessor(
    	ProtocolStackEndpoint<P> endpoint) throws InvalidProtocolStackException
    {
    	if(this.customizer != null)
    	{
    		return new Connector<P>(mediator, endpoint,
    			this, (ConnectorCustomizer<P>) this.customizer);
    	}
		return new Connector<P>(mediator, endpoint, this);
    }
}
