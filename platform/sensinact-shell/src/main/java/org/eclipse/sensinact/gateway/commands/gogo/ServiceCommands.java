/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.commands.gogo;

import java.util.stream.Collectors;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.sensinact.core.session.ProviderDescription;
import org.eclipse.sensinact.core.session.ServiceDescription;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Leangen
 */
@Component(service = ServiceCommands.class)
@GogoCommand(
        scope = "sna",
        function = {"service", "services"}
    )
public class ServiceCommands {

    @Reference private SensiNactCommandSession session;

    /**
     * List all services for a given provider.
     *
     * @param provider the ID of the provider
     */
    @Descriptor("List all services for a given provider")
    public String services(@Descriptor("the provider ID") String provider) {
        final ProviderDescription description = session.get().describeProvider(provider);
        if (description == null)
            return "<NULL>";

        final String services = description.services.stream()
                .collect(Collectors.joining("\n  "));

        return "\n"
                + "Provider: " + description.provider + "\n"
                + "\n"
                + "  Services\n"
                + "  --------\n"
                + "  " + services + "\n";
    }

    /**
     * Describe a service.
     *
     * @param provider the ID of the provider
     * @param service  the ID of the service
     */
    @Descriptor("Describe a service")
    public String service(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service) {
        final ServiceDescription description = session.get().describeService(provider, service);
        if (description == null)
            return "<NULL>";

        final String services = description.resources.stream()
                .collect(Collectors.joining("\n  "));

        return "\n"
                + "Service: " + description.provider + "\n"
                + "\n"
                + "  Resources\n"
                + "  ---------\n"
                + "  " + services + "\n";
    }
}
