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
import org.eclipse.sensinact.core.twin.TimedValue;
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

    public static class MetaDTO {
        public String provider;
        public String service;
        public String resource;
        public Map<String, Object> metadata;
    }

    @Reference private SensiNactCommandSession session;

    /**
     * List all the metadata properties for a resource.
     *
     * @param provider  the ID of the provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     */
    @Descriptor("List all the metadata properties for a resource")
    public MetaDTO meta(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource) {

        final Map<String, Object> metadata = session.get().getResourceMetadata(provider, service, resource);
        final MetaDTO dto = new MetaDTO();
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.metadata = metadata;
        return dto;
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

        final TimedValue<Object> data = session.get().getResourceMetadataValue(provider, service, resource, metadata);
        if (data == null)
            return "<NULL>";

        return data.toString();
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
