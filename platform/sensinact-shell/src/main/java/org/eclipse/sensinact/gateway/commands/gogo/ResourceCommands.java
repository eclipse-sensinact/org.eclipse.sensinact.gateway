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

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.sensinact.core.session.ResourceDescription;
import org.eclipse.sensinact.core.session.ServiceDescription;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Leangen
 */
@Component(service = ResourceCommands.class)
@GogoCommand(
        scope = "sna",
        function = {"resource", "resources"}
    )
public class ResourceCommands {

    @Reference private SensiNactCommandSession session;

    /**
     * List all resources for a given service.
     *
     * @param provider the ID of the provider
     * @param service  the ID of the service
     */
    @Descriptor("List all resources for a given service")
    public String resources(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service) {
        final ServiceDescription description = session.get().describeService(provider, service);
        if (description == null)
            return "<NULL>";

        final String resources = description.resources.stream()
                .collect(Collectors.joining("\n  "));

        return "\n"
                + "Service: " + description.provider + "\n"
                + "\n"
                + "  Resources\n"
                + "  ---------\n"
                + "  " + resources + "\n";
    }

    /**
     * Describe a resource.
     *
     * @param provider the ID of the provider
     * @param service  the ID of the service
     * @param resource the ID of the resource
     */
    @Descriptor("Describe a resource")
    public String resource(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource) {
        final ResourceDescription description = session.get().describeResource(provider, service, resource);
        if (description == null)
            return "<NULL>";

        final List<Entry<String, Class<?>>> actTypes = description.actMethodArgumentsTypes;
        final String params = actTypes == null ? "<NONE>" : actTypes.stream()
                .map(e -> e.getKey() + " (" + e.getValue().toString() + ")")
                .collect(Collectors.joining(", "));

        return "\n"
        + "Resource: " + description.provider + "/" + description.service + "/" + description.resource + "\n"
        + "\n"
        + "  Resource Type: " + description.resourceType + "\n"
        + "  Value Type:    " + description.valueType + "\n"
        + "  Content Type:  " + description.contentType + "\n"
        + "  ACT params:    " + params + "\n";
    }
}
