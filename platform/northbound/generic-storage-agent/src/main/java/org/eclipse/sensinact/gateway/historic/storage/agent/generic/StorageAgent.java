/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.historic.storage.agent.generic;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.message.AgentRelay;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.core.message.whiteboard.AbstractAgentRelay;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

/**
 * {@link AgentRelay} in charge of relaying event notifications to the {@link StorageConnection}
 * in charge of the effective data storage
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class StorageAgent extends AbstractAgentRelay {
	
    private static final Logger LOG = LoggerFactory.getLogger(StorageAgent.class);

	protected static final String STORAGE_AGENT_KEYS_PROPS = "org.eclipse.sensinact.gateway.history.keys";
	protected static final String STORAGE_KEY_PROCESSOR_PROVIDER = "sensinact.history.key.processor";
    
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
    protected abstract String[] getKeyProcessorProviderIdentifiers();
	
    protected BundleContext bc;
    
	private Map<String,Executable<SnaMessage<?>,Object>> keyProcessors;
	private Map<String,Object> storageKeyValuesMap;
	private Map<String,String> storageKeyNamesMap;
	
	/**
	 * The string formated location of service providers that have already been
	 * processed by this {@link StorageAgent}
	 */
	private Map<String, String> locations;
	
    /**
     * the {@link StorageConnection} in charge of effective storage
     */
    private StorageConnection storageConnection;

    /**
     * Constructor
     */
    public StorageAgent(BundleContext bc) {
        super();
        this.bc = bc;
        this.locations = new HashMap<>();		
		this.storageKeyValuesMap = new HashMap<>();		
		this.keyProcessors = new HashMap<>();		

		this.keyProcessors.put("path", new Executable<SnaMessage<?>,Object>(){
			@Override
			public Object execute(SnaMessage<?> message) throws Exception {
				String path = message.getPath();
				String[] pathElements = UriUtils.getUriElements(path);
				if(pathElements.length==3)
					return path.concat("/value");
				return path;
			}			
		});	
		this.keyProcessors.put("resource", new Executable<SnaMessage<?>,Object>(){
			@Override
			public Object execute(SnaMessage<?> message) throws Exception {
				String path = message.getPath();
				String[] pathElements = UriUtils.getUriElements(path);
				if(pathElements.length > 2)
					return pathElements[2];
				return null;
			}			
		});	
		this.keyProcessors.put("location", new Executable<SnaMessage<?>,Object>(){
			@Override
			public Object execute(SnaMessage<?> message) throws Exception {
		        String uri = message.getPath();
				String[] pathElements = UriUtils.getUriElements(uri);
				return StorageAgent.this.getLocation(pathElements[0]);
			}			
		});
    }

    protected Map<String,Executable<SnaMessage<?>,Object>> loadRegisteredProcessors() {
    	
    	Map<String,Executable<SnaMessage<?>,Object>>processors = new HashMap<>();
    	processors.putAll(this.keyProcessors);

		String[] keyProcessorProviderIdentifiers = getKeyProcessorProviderIdentifiers();
		
		if(keyProcessorProviderIdentifiers != null && keyProcessorProviderIdentifiers.length>0) {
			StringBuilder builder = Arrays.stream(keyProcessorProviderIdentifiers).<StringBuilder>collect(
					StringBuilder::new,(sb,s)->{
						sb.append("(");
						sb.append(STORAGE_KEY_PROCESSOR_PROVIDER);
						sb.append("=");
						sb.append(s);
						sb.append(")");
					},
					(sb1,sb2)->{sb1.append(sb2.toString());}
			);
			if(keyProcessorProviderIdentifiers.length>1) {
				builder.insert(0, "(|");
				builder.append(")");
			}
			String filter = builder.toString();
			try {
				Collection<ServiceReference<StorageKeyProcessorProvider>> refs = 
						bc.getServiceReferences(StorageKeyProcessorProvider.class, filter);

				processors.putAll( refs.stream().map(r -> {return bc.getService(r);}
				).<Map<String,Executable<SnaMessage<?>,Object>>>collect(
						HashMap::new,
						(m,s)->{m.putAll(s.getStorageKeyProcessors());},
						Map::putAll));
				refs.stream().forEach(r -> bc.ungetService(r));
			} catch (Exception e) {
				LOG.error(e.getMessage(),e);
			}		
		}
		return processors;
    }
    
	/**
	 * Defines the mapping between the String paths of the attributes from which the mapped values 
	 * will be extracted and key (field) names used in the storage - ex : /&lt;service&gt;/&lt;resource&gt;/&lt;attribute&gt;=&lt;key&gt;  or 
	 * /&lt;service&gt;/&lt;resource&gt;=&lt;key&gt;, in this last  case the default 'value' attribute will be used 
	 *   
	 * @param keys colon separated String formated keys mapping 
	 */
	protected void setStorageKeys(String keys) {
		if(keys!=null)
			this.storageKeyNamesMap = Arrays.asList(keys.split(",")).stream().<HashMap<String,String>>collect(
				HashMap::new,(h,prop)-> { 
					String keyValuePair[] = prop.split("="); 
					String key = keyValuePair[0].trim();
					if(UriUtils.getUriElements(key).length == 2)
						key = key.concat("/value");
					h.put(key, keyValuePair[1].trim());
				}, Map::putAll);
		else
			this.storageKeyNamesMap = Collections.<String,String>emptyMap();
	}
	
	/**
	 * Registers a new key processor, allowing to feed the data object that will be stored
	 * 
	 * @param key the key String name
	 * @param executor the {@link Executable} allowing to process an SnaMessage to extract 
	 * the value to be mapped to the specified key 
	 */
	public void addFixKeyProcessor(String key, Executable<SnaMessage<?>,Object> executor) {
		if(key !=null && executor !=null)
			this.keyProcessors.put(key, executor);
	}
	
    /**
     * Sets the {@link StorageConnection} in charge of the effective data storage
     * 
     * @param storageConnection the {@link StorageConnection} in charge of the effective data storage
     */
    protected void setStorageConnection(StorageConnection storageConnection) {
        this.storageConnection = storageConnection;
    }
    
	/**
	 * Returns the String location for the service provider whose path is passed as
	 * parameter
	 * 
	 * @param path
	 *            the path of the service provider for which to retrieve the string
	 *            location
	 * @return the String location for the specified path
	 */
	protected String getLocation(String serviceProvider) {
		synchronized (this.locations) {
			return this.locations.get(serviceProvider);
		}
	}

	/**
	 * Sets the String location for the service provider whose path is passed as
	 * parameter
	 * 
	 * @param path
	 *            the path of the service provider for which to set the string
	 *            location
	 * @param location
	 *            the string location to set
	 */
	protected void setLocation(String serviceProvider, String location) {
		synchronized (this.locations) {
			this.locations.put(serviceProvider, location);
		}
	}
	
	private Map<String,Object> preProcessSnaMessage(SnaMessage<?> message){
        final Map<String,Object> ts = new HashMap<>();
        Map<String,Executable<SnaMessage<?>,Object>>processors = this.loadRegisteredProcessors();
        for(Iterator<String> it = processors.keySet().iterator();it.hasNext();) {
			String key = it.next();
			Object val = null; 
			try {
				val = processors.get(key).execute(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(val != null)
				ts.put(key,val);
		}
        return ts;
	}

    @Override
    public void doHandle(SnaUpdateMessageImpl message) {
		this.doHandle( message.getPath(), message.getNotification(),preProcessSnaMessage(message));
    }

    @Override
    public void doHandle(SnaLifecycleMessageImpl message) {
        if (!Lifecycle.RESOURCE_APPEARING.equals(message.getType()) || Resource.Type.ACTION.equals(message.getNotification(Resource.Type.class, "type")))
            return;
        this.doHandle( message.getPath(), message.<JsonObject>get("initial"), preProcessSnaMessage(message));
    }

    // 
    private void doHandle(String path, JsonObject content, Map<String, Object> ts) {
        JsonValue value = content.get(DataResource.VALUE);
        if (content.containsKey(DataResource.VALUE) &&  JsonObject.NULL.equals(value)) {
            //exclude initial null value
            return;
        }        
		String[] pathElements = UriUtils.getUriElements(path);
        String provider = pathElements[0];
        String resource = pathElements[2];
        
        if (LocationResource.LOCATION.equalsIgnoreCase(resource)) {
            //set location and escape
            this.setLocation(provider, String.valueOf(value));
            return;
        }
    	if(this.storageConnection == null)
    		return;

		String attribute = (String) content.getString("name", null);		

		if(pathElements[2].equals(attribute))
			attribute = "value";
		
		if(this.storageKeyNamesMap!=null) {			
			Set<String> keys = this.storageKeyNamesMap.keySet();				
			String serviceUri = UriUtils.getUri(new String[] {pathElements[1],pathElements[2],attribute});
			if(keys.contains(serviceUri)) {				
				this.storageKeyValuesMap.put(UriUtils.getUri(new String[] {pathElements[0], pathElements[1], pathElements[2], attribute}),value);
				return;				
			} else {				
				final String serviceProviderId = pathElements[0];
				keys.forEach(s -> {
					String p = UriUtils.getUri(new String[] {serviceProviderId, s});
					Object tagValue = storageKeyValuesMap.get(p);
					if(tagValue != null) 
						ts.put(this.storageKeyNamesMap.get(s), tagValue);
				});
			}
		}
		Long timestamp;
        JsonValue timestampProp = content.get("timestamp");
        if (timestampProp == null) 
            timestamp = System.currentTimeMillis();
        else {
        	try {
        		timestamp = Long.valueOf(String.valueOf(timestampProp));
        	} catch(NumberFormatException e) {
                timestamp = System.currentTimeMillis();
        	}
        }
        String timestampStr = FORMAT.format(new Date(timestamp));
		
        JsonObjectBuilder jsonObject = JsonProviderFactory.getProvider().createObjectBuilder();
        for(Entry<String, Object> e : ts.entrySet()) {
        	Object tmp = e.getValue();
        	if(tmp instanceof JsonValue) {
        		jsonObject.add(e.getKey(), (JsonValue) tmp);
        	} else if(tmp instanceof Number) {
        		jsonObject.add(e.getKey(), JsonProviderFactory.getProvider().createValue((Number) tmp));
        	} else {
        		jsonObject.add(e.getKey(), tmp.toString());
        	}
        }
        jsonObject.add(DataResource.VALUE, value);
        jsonObject.add("timestamp", timestampStr);
        
        this.storageConnection.stack.push(jsonObject.build());
        
        LOG.debug("pushed to stack : {}/{}...", path, value);
    }

    public void stop() {
    	if(this.storageConnection !=null)
    		this.storageConnection.close();
    }
}
