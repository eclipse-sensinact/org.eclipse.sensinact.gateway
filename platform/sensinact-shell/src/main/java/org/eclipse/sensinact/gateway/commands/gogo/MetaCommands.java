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
import java.util.stream.Collectors;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Leangen
 */
@Component(service = MetaCommands.class)
@GogoCommand(
        scope = "sna",
        function = {"meta"}
    )
public class MetaCommands {

    @Reference private SensiNactCommandSession session;

    /**
     * List all the metadata properties for a resource.
     *
     * @param provider  the ID of the provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     */
    @Descriptor("List all the metadata properties for a resource")
    public String meta(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource) {

        final Map<String, Object> description = session.get().getResourceMetadata(provider, service, resource);
        if (description == null)
            return "<NULL>";

        final String data = description.entrySet().stream()
                .map(e -> "  " + e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining("\n  "));

        return "\n"
                + "Resource: " + provider + "/" + service + "/" + resource + "\n"
                + "\n"
                + "  Metadata\n"
                + "  --------\n"
                + "  " + data + "\n";
    }

    /**
     * Get the value of resource metadata.
     *
     * @param provider  the ID of the provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param metadata  the name of the metadata property to get
     */
    @Descriptor("Get the value of a resource's metadata")
    public String meta(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("the metadata property to get") String metadata ) {

        final Map<String, Object> data = session.get().getResourceMetadataValue(provider, service, resource, metadata);
        if (data == null)
            return "<NULL>";

        return data.entrySet().stream()
                .map(e -> e.getValue().toString())
                .collect(Collectors.joining(", "));
    }

    /**
     * Set the metadata value of a resource.
     *
     * @param provider  the ID of the provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param metadata  the name of the metadata property to set
     * @param value     the value to set
     */
    @Descriptor("Set the metadata value of a resource")
    public String meta(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("the metadata property") String metadata,
            @Descriptor("the value to set") Object value) {

        session.get().setResourceMetadata(provider, service, resource, metadata, value);
        return provider + "/" + service + "/" + resource + " → " + metadata + " = " + value;
    }
}
