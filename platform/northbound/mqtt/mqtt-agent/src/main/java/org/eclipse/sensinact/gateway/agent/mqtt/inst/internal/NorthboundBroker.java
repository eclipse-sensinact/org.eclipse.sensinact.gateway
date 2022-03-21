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
package org.eclipse.sensinact.gateway.agent.mqtt.inst.internal;

import java.util.Map;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.GenericMqttAgent;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.converter.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = NorthboundBroker.MQTT_AGENT_BROKER, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class NorthboundBroker {

	@Reference
	private Core core;

	@ObjectClassDefinition(factoryPid = MQTT_AGENT_BROKER)
	interface Config {

		@AttributeDefinition(defaultValue = "127.0.0.1")

		default String host() {
			return "127.0.0.1";
		}

		@AttributeDefinition(defaultValue = "1883")
		default String port() {
			return "1883";
		}

		@AttributeDefinition(defaultValue = "1")
		default String qos() {
			return "1";
		}

		@AttributeDefinition(defaultValue = "/")
		default String prefix() {
			return "/";
		}

		@AttributeDefinition(defaultValue = "tcp")

		default String protocol() {
			return "tcp";
		}

		@AttributeDefinition()
		default String username() {
			return null;
		}

		@AttributeDefinition(type = AttributeType.PASSWORD)
		default String _password() {
			return null;
		}

		default String pattern() {
			return null;
		}

		default String complement() {
			return null;
		}

		default String sender() {
			return null;
		}

		default String types() {
			return null;
		}

		default String conditions() {
			return null;
		}
	}

	@Activate
	BundleContext bc;

	private static final Logger LOG = LoggerFactory.getLogger(NorthboundBroker.class);
	public static final String MQTT_AGENT_BROKER = "mqtt.agent.broker";
	private SnaEventEventHandler handler;

	public NorthboundBroker() {
	}

	@Activate
	public void activate(Map<String, Object> configMap) {

		Config config = Converters.standardConverter().convert(configMap).to(Config.class);
		Mediator mediator = new Mediator(bc);

		try {
			String host = config.host();
    	    if(host == null) {
    	    	host = "127.0.0.1";
    	    }
    	    String port = config.port();
    	    if(port == null) {
    	    	port = "1883";
    	    }
    	    String qos = config.qos();
    	    if(qos == null) {
    	    	qos = "1";
    	    }
    	    String prefix = config.prefix();
    	    if(prefix == null){
    	    	prefix = "/";
    	    }
    	    String protocol = config.protocol();
    	    if(protocol == null) {
    	    	protocol = "tcp";
    	    }
    	    String username = config.username();
    	    String password = config._password();
    	    
    	    //retrieve filtering data 
    	    boolean defined = false;
    	    boolean isPattern = false;
    	    boolean isComplement = false;
    	    
    	    JSONArray constraints = null;
    	    SnaMessage.Type[] handled = null;
    	    
    	    String pattern = config.pattern();
    	    if(pattern != null) {
    	    	isPattern = Boolean.parseBoolean(pattern);
    	    	defined = true;
    	    }
    	    String sender = config.sender();
    	    if(sender == null) {
    	    	sender = "(/[^/]+)+";
    	    	isPattern = true;
    	    } else {
    	    	defined = true;
    	    }
    	    String complement = config.complement();
    	    if(complement != null) {
    	    	isComplement = Boolean.parseBoolean(complement);
    	    	defined = true;
    	    }
    	    String types = config.types();
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
    	    		LOG.error("Unable to build the array of handled message types",e);
    	    	}
    	    }
    	    String conditions = config.conditions();
    	    if(conditions != null) {
    	    	try {
    	    		constraints = new JSONArray(conditions);
    	    		defined = true;
    	    	} catch(JSONException e) {
    	    		constraints = new JSONArray();
    	    		LOG.error("Unable to build the constraint expession",e);
    	    	}
    	    }
    	    SnaFilter filter = null;
    	    if(defined) {
    	    	filter = new SnaFilter(mediator,sender,isPattern,isComplement,constraints); 
    	    	filter.addHandledType(handled);
    	    }

    	    final String broker = String.format("%s://%s:%s",protocol,host,port);    	    
    	    
    	    handler = new SnaEventEventHandler(prefix);
    	    LOG.debug("Starting MQTT Agent point to server {} with prefix {} and qos {}",broker,prefix,qos);
            
    	    GenericMqttAgent agent;
            if(username!=null&&password!=null&&!username.toString().trim().equals("")&&!password.toString().trim().equals("")){
                agent = new GenericMqttAgent(broker, Integer.parseInt(qos), prefix, username.toString(),password.toString());
            }else {
                agent = new GenericMqttAgent(broker, Integer.parseInt(qos), prefix);
            }
            handler.setAgent(agent);

			core.registerAgent(mediator, handler, filter);

		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Deactivate
	public void deleted() {
		handler.stop();
	}
}