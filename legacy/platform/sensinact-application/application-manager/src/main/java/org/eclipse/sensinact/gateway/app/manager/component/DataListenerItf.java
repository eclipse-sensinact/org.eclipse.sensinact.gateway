/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.component;

/**
 * Interface to implement to be notified when a component output/property changes.
 *
 * @author Remi Druilhe
 */
public interface DataListenerItf {
    /**
     * Send a new data from a component output/property to the listeners
     *
     * @param event the event
     */
    void eventNotification(Event event);
}
