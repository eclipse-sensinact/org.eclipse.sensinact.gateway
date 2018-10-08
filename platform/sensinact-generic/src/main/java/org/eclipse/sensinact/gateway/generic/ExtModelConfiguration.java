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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.DefaultResourceConfigBuilder;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy;
import org.eclipse.sensinact.gateway.core.ResourceConfigBuilder;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.security.AccessNodeImpl;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.parser.Commands;
import org.eclipse.sensinact.gateway.generic.parser.FixedProviders;
import org.eclipse.sensinact.gateway.generic.parser.XmlResourceConfigHandler;
import org.eclipse.sensinact.gateway.util.ReflectUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Manages IO for a set of {@link ServiceProvider}s
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ExtModelConfiguration<P extends Packet> extends ModelConfiguration {
	
    /**
     * Returns true if the two bytes arrays passed as parameters are equals
     * (same content)
     *
     * @param base    the first of the two bytes arrays to compare
     * @param compare the second of the two bytes arrays to compare
     * @return true if the two bytes arrays passed as parameters are equals;
     * false otherwise
     */
    public static boolean compareBytesArrays(byte[] base, byte[] compare) {
        if (base == null) {
            return compare == null;
        }
        if (compare == null) {
            return false;
        }
        if (base.length != compare.length) {
            return false;
        }
        for (int index = 0; index < base.length; index++) {
            if (base[index] != compare[index]) {
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
    protected Class<P> packetType;

    /**
     * The {@link ConnectorCustomizer} to attach to the
     * connector to be built 
     */
    protected ConnectorCustomizer<P> customizer;

    /**
     * The {@link Connector} type to be built by this ExtModelConfiguration
     */
    protected Class<? extends Connector<P>> connectorType;
    
    /**
     * the set of {@link Task.CommandType}
     */
    protected Commands commands;
    
    /**
     * the initial set of service providers map to their profile
     */
    protected Map<String, String> fixedProviders;
    
    /**
     * Constructor
     *
     * @param mediator
     * 		the {@link Mediator} allowing the ExtModelConfiguration to be instantiated to interact 
     * 		with the OSGi host environment
     * @param accessTree 
     * 		the {@link AccessTree} defining the secured access rights applying on sensiNact 
     * 		resource model instances configured by the ExtModelConfiguration to be instantiated
     * @param packetType
     * @param configurationFile
     * @param defaults
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public ExtModelConfiguration(
    	Mediator mediator, 
    	AccessTreeImpl<? extends AccessNodeImpl<?>> accessTree, 
    	Class<P> packetType, 
    	String configurationFile, 
    	Map<String, String> defaults) 
    		throws ParserConfigurationException, SAXException, IOException {
        this(mediator, 
        	accessTree, 
        	packetType, 
        	new DefaultResourceConfigBuilder(), 
        	configurationFile, 
        	defaults);
    }

    /**
     * Constructor
     *
     * @param mediator
     * 		the {@link Mediator} allowing the ExtModelConfiguration to be instantiated to interact 
     * 		with the OSGi host environment
     * @param accessTree
     * 		the {@link AccessTree} defining the secured access rights applying on sensiNact 
     * 		resource model instances configured by the ExtModelConfiguration to be instantiated
     * @param packetType
     * @param defaultResourceConfigBuilder
     * @param configurationFile
     * @param defaults
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public ExtModelConfiguration(
    	Mediator mediator, 
    	AccessTreeImpl<? extends AccessNodeImpl<?>> accessTree, 
    	Class<P> packetType, 
    	ResourceConfigBuilder defaultResourceConfigBuilder, 
    	String configurationFile, 
    	Map<String, String> defaults) 
    			throws ParserConfigurationException, SAXException, IOException {
        this(
        	mediator, 
        	accessTree, 
        	packetType, 
        	defaultResourceConfigBuilder, 
        	configurationFile, 
        	defaults, 
        	ExtServiceProviderImpl.class, 
        	ExtServiceImpl.class, 
        	ExtResourceImpl.class);
    }

    /**
     * Constructor
     *
     * @param mediator                     the {@link Mediator} allowing the ExtModelConfiguration
     *                                     to be instantiated to interact with the OSGi host environment
     * @param accessTree                   the {@link AccessTree} defining the secured access
     *                                     rights applying on sensiNact resource model instances configured
     *                                     by the ExtModelConfiguration to be instantiated
     * @param packetType
     * @param defaultResourceConfigBuilder
     * @param configurationFile
     * @param defaults
     * @param defaultServiceProviderType
     * @param defaultServiceType
     * @param defaultResourceType
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    protected ExtModelConfiguration(
		Mediator mediator, 
		AccessTreeImpl<?> accessTree, 
		Class<P> packetType, 
		ResourceConfigBuilder defaultResourceConfigBuilder, 
		String configurationFile, 
		Map<String, String> defaults, 
		Class<? extends ExtServiceProviderImpl> defaultServiceProviderType, 
		Class<? extends ExtServiceImpl> defaultServiceType, 
		Class<? extends ExtResourceImpl> defaultResourceType) 
			throws ParserConfigurationException, SAXException, IOException {
        this(mediator, 
    		accessTree, 
    		packetType, 
    		defaultResourceConfigBuilder, 
    		configurationFile != null?mediator.getContext().getBundle().getResource(configurationFile):null, 
    		defaults, 
    		defaultServiceProviderType, 
    		defaultServiceType, 
    		defaultResourceType);
    }

    /**
     * Constructor
     *
     * @param mediator                     the {@link Mediator} allowing the ExtModelConfiguration
     *                                     to be instantiated to interact with the OSGi host environment
     * @param accessTree                   the {@link AccessTree} defining the secured access
     *                                     rights applying on sensiNact resource model instances configured
     *                                     by the ExtModelConfiguration to be instantiated
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
    public ExtModelConfiguration(
    	Mediator mediator, 
    	AccessTreeImpl<?> accessTree, 
    	Class<P> packetType, 
    	ResourceConfigBuilder defaultResourceConfigBuilder, 
    	URL configurationFile, 
    	Map<String, String> defaults, 
    	Class<? extends ExtServiceProviderImpl> defaultServiceProviderType, 
    	Class<? extends ExtServiceImpl> defaultServiceType, 
    	Class<? extends ExtResourceImpl> defaultResourceType) 
    			throws ParserConfigurationException, SAXException, IOException {
        super(
        	mediator, 
        	accessTree, 
        	defaultResourceConfigBuilder, 
        	defaultServiceProviderType, 
        	defaultServiceType, 
        	defaultResourceType);
        
        this.packetType = packetType;
        super.setResourceConfigType(ExtResourceConfig.class);
        
        XmlResourceConfigHandler handler = ExtResourceConfig.loadFromXml(mediator, configurationFile);
        
        Map<String, String> builtFixedProviders = new HashMap<String, String>();
        Map<String, List<String>> builtFixed = new HashMap<String, List<String>>();
        Map<String, List<String>> builtProfiles = new HashMap<String, List<String>>();
        
        if (handler != null) {
            this.commands = handler.getCommandDefinitions();
            FixedProviders fixedProviders = handler.getDeviceDefinitions();
            builtFixedProviders.putAll(fixedProviders.getProviderMap());
            builtFixed.putAll(fixedProviders.getFixedMap());
            builtProfiles.putAll(handler.getProfiles());            
            super.addResourceConfigCatalog(new ExtResourceConfigCatalog(handler, defaults));
        } else {
            this.commands = new Commands(null);
        }
        //load all xml catalogs from org.eclipse.sensinact.gateway.catalog
        //can we imagine to replace the initial mechanism of the declared xml resources description file ?
        Enumeration<URL> catalogs = mediator.getContext().getBundle().findEntries(
        		"/org/eclipse/sensinact/gateway/catalog/", "*.xml", false);
        
        if(catalogs!= null) {
        	while(catalogs.hasMoreElements()){
        		URL catalog = catalogs.nextElement();
        		if(catalog==null||!catalog.toExternalForm().endsWith(".xml")) {
        			continue;
        		}
        		try {
	        		handler = ExtResourceConfig.loadFromXml(mediator, catalog);
	        		if(handler == null) {
	        			continue;
	        		}
	                FixedProviders fixedProviders = handler.getDeviceDefinitions();
	                builtFixedProviders.putAll(fixedProviders.getProviderMap());
	                builtFixed.putAll(fixedProviders.getFixedMap());
	                
	                Iterator<Entry<String, List<String>>> iterator =
	                		handler.getProfiles().entrySet().iterator();
	                while(iterator.hasNext()) {
	                	Entry<String, List<String>> entry = iterator.next();
	                	if(builtProfiles.containsKey(entry.getKey())) {
	                		builtProfiles.get(entry.getKey()).addAll(entry.getValue());
	                	} else{
	                		builtProfiles.put(entry.getKey(), entry.getValue());
	                	}
	                }
	                super.addResourceConfigCatalog(new ExtResourceConfigCatalog(handler, defaults));
        		}catch( ParserConfigurationException | SAXException | IOException e){
        			mediator.debug("%s does not contains a valid catalog", catalog.toExternalForm());
        			continue;
        		}
        	}
        }
        super.fixed = Collections.unmodifiableMap(builtFixed);
        super.profiles = Collections.unmodifiableMap(builtProfiles);
        this.fixedProviders =  Collections.unmodifiableMap(builtFixedProviders);
        
        this.mediator.info("ExtModelConfiguration initialized...");
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
     * setDefaultResourceType(java.lang.Class)
     */
    @Override
    public ExtModelConfiguration<P> setDefaultResourceType(Class<? extends Resource> defaultResourceType) {
        super.setDefaultResourceType(defaultResourceType);
        return this;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
     * setDefaultDataType(java.lang.Class)
     */
    @Override
    public ExtModelConfiguration<P> setDefaultDataType(Class<?> defaultDataType) {
        super.setDefaultDataType(defaultDataType);
        return this;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
     * setDefaultModifiable(org.eclipse.sensinact.gateway.common.primitive.Modifiable)
     */
    @Override
    public ExtModelConfiguration<P> setDefaultModifiable(Modifiable defaultModifiable) {
        super.setDefaultModifiable(defaultModifiable);
        return this;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
     * setDefaultUpdatePolicy(org.eclipse.sensinact.gateway.core.Resource.UpdatePolicy)
     */
    @Override
    public ExtModelConfiguration<P> setDefaultUpdatePolicy(UpdatePolicy defaultUpdatePolicy) {
        super.setDefaultUpdatePolicy(defaultUpdatePolicy);
        return this;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
     * setProviderImplementationType(java.lang.Class)
     */
    @Override
    public ExtModelConfiguration<P> setProviderImplementationType(Class<? extends ServiceProviderImpl> serviceProviderType) {
        super.setProviderImplementationType(serviceProviderType);
        return this;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
     * setServiceImplmentationType(java.lang.Class)
     */
    @Override
    public ExtModelConfiguration<P> setServiceImplmentationType(Class<? extends ServiceImpl> serviceType) {
        super.setServiceImplmentationType(serviceType);
        return this;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.ModelConfiguration#
     * setResourceImplementationType(java.lang.Class)
     */
    @Override
    public ExtModelConfiguration<P> setResourceImplementationType(Class<? extends ResourceImpl> resourceType) {
        super.setResourceImplementationType(resourceType);
        return this;
    }

    /**
     * @param lockedAtInitializationTime
     * @return
     */
    public ExtModelConfiguration<P> setLockedAtInitializationTime(boolean lockedAtInitializationTime) {
        this.lockedAtInitializationTime = lockedAtInitializationTime;
        return this;
    }

    /**
     * @return
     */
    public boolean isLockedAtInitializationTime() {
        return this.lockedAtInitializationTime;
    }

    /**
     * @param isDesynchronized
     */
    public ExtModelConfiguration<P> setDesynchronized(boolean isDesynchronized) {
        this.isDesynchronized = isDesynchronized;
        return this;
    }

    /**
     * @return
     */
    public boolean isDesynchronized() {
        return this.isDesynchronized;
    }

    /**
     * Defines the instance of {@link ConnectorCustomizer} to attach to
     * the {@link Connector}(s) instantiated by this ExtModelConfiguration
     *
     * @param customizer the {@link ConnectorCustomizer} to attach to
     *                   the {@link Connector}(s) instantiated by this ExtModelConfiguration
     */
    public void setConnectorCustomizer(ConnectorCustomizer<P> customizer) {
        this.customizer = customizer;
    }
    
    /**
     * Defines the {@link Connector} type to be instantiated by this ExtModelConfiguration
     *
     * @param connectorType 
     * 		the {@link Connector} type to be instantiated by this ExtModelConfiguration
     */
    public ExtModelConfiguration<P> setConnectorType(Class<? extends Connector<P>> connectorType) {
        this.connectorType = connectorType;
        return this;
    } 
    
    /**
     * Returns the command bytes array mapped to the
     * {@link Task.CommandType} passed as parameter
     *
     * @param commandType the {@link Task.CommandType}
     * @return the command bytes array for the
     * specified {@link Task.CommandType}
     */
    public byte[] getCommandIdentifier(Task.CommandType commandType) {
        return this.commands.getCommand(commandType);
    }

    /**
     * the set of bytes array commands mapped to
     * existing {@link Task.CommandType}s
     *
     * @return the set of bytes array commands mapped to
     * existing {@link Task.CommandType}s
     */
    public Commands getCommands() {
        return this.commands;
    }

    /**
     * Returns the a Map in which keys are the name of pre-defined
     * service providers and the values are their profile
     *
     * @return the mapping between pre-defined service providers
     * and their profile
     */
    public Map<String, String> getFixedProviders() {
        return Collections.unmodifiableMap(this.fixedProviders);
    }

    /**
     * @return
     */
    public Class<P> getPacketType() {
        return this.packetType;
    }

    /**
     * @param endpoint
     * @return
     * @throws InvalidProtocolStackException
     */
    public Connector<P> connect(ProtocolStackEndpoint<P> endpoint) 
    	throws InvalidProtocolStackException {
    	Connector<P> connector = null;
	    try {
	    	connector = this.newConnector(endpoint);
	    } catch (ClassCastException e) {
	        throw new InvalidProtocolStackException(e);
	    }
    	return connector;
    }

    /**
     * Creates and returns a new {@link Connector} instance
     * to connect to the {@link ProtocolStackEndpoint} passed
     * as parameter
     *
     * @param endpoint the {@link ProtocolStackEndpoint} to which the {@link
     *                 Connector} to instantiate will be connected to
     * @return a new {@link Connector} instance
     */
    protected <N extends Connector<P>> N newConnector(ProtocolStackEndpoint<P> endpoint) 
    	throws InvalidProtocolStackException {
    	N connector = null;
    	if(this.connectorType != null) {
            if (this.customizer != null) {
                connector = (N) ReflectUtils.<N>getTheBestInstance(
                (Class<N>)this.connectorType, new Object[] {mediator, endpoint, this, this.customizer});
            } else {
                connector = (N) ReflectUtils.<N>getTheBestInstance(
                (Class<N>)this.connectorType, new Object[] {mediator, endpoint, this});
            }  		
    	} else {
            if (this.customizer != null) {
                return (N) new Connector<P>(mediator, endpoint, this, (ConnectorCustomizer<P>) this.customizer);
            } else {
            	connector = (N) new Connector<P>(mediator, endpoint, this);
            }  		
    	}
        return connector;
    }
}
