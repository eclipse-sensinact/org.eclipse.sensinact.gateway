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

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.sensinact.northbound.session.ProviderDescription;
import org.eclipse.sensinact.northbound.session.ServiceDescription;
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
    public ProviderDescription services(@Descriptor("the provider ID") String provider) {
        return session.get().describeProvider(provider);
    }

    /**
     * Describe a service.
     *
     * @param provider the ID of the provider
     * @param service  the ID of the service
     */
    @Descriptor("Describe a service")
    public ServiceDescription service(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service) {
        return session.get().describeService(provider, service);
    }
}
