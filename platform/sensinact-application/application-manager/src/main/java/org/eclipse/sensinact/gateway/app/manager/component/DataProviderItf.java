/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.manager.component;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;

import java.util.Set;

/**
 * @author Rémi Druilhe
 */
public interface DataProviderItf {
    /**
     * Get the URI of this data provider
     *
     * @return the URI
     */
    String getUri();

    /**
     * Get the last data
     *
     * @return the data
     */
    DataItf getData();

    /**
     * Add a listener to be notified when a {@link Data} changes
     *
     * @param listener    the listener of the event
     * @param constraints the constraints on the event
     */
    void addListener(DataListenerItf listener, Set<Constraint> constraints);

    /**
     * Remove a listener
     *
     * @param listener the listener of the event
     */
    void removeListener(DataListenerItf listener);
}
