/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.light.internal;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.generic.annotation.TaskInject;

/**
 *
 */
@TaskExecution
public class LightInvoker {
    @TaskInject
    LightConfig config;

    @TaskCommand(method = CommandType.ACT, target = "/light/switch/dim")
    public void dim(String uri, int brightness) {
        config.setBrightness(brightness);
    }

    @TaskCommand(method = CommandType.ACT, target = "/light/switch/turn_on")
    public void on(String uri) {
        config.turnOn();
    }

    @TaskCommand(method = CommandType.ACT, target = "/light/switch/turn_off")
    public void off(String uri) {
        config.turnOff();
    }
}
