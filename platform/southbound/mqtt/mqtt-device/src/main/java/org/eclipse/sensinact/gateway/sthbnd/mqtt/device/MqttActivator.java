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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.device;


import org.eclipse.sensinact.gateway.generic.GenericActivator;


/**
 * sensiNact bundle activator
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public abstract class MqttActivator extends GenericActivator {

    @Override
    public MqttProtocolStackEndpoint getEndPoint() {
        return new MqttProtocolStackEndpoint(mediator);
    }

    public Class getPacketClass(){
        return MqttPacket.class;
    }
}
