/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.sensinact.mqtt.server.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.sensinact.mqtt.server.MQTTException;
import org.sensinact.mqtt.server.MQTTServerService;
import org.sensinact.mqtt.server.internal.SensiNactServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.BrokerConstants;
import io.moquette.interception.InterceptHandler;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;

/**
 * MQTT Server wrapper implementation, to hide dependencies and provide a MQTT server as a auto sufficient bundle
 */
@Designate(ocd = MQTTServerImpl.Config.class)
@Component(immediate = true, service = MQTTServerService.class,configurationPid = MQTTServerImpl.CONFIGURATION_PID)
public class MQTTServerImpl implements MQTTServerService {
    private Logger LOG = LoggerFactory.getLogger(MQTTServerImpl.class);

    @ObjectClassDefinition()
	@interface Config{
		
		boolean autostart() default false;
		String host() default "";
		int port() default 0;
	}
	
   public final static String CONFIGURATION_PID="mqtt.server";

    SensiNactServer server;
    private HashMap<String, SensiNactServer> mqttServiceMap = new HashMap<>();
    
    @Activate
    private BundleContext bc;
    
    
    @Activate
    public void updateConfig(Config config) {
    	
    	if (config.autostart()) {
    		try {
    			if (config.port() > 0) {
    				startService(config.host(), String.valueOf(config.port()));
    			} else {
    				startService();
    			}
    		} catch (MQTTException e) {
    			LOG.error("Failed to start MQTT service", e);
    		}
    	}
    	
    }
    
    @Deactivate
    public void stopServer() {
    	for (SensiNactServer server : mqttServiceMap.values()) {
    		try {
    			server.stopServer();
    		} catch (Exception e) {
    		} finally {
    			mqttServiceMap = new HashMap<>();
    		}
    	}
    }


    private void pushService(String id, SensiNactServer server) throws MQTTException {
        if (mqttServiceMap.get(id) != null) {
            throw new MQTTException(String.format("mqtt service already exists with id %s", id));
        }
        mqttServiceMap.put(id, server);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String startService(final String hostParam, final String portParam) throws MQTTException {
        try {
            String host = "127.0.0.1";
            String port = "1883";
            Properties properties = new Properties();
            if (portParam != null) {
                Integer.parseInt(portParam);
                port = portParam;
            }
            if (hostParam != null) {
                host = hostParam;
            }
            properties.put(BrokerConstants.HOST_PROPERTY_NAME, host);
            properties.put(BrokerConstants.PORT_PROPERTY_NAME, port);
            properties.put(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, "disabled");
            server = new SensiNactServer(bc);
            final String id = String.format("%s:%s", host, port);
            if (mqttServiceMap.get(id) != null) {
                throw new MQTTException(String.format("MQTT service id %s already exists", id));
            }
            IConfig config = new MemoryConfig(properties);
            server.startServer(config, Collections.<InterceptHandler>emptyList(), null, null, null);
            pushService(id, server);
            return id;
        } catch (Exception e) {
            throw new MQTTException("Failed to start MQTT service with the configuration", e);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public String startService(final String port) throws MQTTException {
        return startService(null, port);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String startService() throws MQTTException {
        return startService(null, null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stopService(String id) throws MQTTException {
        SensiNactServer server = mqttServiceMap.remove(id);
        if (server != null) server.stopServer();
    }

    @Override
    public Boolean activeService(String id) throws MQTTException {
        return mqttServiceMap.get(id) != null;
    }


}
	