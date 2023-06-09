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
package org.eclipse.sensinact.prototype.command.impl;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.osgi.util.promise.Promise;

/**
 *
 */
public interface ResourcePushHandler {

    /**
     * Pushes a value
     *
     * @param <T>         Expected resource value type
     * @param model       Model name
     * @param provider    Provider name
     * @param service     Service name
     * @param resource    Resource name
     * @param clazz       Expected resource value type
     * @param cachedValue Current cached value (value and timestamp can be null)
     * @param newValue    Pushed value
     * @return The promise of a new value (can't be null)
     */
    <T> Promise<TimedValue<T>> pushValue(String model, String provider, String service, String resource, Class<T> clazz,
            TimedValue<T> cachedValue, TimedValue<T> newValue);
}
