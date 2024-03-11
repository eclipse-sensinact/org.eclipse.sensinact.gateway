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
import org.eclipse.sensinact.core.session.ProviderDescription;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * @author David Leangen
 */
@Component(service = ProviderCommands.class)
@GogoCommand(
        scope = "sna",
        function = {"provider", "providers"}
    )
public class ProviderCommands {

    @Reference private SensiNactCommandSession session;

    /**
     * List all providers.
     */
    @Descriptor("List all providers")
    public List<ProviderDescription> providers() {
        return session.get().listProviders();
    }

    /**
     * Describe a provider
     *
     * @param provider the ID of the provider
     */
    @Descriptor("Describe a provider")
    public ProviderDescription provider(@Descriptor("the provider ID") String provider) {
        return session.get().describeProvider(provider);
    }
}
