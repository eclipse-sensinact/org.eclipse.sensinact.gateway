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
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import org.eclipse.sensinact.gateway.commands.gogo.osgi.CommandServiceMediator;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;

import org.apache.felix.service.command.Descriptor;

import java.util.List;

public class ServiceCommands {

    private CommandServiceMediator mediator;

    private final String _line;
    private final String _tab;

    public ServiceCommands(CommandServiceMediator mediator) {
        this.mediator = mediator;
        this._line = ShellUtils.lineSeparator(100);
        this._tab = ShellUtils.tabSeparator(3);
    }

    /**
     * Display the existing sensiNact service instances
     * @param serviceProviderID the ID of the service provider
     */
    @Descriptor("display the existing sensiNact service instances")
    public void services(@Descriptor("the service provider ID") String serviceProviderID) {
        StringBuilder buffer = new StringBuilder();
        List<Service> services = mediator.getSession().getServiceProvider(serviceProviderID).getServices();

        if (!services.isEmpty()) {
            buffer.append("\nsensiNact service instances for service provider /" + serviceProviderID +":\n");
            buffer.append(_line + "\n");

            for (Service service : services) {
                buffer.append(_tab + "-- ID: " + service.getName() + "\n");
            }

            buffer.append(_line + "\n");
        } else {
            buffer.append("No existing sensiNact service instances for service provider /" + serviceProviderID);
        }

        System.out.println(buffer.toString());
    }

    /**
     * Display the description of a specific sensiNact service instance
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     */
    @Descriptor("display the description of a specific sensiNact service instance")
    public void service(@Descriptor("the service provider ID") String serviceProviderID,
                        @Descriptor("the service ID") String serviceID) {
        StringBuilder buffer = new StringBuilder();
        Service service = mediator.getSession().getService(serviceProviderID, serviceID);

        if(service != null) {
            buffer.append("\n" + _line);
            buffer.append("\n" + _tab + "ID: " + service.getName());

            List<Resource> serviceResources = service.getResources();

            if (serviceResources != null && !serviceResources.isEmpty()) {
                buffer.append("\n" + _tab + "Provided Resources: ");
                for (Resource resource : serviceResources) {
                    if (resource != null) {
                        buffer.append("\n" + _tab + "   -- " + resource.getName());
                    }
                }
            }

            buffer.append("\n" + _line + "\n");
        } else {
            buffer.append("sensiNact service instance /" + serviceProviderID + "/" + serviceID + " not found");
        }
        
        System.out.println(buffer.toString());
    }
}
