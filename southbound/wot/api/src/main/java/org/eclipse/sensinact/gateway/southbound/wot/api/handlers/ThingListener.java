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

/**
 * Listener called by the {@link ThingManager} when a provider based on Web of
 * Things Thing description has been added or removed
 */
public interface ThingListener {

    /**
     * Notified when a new Web of Things-based provider has been registered
     *
     * @param descriptor Description of the sensiNact provider matching a Thing
     *                   description
     */
    void thingRegistered(SensinactThingDescriptor descriptor);

    /**
     * Notified when an existing Web of Things-based provider has been removed
     *
     * @param descriptor Description of the sensiNact provider matching a Thing
     *                   description
     */
    void thingUnregistered(SensinactThingDescriptor descriptor);
}
