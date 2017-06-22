/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */

package org.eclipse.sensinact.gateway.app.manager.component;

/**
 * Interface to implement to be notified when a component output/property changes.
 *
 * @author Remi Druilhe
 */
public interface DataListenerItf {

    /**
     * Send a new data from a component output/property to the listeners
     * @param event the event
     */
    void eventNotification(Event event);
}
