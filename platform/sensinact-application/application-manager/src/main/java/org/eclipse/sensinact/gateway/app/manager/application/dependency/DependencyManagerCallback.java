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
