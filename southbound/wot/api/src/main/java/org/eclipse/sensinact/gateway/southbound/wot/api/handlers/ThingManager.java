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

/**
 * Service to call to register a Thing as a sensiNact provider based on its
 * description
 */
public interface ThingManager {

    /**
     * Registers a thing based on the given description
     *
     * @param thingDescription A thing description
     * @return The name of the provider showing the Thing
     */
    String registerThing(final Thing thingDescription);

    /**
     * Unregister a provider created with {@link #registerThing(Thing)}
     *
     * @param providerName Name of the provider to unregister
     */
    void unregisterThing(final String providerName);
}
