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

import java.util.stream.Collectors;

import org.apache.felix.service.command.Converter;
import org.eclipse.sensinact.gateway.commands.gogo.MetaCommands.MetaDTO;
import org.osgi.service.component.annotations.Component;

/**
 * @author David Leangen
 */
@Component
public class MetadataConverter
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
        if (target instanceof MetaDTO) {
            final MetaDTO dto = (MetaDTO) target;

            if (level == INSPECT) {

                if (dto.metadata == null)
                    return "<EMPTY>";

                final String data = dto.metadata.entrySet().stream()
                        .map(e -> "  " + e.getKey() + " = " + e.getValue())
                        .collect(Collectors.joining("\n  "));

                return "\n"
                        + "Resource: " + dto.provider + "/" + dto.service + "/" + dto.resource + "\n"
                        + "\n"
                        + "  Metadata\n"
                        + "  --------\n"
                        + "  " + data + "\n";
            }
        }

        return null;
    }
}
