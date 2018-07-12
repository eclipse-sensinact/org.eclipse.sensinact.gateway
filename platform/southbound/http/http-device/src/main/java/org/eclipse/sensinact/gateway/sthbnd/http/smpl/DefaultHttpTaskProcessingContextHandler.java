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

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

import java.util.Map;
import java.util.WeakHashMap;

public class DefaultHttpTaskProcessingContextHandler implements HttpTaskProcessingContextHandler {
    Map<Task, HttpTaskProcessingContext> contexts;

    /**
     *
     */
    public DefaultHttpTaskProcessingContextHandler() {
        this.contexts = new WeakHashMap<Task, HttpTaskProcessingContext>();
    }

    /**
     * @inheritDoc
     * @see HttpTaskProcessingContextHandler#
     * unregisterProcessingContext(java.lang.Object)
     */
    @Override
    public void unregisterProcessingContext(HttpTask<?, ?> key) {
        this.contexts.remove(key);
    }

    /**
     * @inheritDoc
     * @see HttpTaskProcessingContextHandler#
     * configure(HttpTask)
     */
    @Override
    public void configure(HttpTask<?, ?> task) throws Exception {
        HttpTaskProcessingContext context = this.contexts.get(task);
        HttpTaskConfigurator configurator = null;
        if (context != null && (configurator = context.getHttpTaskConfigurator()) != null) {
            configurator.configure(task);
        }
    }

    /**
     * @inheritDoc
     * @see HttpTaskProcessingContextHandler#
     * resolve(java.lang.Object, java.lang.String)
     */
    @Override
    public String resolve(HttpTask<?, ?> key, String property) {
        HttpTaskProcessingContext context = this.contexts.get(key);
        if (context == null) {
            return null;
        }
        return context.resolve(property);
    }

    /**
     * @inheritDoc
     * @see HttpTaskProcessingContextHandler#
     * registerProcessingContext(java.lang.Object, HttpTaskProcessingContext)
     */
    @Override
    public void registerProcessingContext(HttpTask<?, ?> key, HttpTaskProcessingContext context) {
        this.contexts.put((Task) key, context);
    }
}
