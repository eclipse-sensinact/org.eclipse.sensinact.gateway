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
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.sensinact.gateway.commands.gogo.internal.shell.ShellAccess;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = DeviceCommands.class)
@GogoCommand(
		scope = "sna", 
		function = {"device", "devices"}
	)
public class DeviceCommands {
    
	@Reference
	private CommandComponent component;

    /**
     * Display the existing sensiNact service providers instances
     */
    @Descriptor("display the existing sensiNact service providers instances")
    public void devices() {
        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(null, null, null, true)));
    }

    /**
     * Display the description of a specific sensiNact service provider instance
     *
     * @param serviceProviderID the ID of the service provider
     */
    @Descriptor("display the description of a specific sensiNact service provider instance")
    public void device(@Descriptor("the device ID") String serviceProviderID) {
        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, null, null, false)));
    }
}
