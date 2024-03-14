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
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Leangen
 */
@Component(service = PushCommands.class)
@GogoCommand(
        scope = "sna",
        function = {"push"}
    )
public class PushCommands {

    @Reference private SensiNactCommandSession session;

    /**
     * Push a String value of a sesiNact resource. The command will block until a response is received.
     *
     * @param provider  the ID of the service provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param value     the String value to set
     */
    @Descriptor("Push a String value of a sesiNact resource. The command will block until a response is received.")
    public Object push(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("the value to set") String value) throws Exception {

        push(provider, service, resource, value, String.class);
        return "OK!";
    }

    /**
     * Push an int value of a sesiNact resource. The command will block until a response is received.
     *
     * @param provider  the ID of the service provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param value     the int value to set
     */
    @Descriptor("Push an int value of a sesiNact resource. The command will block until a response is received.")
    public Object push(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("   the value to set") int value) throws Exception {

        push(provider, service, resource, value, Integer.class);
        return "OK!";
    }

    /**
     * Push an value of a sesiNact resource of the provided type. The command will block until a response is received.
     *
     * @param provider  the ID of the service provider
     * @param service   the ID of the service
     * @param resource  the ID of the resource
     * @param value     the value to set
     * @param type     the type (Class) of the value to set
     */
    @Descriptor("Push an value of a sesiNact resource of the provided type. The command will block until a response is received.")
    public <T>Object push(
            @Descriptor("the provider ID") String provider,
            @Descriptor("the service ID") String service,
            @Descriptor("the resource ID") String resource,
            @Descriptor("the value to set") T value,
            @Descriptor(" type type of the value to set") Class<T> type) throws Exception {

        final GenericDto dto = new GenericDto();
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = value;
        dto.type = type;
        session.push(dto).getValue();
        return "OK!";
    }
}
