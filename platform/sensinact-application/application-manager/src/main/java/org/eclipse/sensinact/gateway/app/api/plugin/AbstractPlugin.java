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
package org.eclipse.sensinact.gateway.app.api.plugin;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppFunction;

public abstract class AbstractPlugin implements PluginInstaller {
    /**
     * @see PluginInstaller#getFunction(AppFunction)
     */
    public abstract AbstractFunction<?> getFunction(AppFunction function);
}
