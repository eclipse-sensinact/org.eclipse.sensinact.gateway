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

import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

public interface HttpTaskProcessingContextHandler {
    void registerProcessingContext(HttpTask<?, ?> key, HttpTaskProcessingContext context);

    void unregisterProcessingContext(HttpTask<?, ?> key);

    String resolve(HttpTask<?, ?> task, String property);

    void configure(HttpTask<?, ?> task) throws Exception;
}
