/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

/**
 * Configuration applying on an {@link HttpTask}s
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface HttpTaskConfigurator {
    /**
     * Configures the {@link HttpTask} passed as parameter
     *
     * @param <T>
     * @param task the {@link HttpTask} to be configured
     * @return
     */
    <T extends HttpTask<?, ?>> void configure(T task) throws Exception;
}
