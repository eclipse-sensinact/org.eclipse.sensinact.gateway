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

import java.util.Map;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Leangen
 */
@Component(service = ActCommands.class)
@GogoCommand(
        scope = "sna",
        function = {"act"}
    )
public class ActCommands {

    @Reference private SensiNactCommandSession session;

    /**
     * ACT on a resource.
     *
     * @param provider   the ID of the provider
     * @param service    the ID of the service
     * @param resource   the ID of the resource
     * @param parameters the parameters to apply
     */
    @Descriptor("ACT on a resource")
    public Object act(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("   the parameters to apply") Map<String, Object> parameters) {

        return session.get().actOnResource(provider, service, resource, parameters);
    }
}
