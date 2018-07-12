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

    /**
     * @inheritDoc
     * @see HttpTaskProcessingContextFactory#
     * newInstance(HttpTask)
     */
    @Override
    public HttpTaskProcessingContext newInstance(HttpTaskConfigurator httpTaskConfigurator, String endpointId, HttpTask<?, ?> task) {
        return new DefaultHttpTaskProcessingContext(this.mediator, httpTaskConfigurator, endpointId, task);
    }
}
