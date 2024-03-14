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
import org.osgi.service.component.annotations.Component;

/**
 * @author David Leangen
 */
@Component(service = DescribeCommands.class)
@GogoCommand(
        scope = "sna",
        function = {"describe"}
    )
public class DescribeCommands {

    /**
     * Direct the user to the available commands to describe sensiNact models.
     *
     * @param provider  the ID of the service provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     */
    @Descriptor("Not a command. Instead, see: provider, service, resource.")
    public String describe() {

        return "\n"
                + "To describe a sensiNact model, please refer to these commands:\n\n"
                + "   provider:     describes a sensiNact provider\n"
                + "   service:      describes a sensiNact service\n"
                + "   resource:     describes a sensiNact resource\n"
                + "\n";
    }

    /**
     * Direct the user to the available commands to describe sensiNact models.
     *
     * @param provider  the ID of the service provider
     */
    @Descriptor("Not a command. Instead, see: provider")
    public String describe(String provider) {

        return "\n"
                + "To describe a sensiNact provider, please use: provider " + provider + "\n\n";
    }

    /**
     * Direct the user to the available commands to describe sensiNact models.
     *
     * @param provider  the ID of the service provider
     * @param service   the ID of the service
     */
    @Descriptor("Not a command. Instead, see: service")
    public String describe(String provider, String service) {

        return "\n"
                + "To describe a sensiNact service, please use: service " + provider + " " + service + "\n\n";
    }

    /**
     * Direct the user to the available commands to describe sensiNact models.
     *
     * @param provider  the ID of the service provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     */
    @Descriptor("Not a command. Instead, see: resource")
    public String describe(String provider, String service, String resource) {

        return "\n"
                + "To describe a sensiNact resource, please use: resource " + provider + " " + service + " " + resource + "\n\n";
    }
}
