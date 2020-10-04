/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
