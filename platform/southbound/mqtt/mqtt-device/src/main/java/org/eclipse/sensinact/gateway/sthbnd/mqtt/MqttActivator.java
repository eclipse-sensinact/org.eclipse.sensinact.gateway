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
package org.eclipse.sensinact.gateway.sthbnd.mqtt;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.InvalidProtocolStackException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttPacket;
import org.osgi.framework.BundleContext;

import java.util.Collections;

import static org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;

/**
 * sensiNact bundle activator
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public abstract class MqttActivator extends AbstractActivator<Mediator> {
    protected MqttProtocolStackEndpoint endPoint;

    @Override
    public void doStart() throws Exception {
        ExtModelConfiguration<MqttPacket> configuration = ExtModelConfigurationBuilder.instance(
    		mediator, MqttPacket.class
    		).withServiceBuildPolicy((byte) (BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy())
    		).withResourceBuildPolicy((byte) (BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | BuildPolicy.BUILD_NON_DESCRIBED.getPolicy())
    		).withStartAtInitializationTime(true
    		).build("mqtt-resource.xml", Collections.emptyMap());
        endPoint = new MqttProtocolStackEndpoint(mediator);
        this.connect(configuration);
    }

    /**
     * @param configuration the configuration for the bridge
     * @throws InvalidProtocolStackException
     */
    protected void connect( ExtModelConfiguration<MqttPacket> configuration) throws InvalidProtocolStackException {
        endPoint.connect(configuration);
        endPoint.connectBrokers();
    }

    @Override
    public void doStop() {
        try {
            endPoint.stop();
        } finally {
            endPoint = null;
        }
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
