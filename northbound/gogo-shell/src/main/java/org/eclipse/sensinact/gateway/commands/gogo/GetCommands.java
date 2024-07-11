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

import java.util.Objects;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Leangen
 */
@Component(service = GetCommands.class)
@GogoCommand(scope = "sna", function = { "get" })
public class GetCommands {

    @Reference
    private SensiNactCommandSession session;

    /**
     * Get the String value of a resource.
     *
     * @param provider  the ID of the provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     */
    @Descriptor("Get the value of a resource.\n\n   This variant assumes that the type of the value is Object.\n")
    public String get(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource) {
        final ResourceType<Object> type = new ResourceType<>();
        type.type = Object.class;
        return Objects.toString(get(provider, service, resource, type));
    }

    /**
     * Get the value of the resource for a well-known type.
     *
     * @param provider  the ID of the provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param type      type shorthand type of the resource to get (String, Integer, int)
     */
    @Descriptor("Get the value of a resource.\n\n   This variant accepts a simplified type for convenience.\n")
    public <T>T get(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("the simplified type of the resource value to get (one of: String, Integer, int)") ResourceType<T> type) {
        return session.get().getResourceValue(provider, service, resource, type.type);
    }

    /**
     * Get the value of the resource for a fully-qualified type.
     *
     * @param provider  the ID of the provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param type      type fqn of the type of the resource to get
     */
    @Descriptor("Get the value of a resource.\n\n   This variant requires the FQN of the value type (i.e. java.lang.String).\n")
    public <T>T get(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("the type of the resource value to get") Class<T> type) {
        return session.get().getResourceValue(provider, service, resource, type);
    }
}
