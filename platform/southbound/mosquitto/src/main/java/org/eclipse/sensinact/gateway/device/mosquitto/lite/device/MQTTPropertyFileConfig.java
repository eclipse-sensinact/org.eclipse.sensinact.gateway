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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.device;

/**
 * POJO that is base to express the mapping between one MQTT topic and a sensinact device.
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public interface MQTTPropertyFileConfig {

    String getHost();
    Long getPort();
    String getId();
    String getTopicType();
    String getTopic();
    String getProcessor();
    String getProtocol();
    Float getLongitude();
    Float getLatitude();
    Boolean getDiscoveryOnFirstMessage();
    String getUsername();
    String getPassword();
    void act(String command, Object... parameters);

}
