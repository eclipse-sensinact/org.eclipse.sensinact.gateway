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
package org.eclipse.sensinact.gateway.device.mosquitto.lite;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

import static org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTPropertyFileConfig;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTPojoConfigTracker;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTPropertyFileConfigTracker;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTPacket;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Sensinact bundle activator
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class Activator extends AbstractActivator<Mediator>
{

    private ExtModelConfiguration MQTTDeviceFactory;
    private LocalProtocolStackEndpoint<MQTTPacket> MQTTConnector;
    private ServiceRegistration registration;
    private ServiceTracker MQTTBusServiceTracker;

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    @Override
    public void doStart() throws Exception {

        MQTTDeviceFactory =new ExtModelInstanceBuilder(mediator,MQTTPacket.class)
                .withServiceBuildPolicy((byte) (BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()))
                .withResourceBuildPolicy((byte) (BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()))
                .withStartAtInitializationTime(true)
                .buildConfiguration("mosquitto-resource.xml", Collections.emptyMap());
        MQTTConnector =new LocalProtocolStackEndpoint<MQTTPacket>(mediator);
        MQTTConnector.connect(MQTTDeviceFactory);

        this.registration = super.mediator.getContext().registerService(
                ProtocolStackEndpoint.class, MQTTConnector, null);
        MQTTBusServiceTracker = new ServiceTracker(super.mediator.getContext(), Provider.class.getName(), new MQTTPojoConfigTracker(MQTTConnector,super.mediator.getContext()));
        MQTTBusServiceTracker.open(true);
        MQTTBusServiceTracker = new ServiceTracker(super.mediator.getContext(), MQTTPropertyFileConfig.class.getName(), new MQTTPropertyFileConfigTracker(MQTTConnector,super.mediator.getContext()));
        MQTTBusServiceTracker.open(true);

    }

    @Override
    public void doStop() throws Exception{
        MQTTBusServiceTracker.close();
        MQTTConnector.stop();
        MQTTConnector = null;
        MQTTDeviceFactory = null;
        try{
            this.registration.unregister();

        }finally{
            this.registration = null;
        }
    }

    @Override
    public Mediator doInstantiate(BundleContext context)
    {
        return new Mediator(context);
    }
}
