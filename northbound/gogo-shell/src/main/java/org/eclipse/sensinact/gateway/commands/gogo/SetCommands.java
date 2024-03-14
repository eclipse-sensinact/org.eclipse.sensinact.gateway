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

import java.time.Instant;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Leangen
 */
@Component(service = SetCommands.class)
@GogoCommand(
        scope = "sna",
        function = {"set"}
    )
public class SetCommands {

    @Reference private SensiNactCommandSession session;

    /**
     * Set the value of a sesiNact resource.
     *
     * @param provider  the ID of the service provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param value     the value to set
     */
    @Descriptor("Set the value of a resource using the current time.")
    public String set(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("the value to set") Object value) {

        session.get().setResourceValue(provider, service, resource, value);
        return provider + "/" + service + "/" + resource + " = " + value;
    }

    /**
     * Set the value of a sesiNact resource.
     *
     * @param provider  the ID of the service provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param value     the value to set
     * @param instant   the timestamp value to set, which must be later than the current time for the resource
     */
    @Descriptor("Set the value of a resource using the timestamp provided.")
    public String set(
            @Descriptor(" the provider ID") String provider,
            @Descriptor(" the service ID") String service,
            @Descriptor(" the resource ID") String resource,
            @Descriptor(" the value to set") Object value,
            @Descriptor("the timestamp to use for this operation, "
                    + "which must be later than the existing time for this resource") Instant instant) {

        session.get().setResourceValue(provider, service, resource, resource, instant);
        return provider + "/" + service + "/" + resource + " = " + value + " @" + instant;
    }
}
