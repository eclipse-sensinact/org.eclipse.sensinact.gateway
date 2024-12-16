/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - Initial contribution
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.api.constants;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.DataSchema;

public class Utils {

    /**
     * Converts a Web of Things data schema into a matching sensiNact resource type
     *
     * @param wotType Web of Thing data schema
     * @return A Java class
     */
    public static Class<?> classFromType(DataSchema wotType) {
        if (wotType == null) {
            return classFromType((String) null);
        } else {
            return classFromType(wotType.type);
        }
    }

    /**
     * Converts a Web of Things type into a sensiNact resource type
     *
     * @param wotType Web of Thing type name
     * @return A Java class
     */
    public static Class<?> classFromType(final String wotType) {
        if (wotType == null) {
            return Object.class;
        }

        switch (wotType) {
        case "array":
            return List.class;
        case "boolean":
            return Boolean.class;
        case "number":
            return Double.class;
        case "integer":
            return Long.class;
        case "string":
            return String.class;
        case "object":
            return Map.class;
        case "null":
        default:
            return Object.class;
        }
    }

    /**
     * Ensures the given name can be used as a name in sensiNact (provider, service
     * or resource)
     */
    public static String makeWoTSanitizedName(final String name) {
        return String.format("wot_%s", name).replaceAll("[^-_A-Za-z0-9]", "_");
    }
}
