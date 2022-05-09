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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;

public class DefaultHttpChainedTaskProcessingContextFactory implements HttpChainedTaskProcessingContextFactory {
    private Mediator mediator;

    /**
     * @param mediator
     */
    public DefaultHttpChainedTaskProcessingContextFactory(Mediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public <CHAINED extends HttpChainedTask<?>> HttpTaskProcessingContext newInstance(HttpTaskConfigurator httpTaskConfigurator, String endpointId, HttpChainedTasks<?, CHAINED> tasks, CHAINED task) {
        return new DefaultHttpChainedTaskProcessingContext(this.mediator, httpTaskConfigurator, endpointId, tasks, task);
    }
}
