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
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface RecurrentHttpTaskConfigurator extends HttpTaskBuilder {
    /**
     * @return the period
     */
    long getPeriod();

    /**
     * @return the delay
     */
    long getDelay();

    /**
     * @return the timeout
     */
    long getTimeout();

    /**
     * @return the taskType
     */
    Class<? extends HttpTask> getTaskType();
}
