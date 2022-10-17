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
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonObject;

/**
 *
 */
@TaskExecution(profile = {"measurement"})
public class MeasurementInvoker {
    @TaskCommand(target = "/*/*/threshold", method = Task.CommandType.GET)
    Object get(String uri, String attributeName) {
        return 0.2f;
    }

    @TaskCommand(target = "/*/*/activate", method = Task.CommandType.ACT)
    JsonObject act(Object... parameters) {
    	return JsonProviderFactory.getProvider().createObjectBuilder()
    			.add("value", parameters != null ? ((Float) parameters[1]).floatValue() : 0.0f)
    			.build();
    }
}
