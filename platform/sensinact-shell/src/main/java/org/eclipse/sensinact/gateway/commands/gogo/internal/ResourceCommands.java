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
import org.json.JSONArray;
import org.json.JSONObject;

public class ResourceCommands {

    private CommandServiceMediator mediator;

    private final String _line;
    private final String _tab;

    public ResourceCommands(CommandServiceMediator mediator) {
        this.mediator = mediator;
        this._line = ShellUtils.lineSeparator(100);
        this._tab = ShellUtils.tabSeparator(3);
    }

    /**
     * Display the existing sensiNact service instances
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     */
    @Descriptor("display the existing sensiNact service instances")
    public void resources(@Descriptor("the service provider ID") String serviceProviderID,
                          @Descriptor("the service ID") String serviceID) {
        StringBuilder buffer = new StringBuilder();
        Service service = mediator.getSession().service(serviceProviderID, serviceID);

        if (service != null) {
            buffer.append("\nsensiNact resource instances for " + service.getPath() +":\n");
            buffer.append(_line + "\n");

            for (Resource resource : service.getResources()) {
                buffer.append(_tab + "-- ID: " + resource.getName() + "\n");
            }

            buffer.append(_line + "\n");
        } else {
            buffer.append("No existing sensiNact resource instances for service provider " +
                    "/" + serviceProviderID + "/" + serviceID);
        }

        System.out.println(buffer.toString());
    }

    /**
     * Get the description of a specific resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     */
    @Descriptor("get the description of a specific resource of a sensiNact service")
    public void resource(@Descriptor("the service provider ID") String serviceProviderID,
                         @Descriptor("the service ID") String serviceID,
                         @Descriptor("the resource IS") String resourceID) {
        StringBuilder buffer = new StringBuilder();
        Resource resource = mediator.getSession().resource(serviceProviderID, serviceID, resourceID);

        if (resource != null) {
            buffer.append("\n" + _line);
            buffer.append("\n" + _tab + "ID: " + resource.getName());
            buffer.append("\n" + _tab + "Attributes: ");

            JSONArray attributes = new JSONObject(resource.getDescription().getDescription()).getJSONArray("attributes");

            for(int i = 0; i < attributes.length(); i++) {
                buffer.append("\n" + _tab + "   -- " + attributes.getJSONObject(i).get("name"));
            }

            buffer.append("\n" + _line + "\n");
        } else {
            buffer.append("sensiNact resource instance /" + serviceProviderID +
                    "/" + serviceID + "/" + resourceID + " not found");
        }

        System.out.println(buffer.toString());
    }
}
