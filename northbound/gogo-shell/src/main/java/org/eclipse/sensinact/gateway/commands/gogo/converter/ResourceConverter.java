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
package org.eclipse.sensinact.gateway.commands.gogo.converter;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.felix.service.command.Converter;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.osgi.service.component.annotations.Component;

/**
 * @author David Leangen
 */
@Component
public class ResourceConverter
    implements Converter
{
    @Override
    public Object convert(Class<?> desiredType, Object in)
            throws Exception
    {
        return null;
    }

    @Override
    public CharSequence format(Object target, int level, Converter escape)
            throws Exception
    {
        if (target instanceof ResourceDescription) {
            final ResourceDescription description = (ResourceDescription) target;

            if (level == LINE) {
                return description.resource;
            }

            if (level == INSPECT) {

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

        return null;
    }
}
