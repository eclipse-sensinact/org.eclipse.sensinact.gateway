/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

/**
 *
 */
public class NamingUtils {

    public static String sanitizeName(final String name, final boolean isPath) {
        if (name == null) {
            return null;
        }

        final String rejectedPattern;
        if (isPath) {
            // Allow slash in path
            rejectedPattern = "[^-A-Za-z0-9/]";
        } else {
            rejectedPattern = "[^-A-Za-z0-9]";
        }

        return name.replaceAll(rejectedPattern, "-");
    }
}
