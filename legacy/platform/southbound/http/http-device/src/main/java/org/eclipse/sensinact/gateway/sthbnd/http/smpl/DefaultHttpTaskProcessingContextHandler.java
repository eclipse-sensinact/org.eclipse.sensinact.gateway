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

    @Override
    public void unregisterProcessingContext(HttpTask<?, ?> key) {
        this.contexts.remove(key);
    }

    @Override
    public void configure(HttpTask<?, ?> task) throws Exception {
        HttpTaskProcessingContext context = this.contexts.get(task);
        HttpTaskConfigurator configurator = null;
        if (context != null && (configurator = context.getHttpTaskConfigurator()) != null) {
            configurator.configure(task);
        }
    }
    
    @Override
    public String resolve(HttpTask<?, ?> key, String property) {
        HttpTaskProcessingContext context = this.contexts.get(key);
        if (context == null) {
            return null;
        }
        return context.resolve(property);
    }

    @Override
    public void registerProcessingContext(HttpTask<?, ?> key, HttpTaskProcessingContext context) {
        this.contexts.put((Task) key, context);
    }
}
