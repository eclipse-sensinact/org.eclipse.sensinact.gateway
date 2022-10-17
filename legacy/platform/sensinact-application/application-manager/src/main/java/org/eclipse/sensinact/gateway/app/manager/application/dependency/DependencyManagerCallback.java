/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.application.dependency;

/**
 * Callback used to indicate if the all the dependencies all a given application were all satisfied
 */
public interface DependencyManagerCallback {
    /**
     * All Dependencies are satisfied
     *
     * @param applicationName
     */
    void ready(String applicationName);

    /**
     * Some Dependencies are NOT satisfied
     *
     * @param applicationName
     */
    void unready(String applicationName);
}
