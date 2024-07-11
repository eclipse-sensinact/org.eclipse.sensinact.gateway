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

import java.util.Map;

import org.apache.felix.service.command.Converter;
import org.eclipse.sensinact.gateway.commands.gogo.ResourceType;
import org.osgi.service.component.annotations.Component;

/**
 * @author David Leangen
 */
@Component
public class ResourceTypeConverter implements Converter {
    @Override
    public Object convert(Class<?> desiredType, Object in) throws Exception {
        if (desiredType == ResourceType.class && in instanceof CharSequence) {
            final String input = in.toString().toLowerCase();

            switch (input) {
            case "string": {
                final ResourceType<String> type = new ResourceType<>();
                type.type = String.class;
                return type;
            }

            case "int":
            case "integer": {
                final ResourceType<Integer> type = new ResourceType<>();
                type.type = Integer.class;
                return type;
            }

            case "long": {
                final ResourceType<Long> type = new ResourceType<>();
                type.type = Long.class;
                return type;
            }

            case "float": {
                final ResourceType<Float> type = new ResourceType<>();
                type.type = Float.class;
                return type;
            }

            case "double": {
                final ResourceType<Double> type = new ResourceType<>();
                type.type = Double.class;
                return type;
            }

            case "hashmap":
            case "map": {
                @SuppressWarnings("rawtypes")
                final ResourceType<Map> type = new ResourceType<>();
                type.type = Map.class;
                return type;
            }

            case "object":
            default: {
                final ResourceType<Object> type = new ResourceType<>();
                type.type = Object.class;
                return type;
            }
            }
        }

        return null;
    }

    @Override
    public CharSequence format(Object target, int level, Converter escape) throws Exception {
        return null;
    }
}
