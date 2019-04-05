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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.device;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Service;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttBroker;

import java.util.List;

/**
 * POJO that is base to express the mapping between one MQTT topic and a internal device.
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public interface MqttProvider {
    String getName();

    Boolean isDiscoveryOnFirstMessage();

    List<Service> getServices();

    MqttBroker getBroker();
}
