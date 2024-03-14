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

@org.osgi.annotation.bundle.Capability(
        namespace = "org.apache.felix.gogo",
        name = "command.implementation",
        version = "1.0.0"
)
@org.osgi.annotation.bundle.Requirement(
        effective = "active",
        namespace = "org.apache.felix.gogo",
        name = "runtime.implementation",
        version = "1.0.0"
)
/**
 * Declare Requirements and Capabilities using OSGi annotations.
 *
 * @author David Leangen
 */
public class SensiNactCommands {

}
