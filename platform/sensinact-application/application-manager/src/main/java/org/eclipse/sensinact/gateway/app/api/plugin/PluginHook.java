/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.api.plugin;

/**
 * Hook actions are performed after the application tree has been completely browsed.
 * It enables to avoid firing actions while the tree is not be completely browsed.
 *
 * @author Remi Druilhe
 */
public interface PluginHook {
    /**
     * Fire the hooked action
     *
     * @throws Exception when any problems occur
     */
    void fireHook() throws Exception;
}
