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
