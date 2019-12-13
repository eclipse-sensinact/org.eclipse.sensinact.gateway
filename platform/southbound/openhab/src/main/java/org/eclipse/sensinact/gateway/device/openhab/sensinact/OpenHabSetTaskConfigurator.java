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
package org.eclipse.sensinact.gateway.device.openhab.sensinact;

import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

public class OpenHabSetTaskConfigurator implements HttpTaskConfigurator {
    @Override
    public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
        final Object[] parameters = task.getParameters();
        final String content = parameters[1].toString();
        task.setContent(content);
    }
}