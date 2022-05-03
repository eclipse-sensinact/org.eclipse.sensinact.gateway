/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
/**
 *
 */
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.osgi.framework.BundleContext;

/**
 * Extended {@link Mediator} dedicated to Http protocol based bridges.
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpMediator extends Mediator implements HttpTaskProcessingContextHandler {
    private HttpTaskProcessingContextHandler taskProcessingContextHandler;
    private HttpTaskProcessingContextFactory taskProcessingContextFactory;
    private HttpChainedTaskProcessingContextFactory chainedTaskProcessingContextFactory;

    /**
     * @param context
     */
    public HttpMediator(BundleContext context) {
        super(context);
    }

    /**
     * Defines the {@link HttpTaskProcessingContextHandler} used by this Mediator
     *
     * @param taskProcessingContextHandler the {@link HttpTaskProcessingContextHandler}
     *                                     to be defined
     */
    public void setTaskProcessingContextHandler(HttpTaskProcessingContextHandler taskProcessingContextHandler) {
        this.taskProcessingContextHandler = taskProcessingContextHandler;
    }

    /**
     * Defines the {@link HttpTaskProcessingContextFactory} provided by this Mediator
     *
     * @param taskProcessingContextFactory the {@link HttpTaskProcessingContextFactory}
     *                                     to be set
     */
    public void setTaskProcessingContextFactory(HttpTaskProcessingContextFactory taskProcessingContextFactory) {
        this.taskProcessingContextFactory = taskProcessingContextFactory;
    }

    /**
     * Returns the {@link HttpTaskProcessingContextFactory} to be used to create
     * new {@link HttpTaskProcessingContext}
     *
     * @returns the {@link HttpTaskProcessingContextFactory} to be used
     */
    public HttpTaskProcessingContextFactory getTaskProcessingContextFactory() {
        return this.taskProcessingContextFactory;
    }

    /**
     * Defines the {@link HttpChainedTaskProcessingContextFactory} provided by this Mediator
     *
     * @param chainedTaskProcessingContextFactory the {@link HttpChainedTaskProcessingContextFactory}
     *                                            to be set
     */
    public void setChainedTaskProcessingContextFactory(HttpChainedTaskProcessingContextFactory chainedTaskProcessingContextFactory) {
        this.chainedTaskProcessingContextFactory = chainedTaskProcessingContextFactory;
    }

    /**
     * Returns the {@link HttpChainedTaskProcessingContextFactory} to be used to create
     * new {@link HttpTaskProcessingContext} dedicated to chained tasks
     *
     * @returns the {@link HttpChainedTaskProcessingContextFactory} to be used
     */
    public HttpChainedTaskProcessingContextFactory getChainedTaskProcessingContextFactory() {
        return this.chainedTaskProcessingContextFactory;
    }

    @Override
    public void registerProcessingContext(HttpTask<?, ?> key, HttpTaskProcessingContext context) {
        if (this.taskProcessingContextHandler == null || key == null || context == null) {
            return;
        }
        this.taskProcessingContextHandler.registerProcessingContext(key, context);
    }

    @Override
    public void unregisterProcessingContext(HttpTask<?, ?> key) {
        if (this.taskProcessingContextHandler == null || key == null) {
            return;
        }
        this.taskProcessingContextHandler.unregisterProcessingContext(key);
    }

    @Override
    public String resolve(HttpTask<?, ?> task, String property) {
        if (this.taskProcessingContextHandler == null || task == null || property == null) {
            return null;
        }
        return this.taskProcessingContextHandler.resolve(task, property);
    }

    @Override
    public void configure(HttpTask<?, ?> task) throws Exception {
        if (this.taskProcessingContextHandler != null && task != null) {
            this.taskProcessingContextHandler.configure(task);
        }
    }
}
