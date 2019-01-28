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
public abstract class MqttActivator extends GenericProtocolStackendpoint {

    @Override
    public MqttProtocolStackEndpoint getEndPoint() {
        return new MqttProtocolStackEndpoint(mediator);
    }

    public Class getPacketClass(){
        return MqttPacket.class;
    }
}
