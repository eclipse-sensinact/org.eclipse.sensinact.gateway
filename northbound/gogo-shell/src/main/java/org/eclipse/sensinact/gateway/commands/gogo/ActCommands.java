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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.converter.Converters;

/**
 * @author David Leangen
 */
@Component(service = ActCommands.class)
@GogoCommand(scope = "sna", function = { "act" })
public class ActCommands {

    @Reference
    private SensiNactCommandSession session;

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
            @Descriptor("the parameters to apply") String... parameters) throws Exception {

        Map<String, Class<?>> paramTypes = session.get().describeResource(provider, service,
                resource).actMethodArgumentsTypes.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Map<String, Object> params = new HashMap<>();
        for (String strEntry : parameters) {
            final int idx = strEntry.indexOf("=");
            if (idx == -1) {
                throw new Exception("Parameters must be given as name=value");
            }

            final String argName = strEntry.substring(0, idx);
            final String strValue = strEntry.substring(idx + 1);

            if (!paramTypes.containsKey(argName)) {
                throw new Exception("Unknown argument " + argName);
            } else if (strValue.equals("null")) {
                params.put(argName, null);
            } else {
                params.put(argName, Converters.standardConverter().convert(strValue).to(paramTypes.get(argName)));
            }
        }

        return session.get().actOnResource(provider, service, resource, params);
    }
}
