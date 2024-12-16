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

package org.eclipse.sensinact.gateway.southbound.wot.api.handlers;

import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;

public class SensinactThingDescriptor {

    /**
     * Parsed thing
     */
    public Thing thing;

    /**
     * SensiNact model package URI
     */
    public String modelPackageUri;

    /**
     * SensiNact model name
     */
    public String modelName;

    /**
     * SensiNact provider ID
     */
    public String providerId;
}
