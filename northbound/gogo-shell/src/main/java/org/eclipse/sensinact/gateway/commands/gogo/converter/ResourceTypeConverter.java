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

import org.apache.felix.service.command.Converter;
import org.eclipse.sensinact.gateway.commands.gogo.ResourceType;
import org.osgi.service.component.annotations.Component;

/**
 * @author David Leangen
 */
@Component
public class ResourceTypeConverter
    implements Converter
{
    @Override
    public Object convert(Class<?> desiredType, Object in)
            throws Exception
    {
        if( desiredType == ResourceType.class && in instanceof CharSequence ) {
            final String input = in.toString().toLowerCase();

            if ("string".equals(input)) {
                final ResourceType<String> type = new ResourceType<>();
                type.type = String.class;
                return type;
            }

            if ("integer".equals(input) || "int".equals(input)) {
                final ResourceType<Integer> type = new ResourceType<>();
                type.type = Integer.class;
                return type;
            }
        }

        return null;
    }

    @Override
    public CharSequence format(Object target, int level, Converter escape)
            throws Exception
    {
        return null;
    }
}
