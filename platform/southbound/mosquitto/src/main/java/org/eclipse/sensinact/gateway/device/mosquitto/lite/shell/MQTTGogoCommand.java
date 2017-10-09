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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.shell;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTPropertyFileConfig;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.service.command.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Component(immediate = true)
@Instantiate
@Provides
/**
 * MQTT command shell
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Nascimento</a>
 */
public class MQTTGogoCommand {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTGogoCommand.class);

    @ServiceProperty(name = "osgi.command.scope", value = "mosquitto")
    private String scope;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    private String[] function = new String[]{"clients"};

    @Requires (optional = true,specification = MQTTPropertyFileConfig.class)
    List<MQTTPropertyFileConfig> dums;

    @Descriptor(value = "")
    public void clients(@Descriptor("") String... parameters) throws IOException {

        System.out.println("Not implemented");

    }


}
