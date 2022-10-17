/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.bundle;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.annotation.TaskCommand;
import org.eclipse.sensinact.gateway.generic.annotation.TaskExecution;

/**
 *
 */
@TaskExecution(profile = {"testProfile"})
public class Invoker2 {
    @TaskCommand(method = Task.CommandType.SERVICES_ENUMERATION)
    Object services(String uri) {
        return new String[]{"measureTest", "serviceTest"};
    }
}
