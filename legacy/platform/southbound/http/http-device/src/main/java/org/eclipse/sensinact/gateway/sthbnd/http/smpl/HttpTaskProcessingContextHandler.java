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

public interface HttpTaskProcessingContextHandler {
    void registerProcessingContext(HttpTask<?, ?> key, HttpTaskProcessingContext context);

    void unregisterProcessingContext(HttpTask<?, ?> key);

    String resolve(HttpTask<?, ?> task, String property);

    void configure(HttpTask<?, ?> task) throws Exception;
}
