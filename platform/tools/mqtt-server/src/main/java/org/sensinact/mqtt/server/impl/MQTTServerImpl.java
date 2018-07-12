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
package org.sensinact.mqtt.server.impl;

import io.moquette.BrokerConstants;
import io.moquette.interception.InterceptHandler;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import org.osgi.framework.BundleContext;
import org.sensinact.mqtt.server.MQTTException;
import org.sensinact.mqtt.server.MQTTServerService;
import org.sensinact.mqtt.server.internal.SensiNactServer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

/**
 * MQTT Server wrapper implementation, to hide dependencies and provide a MQTT server as a auto sufficient bundle
 */
public class MQTTServerImpl implements MQTTServerService {
    private final BundleContext bundleContext;
    SensiNactServer server;
    private HashMap<String, SensiNactServer> mqttServiceMap = new HashMap<>();

    public MQTTServerImpl(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
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
            server = new SensiNactServer(bundleContext);
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

    /**
     * @inheritDoc
     */
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
}
