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
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;

import org.apache.felix.service.command.Descriptor;

import java.security.InvalidKeyException;
import java.util.List;
import java.util.Set;

public class DeviceCommands {

    private CommandServiceMediator mediator;

    private final String _line;
    private final String _tab;

    public DeviceCommands(CommandServiceMediator mediator) throws DataStoreException, InvalidKeyException {
        this.mediator = mediator;
        this._line = ShellUtils.lineSeparator(100);
        this._tab = ShellUtils.tabSeparator(3);
    }

    /**
     * Display the existing sensiNact service providers instances
     */
    @Descriptor("display the existing sensiNact service providers instances")
    public void devices() {
        StringBuilder buffer = new StringBuilder();

        Set<ServiceProvider> devices = mediator.getSession().serviceProviders();

        if (devices != null) {
            buffer.append("\nsensiNact service provider instances:\n" + _line + "\n");

            for (ServiceProvider device : devices) {
                buffer.append(_tab + "-- ID: " + device.getName() + "\n");
            }

            buffer.append(_line + "\n");
        } else {
            buffer.append("No existing sensiNact service provider instances");
        }

        System.out.println(buffer.toString());
    }

    /**
     * Display the description of a specific sensiNact service provider instance
     * @param serviceProviderID the ID of the service provider
     */
    @Descriptor("display the description of a specific sensiNact service provider instance")
    public void device(@Descriptor("the device ID") String serviceProviderID) {
        StringBuilder buffer = new StringBuilder();
        ServiceProvider device = mediator.getSession().serviceProvider(serviceProviderID);

        if (device != null) {
            buffer.append("\n" + _line);
            buffer.append("\n" + _tab + "ID: " + device.getName());

            List<Service> services = device.getServices();
            
            if (services != null && !services.isEmpty()) {
                buffer.append("\n" + _tab + "Provided Services:");

                for (Service service : services) {
                    if (service != null) {
                        buffer.append("\n" + _tab + "   -- ID: " + service.getName());
                    }
                }
            }

            buffer.append("\n" + _line + "\n");
        } else {
            buffer.append("sensiNact service provider instance /" + serviceProviderID + " not found");
        }

        System.out.println(buffer.toString());
    }
}
