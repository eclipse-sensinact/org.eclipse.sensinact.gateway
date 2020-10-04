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
package org.eclipse.sensinact.gateway.simulated.billboard.internal;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.generic.annotation.TaskInject;

@TaskExecution
public class BillboardInvoker {
    @TaskInject
    BillboardConfig config;

    @TaskCommand(method = Task.CommandType.ACT, target = "/billboard/screen/display")
    public void display(String uri, String message) {
        config.setMessage(message);
    }
}
