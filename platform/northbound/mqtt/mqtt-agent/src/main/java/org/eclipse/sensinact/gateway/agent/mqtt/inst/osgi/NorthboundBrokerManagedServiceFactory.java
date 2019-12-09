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
package org.eclipse.sensinact.gateway.agent.mqtt.inst.osgi;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.AbstractMqttHandler;
import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.GenericMqttAgent;
import org.eclipse.sensinact.gateway.agent.mqtt.inst.internal.SnaEventEventHandler;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;

/**
 */
public class NorthboundBrokerManagedServiceFactory implements ManagedServiceFactory {
	
    public static final String MANAGER_NAME = "mqtt.agent.broker";
	private Map<String, MidAgentCallback> pids;

	private Mediator mediator ;
    
    public NorthboundBrokerManagedServiceFactory(Mediator mediator) {
    	this.mediator = mediator;
    	this.pids = Collections.synchronizedMap(new HashMap<String,MidAgentCallback>());
    }

    /* (non-Javadoc)
     * @see org.osgi.service.cm.ManagedServiceFactory#getName()
     */
    @Override
    public String getName() {
        return MANAGER_NAME;
    }

    /* (non-Javadoc)
     * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
     */
    @Override
    public void updated(String servicePID, Dictionary dictionary) throws ConfigurationException {
        if(this.pids.containsKey(servicePID)) {
        	deleted(servicePID);
        }
    	try {
    		//retrieve connecting data
    		String host = (String)dictionary.get("host");
    	    if(host == null) {
    	    	host = "127.0.0.1";
    	    }
    	    String port = (String)dictionary.get("port");
    	    if(port == null) {
    	    	port = "1883";
    	    }
    	    String qos = (String)dictionary.get("qos");
    	    if(qos == null) {
    	    	qos = "1";
    	    }
    	    String prefix = (String)dictionary.get("prefix");
    	    if(prefix == null){
    	    	prefix = "/";
    	    }
    	    String protocol = (String)dictionary.get("protocol");
    	    if(protocol == null) {
    	    	protocol = "tcp";
    	    }
    	    String username = (String)dictionary.get("username");
    	    String password = (String)dictionary.get("password");
    	    
    	    //retrieve filtering data 
    	    boolean defined = false;
    	    boolean isPattern = false;
    	    boolean isComplement = false;
    	    
    	    JSONArray constraints = null;
    	    SnaMessage.Type[] handled = null;
    	    
    	    String pattern = (String)dictionary.get("pattern");
    	    if(pattern != null) {
    	    	isPattern = Boolean.parseBoolean(pattern);
    	    	defined = true;
    	    }
    	    String sender = (String)dictionary.get("sender");
    	    if(sender == null) {
    	    	sender = "(/[^/]+)+";
    	    	isPattern = true;
    	    } else {
    	    	defined = true;
    	    }
    	    String complement = (String)dictionary.get("complement");
    	    if(complement != null) {
    	    	isComplement = Boolean.parseBoolean(complement);
    	    	defined = true;
    	    }
    	    String types = (String)dictionary.get("types");
    	    if(types == null) {
    	    	handled = SnaMessage.Type.values();
    	    } else {
    	    	try {
	    	    	JSONArray array = new JSONArray(types);
	    	    	handled = new SnaMessage.Type[array.length()];
	    	    	for(int i = 0;i < array.length(); i++) {
	    	    		handled[i] = SnaMessage.Type.valueOf(array.getString(i));
	    	    	}
	    	    	defined = true;
    	    	} catch(JSONException | NullPointerException e) {
    	    		handled = SnaMessage.Type.values();
    	    		mediator.error("Unable to build the array of handled message types",e);
    	    	}
    	    }
    	    String conditions = (String)dictionary.get("conditions");
    	    if(conditions != null) {
    	    	try {
    	    		constraints = new JSONArray(conditions);
    	    		defined = true;
    	    	} catch(JSONException e) {
    	    		constraints = new JSONArray();
    	    		mediator.error("Unable to build the constraint expession",e);
    	    	}
    	    }
    	    SnaFilter filter = null;
    	    if(defined) {
    	    	filter = new SnaFilter(mediator,sender,isPattern,isComplement,constraints); 
    	    	filter.addHandledType(handled);
    	    }
    	    final SnaFilter flt = filter;
    	    final String broker = String.format("%s://%s:%s",protocol,host,port);    	    
    	    
    	    final AbstractMqttHandler  handler = new SnaEventEventHandler(prefix);
    	    mediator.debug("Starting MQTT Agent point to server %s with prefix %s and qos %s",broker,prefix,qos);
            
    	    GenericMqttAgent agent;
            if(username!=null&&password!=null&&!username.toString().trim().equals("")&&!password.toString().trim().equals("")){
                agent = new GenericMqttAgent(broker, Integer.parseInt(qos), prefix, username.toString(),password.toString());
            }else {
                agent = new GenericMqttAgent(broker, Integer.parseInt(qos), prefix);
            }
            handler.setAgent(agent);
            String registration = mediator.callService(Core.class, new Executable<Core, String>() {
                @Override
                public String execute(Core core) throws Exception {
                    return core.registerAgent(mediator, handler, flt);
                }
            });
            mediator.info("Agent with id:[%s] registered ", registration);
            this.pids.put(servicePID, handler);
    	} catch (Exception e) {
			mediator.error(e);
		}
    }

	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
	 */
	@Override
    public void deleted(String servicePID) {
    	try {
    		MidAgentCallback callback = this.pids.remove(servicePID);
        	callback.stop();    		
    	} catch (Exception e) {
			mediator.error(e);
		}
    }
	
	public void stop() {
		for(MidAgentCallback callback :this.pids.values()) {
			callback.stop();
		}
		this.pids.clear();
	}
}