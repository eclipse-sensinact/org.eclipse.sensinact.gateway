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
package org.eclipse.sensinact.gateway.generic.test.tb.bundle;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;
import org.eclipse.sensinact.gateway.generic.annotation.TaskInject;
import org.json.JSONObject;

/**
 *
 */
@TaskExecution
public class Invoker {
    @TaskInject
    Calculator calculator;

    @TaskCommand(method = Task.CommandType.GET)
    Object get(String uri, String attributeName) {
        return "THIS IS THE GET " + calculator.plus(5, 3);
    }

    @TaskCommand(method = Task.CommandType.ACT)
    JSONObject act(Object... parameters) {
        return new JSONObject().put("message", "THIS IS THE ACT");
    }

    @TaskCommand(method = Task.CommandType.SERVICES_ENUMERATION)
    Object services(String uri) {
        return new String[]{};
    }
}
