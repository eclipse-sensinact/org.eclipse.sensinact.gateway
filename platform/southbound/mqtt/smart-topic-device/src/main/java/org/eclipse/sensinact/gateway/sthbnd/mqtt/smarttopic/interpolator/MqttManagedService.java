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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.interpolator;

import org.eclipse.sensinact.gateway.common.interpolator.Interpolator;
import org.eclipse.sensinact.gateway.common.interpolator.exception.InterpolationException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.device.MqttPropertyFileConfig;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.device.impl.MqttPropertyFileConfigImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Managed Service factory that generates the Service that will be used to create the MQTT connection to a topic
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MqttManagedService implements ManagedServiceFactory {
    public static final String MANAGER_NAME = "bridge.sb.mqtt";
    private static final String OSGI_PROPERTY_FOR_FILENAME = "felix.fileinstall.filename";
    private static final Logger LOG = LoggerFactory.getLogger(MqttManagedService.class);
    private final BundleContext context;
    private HashMap<String, ServiceRegistration<MqttPropertyFileConfig>> tablepidServiceRegistration = new HashMap<>();

    public MqttManagedService(BundleContext context) {
        LOG.debug("Instantiating mosquitto managed service..");
        this.context = context;
    }

    @Override
    public String getName() {
        return MANAGER_NAME;
    }

    private void logDictionnary(Dictionary dictionary) {
        Enumeration el = dictionary.keys();
        LOG.debug("Listing service properties received..");
        while (el.hasMoreElements()) {
            String key = (String) el.nextElement();
            LOG.debug("key {} value {}", key, dictionary.get(key));
        }
        LOG.debug("Listing service properties received.. finished");
    }

    @Override
    public void updated(String servicePID, Dictionary dictionary) throws ConfigurationException {
        LOG.debug("Instantiating mosquitto managed service pid {} ..", servicePID);
        logDictionnary(dictionary);
        if (tablepidServiceRegistration.get(servicePID) == null) {
            LOG.debug("new registration for file {}", dictionary.get(OSGI_PROPERTY_FOR_FILENAME).toString());
            register(servicePID, dictionary);
        } else {
            LOG.debug("update information received, the instance will be distroyed and re-created for file {}", dictionary.get(OSGI_PROPERTY_FOR_FILENAME).toString());
            deleted(servicePID);
            register(servicePID, dictionary);
        }
    }

    private void register(String servicePID, Dictionary dictionary) {
        try {
            MqttPropertyFileConfig config = new Interpolator(dictionary).getNewInstance(MqttPropertyFileConfigImpl.class);
            LOG.debug("Interpolation result of service PID {} with POJO {}", config.toString(), config.getClass().getCanonicalName());
            ServiceRegistration<MqttPropertyFileConfig> registration = (ServiceRegistration<MqttPropertyFileConfig>) context.registerService(MqttPropertyFileConfig.class.getCanonicalName(), config, dictionary);
            LOG.info("Service registered for id {}", config.getId());
            tablepidServiceRegistration.put(servicePID, registration);
        } catch (InterpolationException e) {
            LOG.error("Failed to interpolate properties in the service pid {}", servicePID, e);
        }
    }

    @Override
    public void deleted(String servicePID) {
        LOG.info("removing service pid {}", servicePID);
        ServiceRegistration sr = tablepidServiceRegistration.remove(servicePID);
        if (sr == null) {
            LOG.warn("Service registration for service pid {} does not exist, impossible to remove instance", servicePID);
        } else {
            LOG.debug("destroying instance for servicePID {}", servicePID);
            sr.unregister();
            LOG.debug("servicePID {} destroyed", servicePID);
        }
    }
}