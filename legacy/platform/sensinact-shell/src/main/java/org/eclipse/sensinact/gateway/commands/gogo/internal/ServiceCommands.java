/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.sensinact.gateway.commands.gogo.internal.shell.ShellAccess;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = ServiceCommands.class)
@GogoCommand(
		scope = "sna", 
		function = {"service", "services"}
	)
public class ServiceCommands {
   
	@Reference
	private CommandComponent component;

    /**
     * Display the existing sensiNact service instances
     *
     * @param serviceProviderID the ID of the service provider
     */
    @Descriptor("display the existing sensiNact service instances")
    public void services(@Descriptor("the service provider ID") String serviceProviderID) {
        ShellAccess.proceed(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, null, null, true)).build());
    }

    /**
     * Display the description of a specific sensiNact service instance
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     */
    @Descriptor("display the description of a specific sensiNact service instance")
    public void service(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID) {
        ShellAccess.proceed(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, null, false)).build());
    }
}
