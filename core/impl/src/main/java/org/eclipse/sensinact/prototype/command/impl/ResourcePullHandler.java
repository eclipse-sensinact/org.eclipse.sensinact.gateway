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

import java.util.function.Consumer;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.osgi.util.promise.Promise;

/**
 * Definition of the external get caller
 */
public interface ResourcePullHandler {

    /**
     * Pulls the value from the external getter.
     *
     * @param <T>           Expected resource value type
     * @param model         Model name
     * @param provider      Provider name
     * @param service       Service name
     * @param resource      Resource name
     * @param clazz         Expected resource value type
     * @param cachedValue   Current cached value (value and time stamp can be null)
     * @param gatewayUpdate Method to call in the gateway thread to update the twin
     * @return The promise of the value to be returned by the external getter (can't
     *         be null). This value will be stored in the twin.
     */
    <T> Promise<TimedValue<T>> pullValue(String model, String provider, String service, String resource, Class<T> clazz,
            TimedValue<T> cachedValue, Consumer<TimedValue<T>> gatewayUpdate);
}
