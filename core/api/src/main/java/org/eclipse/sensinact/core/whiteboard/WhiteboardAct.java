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

import java.util.Map;

import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Whiteboard service to handle resource act operations
 */
public interface WhiteboardAct<T> extends WhiteboardHandler {

    /**
     * Whiteboard is called to get the resource value as a promise
     *
     * @param promiseFactory  Promise factory
     * @param modelPackageUri Package URI of the provider model
     * @param model           Provider model name
     * @param provider        Provider name
     * @param service         Service name
     * @param resource        Resource name
     * @param arguments       Map of given arguments
     * @return A promise, created with the promise factory
     */
    Promise<T> act(PromiseFactory promiseFactory, String modelPackageUri, String model, String provider,
            String service, String resource, Map<String, Object> arguments);
}
