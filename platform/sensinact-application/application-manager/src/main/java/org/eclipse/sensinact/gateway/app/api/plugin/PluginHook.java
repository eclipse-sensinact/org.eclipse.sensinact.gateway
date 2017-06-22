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
     * @throws Exception when any problems occur
     */
    void fireHook() throws Exception;
}
