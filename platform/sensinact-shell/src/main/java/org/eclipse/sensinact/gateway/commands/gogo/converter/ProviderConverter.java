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
import org.eclipse.sensinact.core.session.ProviderDescription;
import org.osgi.service.component.annotations.Component;

/**
 * @author David Leangen
 */
@Component
public class ProviderConverter
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
        if (target instanceof ProviderDescription) {
            final ProviderDescription description = (ProviderDescription) target;

            if (level == LINE) {
                return description.provider;
            }

            if (level == INSPECT) {

                final String services = description.services.stream()
                        .collect(Collectors.joining("\n  "));

                return "\n"
                        + "Provider: " + description.provider + "\n"
                        + "\n"
                        + "  Services\n"
                        + "  --------\n"
                        + "  " + services + "\n";
            }
        }

        return null;
    }
}
