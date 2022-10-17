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
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

public class DefaultHttpTaskProcessingContextFactory implements HttpTaskProcessingContextFactory {
    protected Mediator mediator;

    /**
     * @param mediator
     */
    public DefaultHttpTaskProcessingContextFactory(Mediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public HttpTaskProcessingContext newInstance(HttpTaskConfigurator httpTaskConfigurator, String endpointId, HttpTask<?, ?> task) {
        return new DefaultHttpTaskProcessingContext(this.mediator, httpTaskConfigurator, endpointId, task);
    }
}
