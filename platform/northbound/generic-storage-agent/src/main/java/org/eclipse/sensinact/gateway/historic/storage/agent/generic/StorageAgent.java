/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.historic.storage.agent.generic;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.core.message.whiteboard.AbstractAgentRelay;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AgentRelay} in charge of relaying event notifications to the {@link StorageConnection}
 * in charge of the effective data storage
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class StorageAgent extends AbstractAgentRelay {
	
    private static final Logger LOG = LoggerFactory.getLogger(StorageAgent.class);

	protected static final String STORAGE_AGENT_KEYS_PROPS = "org.eclipse.sensinact.gateway.history.keys";
    
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	
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
    public StorageAgent() {
        super();
        this.locations = new HashMap<>();		
		this.storageKeyValuesMap = new HashMap<>();		
		this.keyProcessors = new HashMap<>();		
//		Key processor example		
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
//		Define keys mapping at initialization time
    }

	/**
	 * Defines the mapping between key names and the String paths of the attributes from which the mapped values 
	 * will be extracted - ex : &lt;key&gt;=/&lt;service&gt;/&lt;resource&gt;/&lt;attribute&gt; or &lt;key&gt;=/&lt;service&gt;/&lt;resource&gt;, in this last 
	 * case the default 'value' attribute will be used 
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
	
	private Dictionary<String,Object> preProcessSnaMessage(SnaMessage<?> message){
        final Dictionary<String,Object> ts = new Hashtable<>();
        
        for(Iterator<String> it = this.keyProcessors.keySet().iterator();it.hasNext();) {
			String key = it.next();
			Object val = null; 
			try {
				val = this.keyProcessors.get(key).execute(message);
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
        this.doHandle( message.getPath(), message.<JSONObject>get("initial"), preProcessSnaMessage(message));
    }

    // 
    private void doHandle(String path, JSONObject content, Dictionary<String, Object> ts) {
        Object value = content.opt(DataResource.VALUE);
        if (JSONObject.NULL.equals(value)) {
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

		String attribute = (String) content.opt("name");		

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
        Object timestampProp = content.opt("timestamp");
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
		
        JSONObject jsonObject = new JSONObject();
        for(Enumeration<String> it = ts.keys();it.hasMoreElements();) {
        	String k = it.nextElement();
        	jsonObject.put(k, ts.get(k));
        }
        jsonObject.put(DataResource.VALUE, value);
        jsonObject.put("timestamp", timestampStr);
        
        this.storageConnection.stack.push(jsonObject);
        
        LOG.debug("pushed to stack : {}/{}...", path, value);
    }

    public void stop() {
    	if(this.storageConnection !=null)
    		this.storageConnection.close();
    }
}
