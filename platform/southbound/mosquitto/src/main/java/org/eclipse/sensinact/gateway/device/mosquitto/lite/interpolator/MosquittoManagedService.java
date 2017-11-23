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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.interpolator;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTPropertyFileConfig;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.impl.MQTTPropertyFileConfigImpl;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.interpolator.exception.IncompleteDataException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
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
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MosquittoManagedService implements ManagedServiceFactory {

    public static final String MANAGERNAME="mosquitto";
    private static final Logger LOG = LoggerFactory.getLogger(MosquittoManagedService.class);
    private final BundleContext context;
    private HashMap<String,ServiceRegistration<MQTTPropertyFileConfig>> registrations=new HashMap<>();

    public MosquittoManagedService(BundleContext context){
        LOG.debug("Instantiating mosquitto managed service..");
        this.context=context;
    }

    @Override
    public String getName() {
        return MANAGERNAME;
    }

    private void logDictionnary(Dictionary dictionary){

        Enumeration el=dictionary.keys();

        LOG.debug("Listing service properties received..");

        while (el.hasMoreElements()){
            String key=(String)el.nextElement();
            LOG.debug("key {} value {}",key,dictionary.get(key));
        }

        LOG.debug("Listing service properties received.. finished");
    }

    @Override
    public void updated(String s, Dictionary dictionary) throws ConfigurationException {

        LOG.debug("Instantiating mosquitto managed service..");

        logDictionnary(dictionary);

        if(registrations.get(Constants.SERVICE_PID)==null){

            LOG.debug("new registration for file {}",dictionary.get("felix.fileinstall.filename").toString());
            register(s,dictionary);

        }else {

            LOG.debug("update information received, the instance will be distroyed and re-created for file {}",dictionary.get("felix.fileinstall.filename").toString());
            deleted(s);
            register(s,dictionary);

        }


    }

    private void register(String servicePID, Dictionary dictionary){

        try {
            MosquittoManagedServiceIterpolate mit=new MosquittoManagedServiceIterpolate(MQTTPropertyFileConfigImpl.class,dictionary);
            MQTTPropertyFileConfig config=(MQTTPropertyFileConfig)mit.getInstance();
            LOG.debug("Interpolation of service PID result {}",config.toString());
            ServiceRegistration<MQTTPropertyFileConfig> registration= (ServiceRegistration<MQTTPropertyFileConfig>) context.registerService(MQTTPropertyFileConfig.class.getCanonicalName(), config, dictionary);
            LOG.info("Service registered for id {}",config.getId());
            registrations.put(servicePID,registration);
        } catch (IncompleteDataException e) {
            LOG.error("Failed to interpolate properties",e);
        }

    }

    @Override
    public void deleted(String servicePID) {

        ServiceRegistration sr=registrations.remove(servicePID);

        if(sr==null){

            LOG.warn("The service pid {} does not exist, impossible to remove instance",servicePID);

        }else {

            LOG.debug("destroying instance for servicePID {}",servicePID);
            sr.unregister();
            LOG.debug("servicePID {} destroyed",servicePID);

        }

    }
}
