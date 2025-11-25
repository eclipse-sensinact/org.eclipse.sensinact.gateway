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
*   Thomas Calmant (Kentyou) - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.whiteboard;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Whiteboard service to handle resource get/set operations
 *
 * @param <T> Resource type
 */
public interface WhiteboardSet<T> extends WhiteboardGet<T> {

    /**
     * Whiteboard is called to set the resource value
     *
     *
     * @param promiseFactory  Promise factory
     * @param modelPackageUri Package URI of the provider model
     * @param model           Provider model name
     * @param provider        Provider name
     * @param service         Service name
     * @param resource        Resource name
     * @param resourceType    Expected type of the result
     * @param cachedValue     Previously cached value
     * @param newValue        New value
     * @return A promise, created with the promise factory
     */
    Promise<TimedValue<?>> pushValue(PromiseFactory promiseFactory, String modelPackageUri, String model,
            String provider, String service, String resource, Class<T> resourceType, TimedValue<?> cachedValue,
            TimedValue<?> newValue);
}
